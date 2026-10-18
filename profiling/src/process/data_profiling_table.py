from __future__ import annotations

import contextlib
import functools
import json
import multiprocessing
import os
import math
import copy
import time
from collections import deque
from concurrent.futures import FIRST_COMPLETED, ProcessPoolExecutor, wait
from concurrent.futures.process import BrokenProcessPool
from dataclasses import dataclass
from datetime import datetime
from decimal import Decimal
from multiprocessing import util as mp_util
from typing import Any, Dict, List, Optional, Sequence, Tuple
from itertools import islice

import numpy as np
from sqlalchemy import select

from DataProfiling.models.postgres import MetaData, ProfileColumn, ProfileTable
from DataProfiling.process.profiling_common import FilterOption, ProfilingCommon
from DataProfiling.process.report_html_localizer import localize_html_to_korean
from DataProfiling.utils.logger import get_logger

from DataProfiling.utils.trino_dataframe import fetch_as_dataframe

# matplotlib 한글 폰트 설정 (ydata-profiling 차트의 한글 깨짐 방지).
# ProfileReport 생성 전에 적용되도록 모듈 import 시점에 1회 설정.
import matplotlib  # noqa: E402
matplotlib.use("Agg")
from matplotlib import font_manager as _font_manager, pyplot as _plt  # noqa: E402
for _font_path in _font_manager.findSystemFonts():
    _lower = _font_path.lower()
    if "notosanscjk" in _lower or "notosanskr" in _lower or "nanum" in _lower:
        try:
            _font_manager.fontManager.addfont(_font_path)
        except Exception:
            pass
# 등록된 폰트 이름은 환경에 따라 'Noto Sans CJK JP/KR' 등으로 달라지므로 부분일치로 탐지
_korean_font = None
for _f in _font_manager.fontManager.ttflist:
    if "Noto Sans CJK" in _f.name or "Noto Sans KR" in _f.name or "Nanum" in _f.name:
        _korean_font = _f.name
        break
if _korean_font:
    _plt.rcParams["font.family"] = _korean_font
    _plt.rcParams["font.sans-serif"] = [_korean_font] + list(_plt.rcParams.get("font.sans-serif", []))
_plt.rcParams["axes.unicode_minus"] = False

# 워드클라우드 한글 폰트 지정 + (폰트, 크기)별 폰트 캐시
import wordcloud.wordcloud as _wordcloud_module  # noqa: E402
import ydata_profiling.visualisation.plot as _ydata_plot  # noqa: E402
from PIL import ImageFont as _ImageFont  # noqa: E402

_font_files = {os.path.basename(_f.fname).lower(): _f.fname for _f in _font_manager.fontManager.ttflist}
_wordcloud_font = next(
    (_font_files[name] for name in ("notosanscjk-regular.ttc", "nanumgothic.ttf", "malgun.ttf") if name in _font_files),
    None,
)
if _wordcloud_font:
    _ydata_plot.WordCloud = functools.partial(_ydata_plot.WordCloud, font_path=_wordcloud_font)


class _CachedImageFont:
    truetype = staticmethod(functools.lru_cache(maxsize=1024)(_ImageFont.truetype))

    def __getattr__(self, name):
        return getattr(_ImageFont, name)


_wordcloud_module.ImageFont = _CachedImageFont()

_HEAVY_KEYS = ("value_counts_without_nan", "value_counts_index_sorted", "word_counts")
_BIG_TABLE_ROWS = 100_000


def _json_key(value: Any) -> Any:
    """ydata 의 to_json 과 같은 규칙으로 값을 바꾼다(numpy 값 -> 파이썬 값, 그 밖의 객체 -> 문자열)."""
    if isinstance(value, (bool, int, float, str)):
        return value
    if isinstance(value, np.generic):
        return value.item()
    return str(value)


def _safe_filename(name: str) -> str:
    return (
        name.replace("/", "_")
        .replace("\\", "_")
        .replace(" ", "_")
        .replace(":", "_")
        .replace("|", "_")
    )


@dataclass(frozen=True)
class DataProfilingOptions:
    limit_rows: int = 10000
    output_dir: str = "reports/data_profiling"
    explorative: bool = True
    generate_report: bool = False
    preview: bool = True
    preview_top_k: int = 20
    max_avg_str_len: int = 500
    avg_str_len_sample_rows: int = 200
    workers: int = 1
    big_workers: int = 1
    localize_report: bool = False
class DataProfilingTable(ProfilingCommon):
    logger = get_logger()

    def _nan_to_none(self, v: Any):
        if isinstance(v, float) and math.isnan(v):
            return None
        return v

    def _to_decimal_or_none(self, v: Any) -> Optional[Decimal]:
        v = self._nan_to_none(v)
        if v is None:
            return None
        try:
            return Decimal(str(v))
        except Exception:
            return None

    def _parse_meta_columns(self, meta_data) -> Dict[str, Dict[str, Any]]:
        attrs = getattr(meta_data, "attributes", None)
        if attrs is None:
            return {}

        if isinstance(attrs, str):
            try:
                attrs = json.loads(attrs)
            except Exception:
                return {}

        if not isinstance(attrs, dict):
            return {}

        cols = attrs.get("columns") or []
        if not isinstance(cols, list):
            return {}

        m: Dict[str, Dict[str, Any]] = {}
        for c in cols:
            if not isinstance(c, dict):
                continue
            name = c.get("meta_name")
            if not name:
                continue
            m[str(name).lower()] = {
                "meta_name": str(name),
                "qualified_name": c.get("qualified_name"),
                "column_type": c.get("column_type"),
            }
        return m

    def upsert_profile_table(
        self,
        meta_data,
        db_name: str,
        profile_dict: Dict[str, Any],
        err_description: Optional[str] = None,
    ):
        table = profile_dict.get("table") or {}
        if not isinstance(table, dict):
            table = {}

        update_data = dict(
            meta_id=meta_data.meta_id,
            name=meta_data.meta_name,
            meta_type=meta_data.meta_type,
            qualified_name=meta_data.qualified_name,
            status=getattr(meta_data, "status", None),
            db_name=db_name,
            update_date=datetime.now(),
            last_modified_time=None,
            err_description=err_description,
            sample_data_url=None,
            n_rows=self._nan_to_none(table.get("n")),
            n_columns=self._nan_to_none(table.get("n_var")),
            memory_size=self._nan_to_none(table.get("memory_size")),
            record_size=self._to_decimal_or_none(table.get("record_size")),
            n_cells_missing=self._nan_to_none(table.get("n_cells_missing")),
            n_vars_with_missing=self._nan_to_none(table.get("n_vars_with_missing")),
            n_vars_all_missing=self._nan_to_none(table.get("n_vars_all_missing")),
            p_cells_missing=self._to_decimal_or_none(table.get("p_cells_missing")),
            column_types=table.get("types"),
        )

        self.upsert_data(ProfileTable, update_data, "meta_id")

    def upsert_profile_columns(
        self,
        meta_data,
        db_name: str,
        table_name: str,
        profile_dict: Dict[str, Any],
        err_description: Optional[str] = None,
    ):
        variables = profile_dict.get("variables") or {}
        if not isinstance(variables, dict):
            return

        meta_col_map = self._parse_meta_columns(meta_data)

        for col_name, v in variables.items():
            if not isinstance(v, dict):
                continue

            meta_col = meta_col_map.get(str(col_name).lower(), {})
            col_display_name = meta_col.get("meta_name", str(col_name))
            qualified_name = meta_col.get("qualified_name")
            if not qualified_name:
                qualified_name = f"{meta_data.qualified_name}.{col_display_name}"

            top_counts = v.get("value_counts_without_nan")
            if hasattr(top_counts, 'items'):
                top_value_counts = copy.deepcopy(dict(islice(top_counts.items(), 5)))
            else:
                top_value_counts = {}
            
            # jsonb 용량 문제로 인해 아래 항목들 제거
            keys_to_remove = [
                "value_counts_without_nan", 
                "value_counts_index_sorted",
                "word_counts"
            ]

            for k in keys_to_remove:
                v.pop(k, None)
                
            update_data = dict(
                column_id=qualified_name,
                name=col_display_name,
                meta_type=meta_col.get("column_type"),
                data_type=v.get("type"),
                qualified_name=qualified_name,
                status="ACTIVE",
                db_name=db_name,
                table_name=table_name,
                update_date=datetime.now(),
                err_description=err_description,
                hashable=v.get("hashable"),
                ordering=v.get("ordering"),
                n_missing=self._nan_to_none(v.get("n_missing")),
                n=self._nan_to_none(v.get("n")),
                p_missing=self._to_decimal_or_none(v.get("p_missing")),
                count=self._nan_to_none(v.get("count")),
                memory_size=self._nan_to_none(v.get("memory_size")),
                n_distinct=self._nan_to_none(v.get("n_distinct")),
                p_distinct=self._to_decimal_or_none(v.get("p_distinct")),
                is_unique=v.get("is_unique"),
                n_unique=self._nan_to_none(v.get("n_unique")),
                p_unique=self._to_decimal_or_none(v.get("p_unique")),
                top_value_counts=top_value_counts,
                attributes=self.replace_nan(v),
            )

            self.upsert_data(ProfileColumn, update_data, "column_id")

    def _light_profile_dict(self, report) -> Dict[str, Any]:
        """json.loads(report.to_json()) 과 같은 dict. DB 에 넣지 않는 큰 항목은 빼고 직렬화한다."""
        variables = report.get_description().variables
        tops: Dict[str, Optional[Dict[str, Any]]] = {}
        heavy: Dict[str, Dict[str, Any]] = {}
        for col, v in variables.items():
            counts = v.get("value_counts_without_nan")
            tops[col] = None if counts is None else json.loads(
                json.dumps({_json_key(k): _json_key(c) for k, c in islice(counts.items(), 5)})
            )
            heavy[col] = {k: v.pop(k) for k in _HEAVY_KEYS if k in v}
        try:
            profile_dict = json.loads(report.to_json())
        finally:
            for col, values in heavy.items():
                variables[col].update(values)

        for col, top in tops.items():
            if top is not None and col in (profile_dict.get("variables") or {}):
                profile_dict["variables"][col]["value_counts_without_nan"] = top
        return profile_dict

    def replace_null(self, df):
        """
        지정된 문자열 값들을 결측치(np.nan)로 치환한 DataFrame을 반환한다.
        """
        null_values = ["", " ", "-", "NA", "N/A", "NULL", "null"]
        try:
            import numpy as np  # type: ignore
        except Exception as e:  # pragma: no cover
            raise RuntimeError("numpy 가 필요합니다. (pip install numpy)") from e

        return df.replace(to_replace=null_values, value=np.nan)

    def _ensure_output_dir(self, output_dir: str) -> str:
        os.makedirs(output_dir, exist_ok=True)
        return output_dir

    def _build_query(self, catalog_name: str, db_name: str, table_name: str, limit_rows: int) -> str:
        limit_rows = max(int(limit_rows), 1)
        return f'SELECT * FROM "{catalog_name}"."{db_name}"."{table_name}" LIMIT {limit_rows}'

    def _get_table_columns(self, catalog_name: str, db_name: str, table_name: str) -> List[Tuple[str, str]]:
        """
        Trino information_schema로 컬럼명/타입을 가져온다.
        return: [(column_name, data_type), ...]
        """
        q = (
            f'SELECT column_name, data_type '
            f'FROM "{catalog_name}".information_schema.columns '
            f"WHERE table_schema = '{db_name}' AND table_name = '{table_name}' "
            f"ORDER BY ordinal_position"
        )
        rows: Sequence[Tuple[Any, Any]] = self.trino_get_all(q) or []
        return [(str(r[0]), str(r[1])) for r in rows]

    def _is_string_type(self, data_type: str) -> bool:
        t = (data_type or "").lower()
        return (
            t == "varchar"
            or t.startswith("varchar(")
            or t == "char"
            or t.startswith("char(")
            or t == "string"
        )

    def _get_excluded_string_columns_by_avg_len(
        self,
        catalog_name: str,
        db_name: str,
        table_name: str,
        max_avg_len: int,
        sample_rows: int,
    ) -> List[str]:
        """
        문자열 컬럼 대상으로, (샘플) 평균 길이가 max_avg_len 이상인 컬럼을 제외 리스트로 반환.
        - 원천데이터를 Python으로 끌어오기 전에 Trino에서 길이만 계산해 OOM을 방지.
        """
        max_avg_len = int(max_avg_len)
        sample_rows = max(int(sample_rows), 1)

        cols = self._get_table_columns(catalog_name, db_name, table_name)
        str_cols = [c for c, t in cols if self._is_string_type(t)]
        if not str_cols:
            return []

        # 샘플 row만 대상으로 평균 길이 계산 (한 번의 스캔으로 처리)
        select_cols = ", ".join([f'"{c}"' for c in str_cols])
        avg_exprs = ", ".join([f'AVG(LENGTH(CAST("{c}" AS VARCHAR))) AS "{c}"' for c in str_cols])
        q = (
            f"SELECT {avg_exprs} "
            f'FROM (SELECT {select_cols} FROM "{catalog_name}"."{db_name}"."{table_name}" '
            f"LIMIT {sample_rows}) t"
        )

        row = self.trino_get_one(q)
        if row is None:
            return []

        excluded: List[str] = []
        for c, avg_len in zip(str_cols, row):
            try:
                if avg_len is not None and float(avg_len) >= max_avg_len:
                    excluded.append(c)
            except Exception:
                # 계산 불가/형 변환 실패 시 제외하지 않음
                pass

        return excluded

    def _build_query_with_exclusions(
        self,
        catalog_name: str,
        db_name: str,
        table_name: str,
        limit_rows: int,
        excluded_columns: List[str],
    ) -> str:
        limit_rows = max(int(limit_rows), 1)
        if not excluded_columns:
            return f'SELECT * FROM "{catalog_name}"."{db_name}"."{table_name}" LIMIT {limit_rows}'

        cols = self._get_table_columns(catalog_name, db_name, table_name)
        included = [c for c, _t in cols if c not in set(excluded_columns)]

        if not included:
            # 전부 제외된 경우: 빈 결과를 반환해 테이블 스킵 처리 유도
            return f'SELECT NULL WHERE 1=0'

        select_cols = ", ".join([f'"{c}"' for c in included])
        return f'SELECT {select_cols} FROM "{catalog_name}"."{db_name}"."{table_name}" LIMIT {limit_rows}'

    def _render_preview(self, title: str, df, report) -> None:
        """
        ydata-profiling 결과를 메모리에서 요약해 일부만 출력한다.
        - report.get_description(): dict 형태 요약
        - report.to_html(): HTML 문자열(원하면 확장 가능)
        """
        print("\n" + "=" * 80)
        print(f"[DataProfiling Preview] {title}")
        print(f"- rows: {len(df)}")
        print(f"- cols: {len(getattr(df, 'columns', []))}")

        desc: Optional[Dict[str, Any]] = None
        try:
            desc = report.get_description()
        except Exception:
            desc = None

        if not desc:
            print("- (no description available)")
            return

        table = desc.get("table", {}) if isinstance(desc, dict) else {}
        if isinstance(table, dict):
            for k in ("n", "n_var", "n_obs", "n_cells_missing", "p_cells_missing", "n_duplicates", "p_duplicates"):
                if k in table:
                    print(f"- {k}: {table.get(k)}")

        variables = desc.get("variables") if isinstance(desc, dict) else None
        try:
            import pandas as pd  # type: ignore
        except Exception:
            pd = None  # type: ignore

        if pd is not None and variables is not None:
            try:
                var_df = pd.DataFrame(variables)
                if "p_missing" in var_df.columns:
                    var_df = var_df.sort_values("p_missing", ascending=False)

                top_k = max(int(getattr(self, "_preview_top_k", 20)), 1)
                top_k = min(top_k, len(var_df))
                show_cols = [c for c in ["type", "p_missing", "n_missing", "n_distinct"] if c in var_df.columns]
                if len(show_cols) == 0:
                    show_cols = list(var_df.columns)[:6]

                print(f"\n- top variables (by missing) top_k={top_k}")
                # index(컬럼명) 중심으로 보기 좋게 출력
                preview_df = var_df[show_cols].head(top_k)
                with pd.option_context("display.max_rows", top_k, "display.max_columns", 50, "display.width", 200):
                    print(preview_df)
            except Exception:
                pass

    def _cleanup_deleted_html(self, system_id: str, output_dir: str) -> None:
        """status=DELETE 테이블의 stale HTML 파일 삭제 (디스크 누수 방지)."""
        if not output_dir or not os.path.isdir(output_dir):
            return
        deleted_metas: List[MetaData] = self.get_all(
            select(MetaData)
            .where(MetaData.status == "DELETE",
                   MetaData.meta_type == "table",
                   MetaData.qualified_name.like(f"{system_id}%"))
        )
        removed = 0
        for m in deleted_metas:
            for suffix in (".html", ".html.raw", ".html.html"):
                f = os.path.join(output_dir, f"{m.meta_id}{suffix}")
                if os.path.exists(f):
                    os.remove(f)
                    removed += 1
        if removed:
            self.logger.info(f"\t stale HTML {removed}개 정리 [{system_id}]")

    def _write_report(self, report, meta_id: str, output_dir: str, localize: bool) -> None:
        """HTML 을 다 만든 뒤 임시 파일에 쓰고 이름을 바꾼다."""
        html = report.to_html()
        if localize:
            html = localize_html_to_korean(html)
            report_path = os.path.join(output_dir, f"{meta_id}.html")
        else:
            report_path = os.path.join(output_dir, f"{meta_id}.html.raw")

        tmp_path = f"{report_path}.tmp"
        with open(tmp_path, "w", encoding="utf-8") as f:
            f.write(html)
        os.replace(tmp_path, report_path)

        if localize:
            for suffix in (".html.raw", ".html.html"):
                stale_path = os.path.join(output_dir, f"{meta_id}{suffix}")
                if os.path.exists(stale_path):
                    os.remove(stale_path)

    def profile_one(self, meta_data, system_id: str, options: DataProfilingOptions, output_dir: Optional[str]) -> bool:
        """테이블 하나를 프로파일링해 DB 에 저장하고, 설정에 따라 리포트를 쓴다. 저장했으면 True."""
        from ydata_profiling import ProfileReport  # type: ignore

        started = time.perf_counter()
        catalog_name, db_name, table_name = self.get_trino_info(meta_data)
        title = f"{system_id}.{db_name}.{table_name}"

        excluded_cols: List[str] = []
        try:
            excluded_cols = self._get_excluded_string_columns_by_avg_len(
                catalog_name=catalog_name, db_name=db_name, table_name=table_name,
                max_avg_len=options.max_avg_str_len, sample_rows=options.avg_str_len_sample_rows,
            )
        except Exception as e:
            self.logger.warning(f"avg string length 계산 실패 - 제외 없이 진행 [{title}] {e}")

        try:
            query = self._build_query_with_exclusions(
                catalog_name=catalog_name, db_name=db_name, table_name=table_name,
                limit_rows=options.limit_rows, excluded_columns=excluded_cols,
            )
            df = self.replace_null(fetch_as_dataframe(self.trino_connection, query))
            if len(df) == 0 and query.strip().lower().startswith("select null where 1=0"):
                self.logger.warning(f"\t 모든 컬럼이 제외되어 스킵 [{title}]")
                return False

            parallel_kwargs = {"pool_size": 1, "progress_bar": False} if options.workers > 1 else {}
            report = ProfileReport(df, title=title, minimal=True, explorative=options.explorative, **parallel_kwargs)
            profile_dict = self._light_profile_dict(report)
            self.upsert_profile_table(meta_data, db_name, profile_dict)
            self.upsert_profile_columns(meta_data, db_name, table_name, profile_dict)

            if options.preview:
                self._preview_top_k = options.preview_top_k
                self._render_preview(title, df, report)

            if options.generate_report and output_dir is not None:
                self._write_report(report, meta_data.meta_id, output_dir, options.localize_report)

            self.logger.info(f"\t 프로파일링 완료 [{title}] rows={len(df)} {time.perf_counter() - started:.1f}s")
            return True
        except Exception as e:
            self.logger.error(f"data-profiling 실패 [{title}] {e}")
            return False

    def _estimate_rows(self, meta_data: MetaData) -> Optional[int]:
        """Trino SHOW STATS 의 row_count. 통계가 없으면 None."""
        catalog_name, db_name, table_name = self.get_trino_info(meta_data)
        try:
            rows = self.trino_get_all(f'SHOW STATS FOR "{catalog_name}"."{db_name}"."{table_name}"')
        except Exception:
            return None
        for row in rows or []:
            if row[0] is None and row[4] is not None:  # column_name 이 NULL 인 행의 row_count
                return int(row[4])
        return None

    def _plan_parallel(self, targets: List[MetaData], options: DataProfilingOptions) -> Dict[bool, deque]:
        """예상 행 수(지난 n_rows → Trino 통계 → limit_rows) 내림차순으로 {True: 큰 테이블, False: 일반} 큐를 만든다."""
        prev_rows = dict(self.execute(
            select(ProfileTable.meta_id, ProfileTable.n_rows)
            .where(ProfileTable.meta_id.in_([m.meta_id for m in targets]))
        ).all())
        self.db_session.commit()  # 긴 실행 동안 트랜잭션을 열어 두지 않는다

        estimated = assumed = 0
        for meta_data in targets:
            if prev_rows.get(meta_data.meta_id) is None:
                prev_rows[meta_data.meta_id] = self._estimate_rows(meta_data)
                if prev_rows[meta_data.meta_id] is None:
                    assumed += 1
                else:
                    estimated += 1
        if estimated or assumed:
            self.logger.info(f"\t 지난 결과가 없는 테이블: Trino 통계로 추정 {estimated}개, 큰 테이블로 간주 {assumed}개")

        def expected_rows(meta_id: str) -> int:
            n_rows = prev_rows.get(meta_id)
            return options.limit_rows if n_rows is None else min(n_rows, options.limit_rows)

        ordered = sorted((m.meta_id for m in targets), key=expected_rows, reverse=True)
        return {
            True: deque(i for i in ordered if expected_rows(i) >= _BIG_TABLE_ROWS),
            False: deque(i for i in ordered if expected_rows(i) < _BIG_TABLE_ROWS),
        }

    def _profile_parallel(self, targets: List[MetaData], system_id: str, options: DataProfilingOptions,
                          output_dir: Optional[str]) -> int:
        """테이블을 워커 프로세스에 나눠 처리하고 저장한 수를 돌려준다. 큰 테이블은 동시에 big_workers 개까지."""
        queues = self._plan_parallel(targets, options)
        total = len(queues[True]) + len(queues[False])
        big_limit = max(1, min(options.big_workers, options.workers))
        self.logger.info(
            f"\t 병렬 프로파일링 시작: 대상 {total}개(큰 테이블 {len(queues[True])}개), "
            f"워커 {options.workers}개(큰 테이블은 동시에 {big_limit}개까지)"
        )

        for key in ("OMP_NUM_THREADS", "OPENBLAS_NUM_THREADS", "MKL_NUM_THREADS"):
            os.environ.setdefault(key, "1")

        def new_pool() -> ProcessPoolExecutor:
            return ProcessPoolExecutor(
                max_workers=options.workers,
                mp_context=multiprocessing.get_context("spawn"),
                initializer=_init_profiling_worker,
            )

        pool = new_pool()
        running: Dict[Any, Tuple[str, bool]] = {}
        retried = set()
        saved = finished = 0
        next_progress = 100
        try:
            while queues[True] or queues[False] or running:
                while len(running) < options.workers:
                    running_big = sum(is_big for _, is_big in running.values())
                    is_big = bool(queues[True]) and running_big < big_limit
                    if not is_big and not queues[False]:
                        break
                    meta_id = queues[is_big].popleft()
                    future = pool.submit(_profile_in_worker, meta_id, system_id, options, output_dir)
                    running[future] = (meta_id, is_big)

                done, _ = wait(running, return_when=FIRST_COMPLETED)
                lost: List[Tuple[str, bool]] = []
                for future in done:
                    meta_id, is_big = running.pop(future)
                    try:
                        saved += bool(future.result())
                        finished += 1
                    except BrokenProcessPool:
                        lost.append((meta_id, is_big))
                    except Exception as e:
                        finished += 1
                        self.logger.error(f"data-profiling 실패 [{meta_id}] {e}")

                if lost:
                    lost += list(running.values())
                    running.clear()
                    pool.shutdown(wait=False)
                    pool = new_pool()
                    self.logger.warning(f"\t 워커 비정상 종료 - 풀을 다시 만들고 {len(lost)}개 테이블을 다시 시도")
                    for meta_id, is_big in lost:
                        if meta_id in retried:
                            finished += 1
                            self.logger.error(f"data-profiling 실패 [{meta_id}] 워커 비정상 종료가 반복되어 건너뜀")
                        else:
                            retried.add(meta_id)
                            queues[is_big].appendleft(meta_id)

                if finished >= next_progress:
                    self.logger.info(f"\t 진행 {finished}/{total} (저장 {saved})")
                    next_progress += 100
        finally:
            pool.shutdown()
        return saved

    def profiling(self, system_id: str, filter_include, filter_exclude, options: DataProfilingOptions):
        start_time = datetime.now()
        self.logger.info(f"\t data-profiling 시작 [{system_id}] workers={options.workers}")

        output_dir = self._ensure_output_dir(options.output_dir) if options.generate_report else None

        if options.generate_report and output_dir is not None:
            self._cleanup_deleted_html(system_id, output_dir)

        meta_data_list = self.get_meta_data_by_system_id("table", system_id)
        profiling_filter_count = 0
        targets: List[MetaData] = []
        for meta_data in meta_data_list:
            _catalog_name, db_name, table_name = self.get_trino_info(meta_data)
            if self.check_filter(filter_include, filter_exclude, FilterOption.DATABASE.value, db_name):
                profiling_filter_count += 1
                continue
            if self.check_filter(filter_include, filter_exclude, FilterOption.TABLE.value, table_name):
                profiling_filter_count += 1
                continue
            targets.append(meta_data)

        if options.workers > 1 and len(targets) > 1:
            profiling_save_count = self._profile_parallel(targets, system_id, options, output_dir)
        else:
            profiling_save_count = sum(self.profile_one(m, system_id, options, output_dir) for m in targets)

        duration = datetime.now() - start_time
        self.logger.info(
            f"data-profiling 종료 [{system_id}]\n"
            f"\t\t\t\t\t\t\t\t\t\t\t\t\t Total Table Count : {len(meta_data_list)}\n"
            f"\t\t\t\t\t\t\t\t\t\t\t\t\t Filter Count : {profiling_filter_count}\n"
            f"\t\t\t\t\t\t\t\t\t\t\t\t\t Saved Count : {profiling_save_count}\n"
            f"\t\t\t\t\t\t\t\t\t\t\t\t\t Workers : {options.workers}\n"
            f"\t\t\t\t\t\t\t\t\t\t\t\t\t Total Time : {duration}"
        )


_worker: Optional[DataProfilingTable] = None


def _init_profiling_worker() -> None:
    """워커 프로세스마다 DB 세션과 Trino 커서를 연다."""
    global _worker
    from DataProfiling.configs.database_session import create_session as db_create_session
    from DataProfiling.configs.trino_session import create_session as trino_create_session

    sessions = contextlib.ExitStack()
    db_session = sessions.enter_context(db_create_session())
    trino_cursor = sessions.enter_context(trino_create_session())
    mp_util.Finalize(None, sessions.close, exitpriority=10)
    _worker = DataProfilingTable(db_session, trino_cursor)


def _profile_in_worker(meta_id: str, system_id: str, options: DataProfilingOptions, output_dir: Optional[str]) -> bool:
    meta_data = _worker.get_one(select(MetaData).where(MetaData.meta_id == meta_id))
    if meta_data is None:
        return False
    return _worker.profile_one(meta_data, system_id, options, output_dir)

