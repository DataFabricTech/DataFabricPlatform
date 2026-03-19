from __future__ import annotations

import json
import os
import math
import copy
from dataclasses import dataclass
from datetime import datetime
from decimal import Decimal
from typing import Any, Dict, List, Optional, Sequence, Tuple
from itertools import islice

from DataProfiling.models.postgres import ProfileColumn, ProfileTable
from DataProfiling.process.profiling_common import FilterOption, ProfilingCommon
from DataProfiling.utils.logger import get_logger

from DataProfiling.utils.trino_dataframe import fetch_as_dataframe


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
            m[str(name)] = {
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

            meta_col = meta_col_map.get(str(col_name), {})
            qualified_name = meta_col.get("qualified_name")
            if not qualified_name:
                qualified_name = f"{meta_data.qualified_name}.{col_name}"

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
                name=str(col_name),
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

    def profiling(self, system_id: str, filter_include, filter_exclude, options: DataProfilingOptions):
        start_time = datetime.now()
        self.logger.info(f"\t data-profiling 시작 [{system_id}]")

        output_dir = self._ensure_output_dir(options.output_dir) if options.generate_report else None

        meta_data_list = self.get_meta_data_by_system_id("table", system_id)

        profiling_filter_count = 0
        profiling_save_count = 0

        try:
            from ydata_profiling import ProfileReport  # type: ignore
        except Exception as e:  # pragma: no cover
            raise RuntimeError(
                "ydata-profiling 이 필요합니다. (pip install ydata-profiling)"
            ) from e

        for meta_data in meta_data_list:
            catalog_name, db_name, table_name = self.get_trino_info(meta_data)

            if self.check_filter(filter_include, filter_exclude, FilterOption.DATABASE.value, db_name):
                profiling_filter_count += 1
                continue

            if self.check_filter(filter_include, filter_exclude, FilterOption.TABLE.value, table_name):
                profiling_filter_count += 1
                continue

            excluded_cols: List[str] = []
            try:
                excluded_cols = self._get_excluded_string_columns_by_avg_len(
                    catalog_name=catalog_name,
                    db_name=db_name,
                    table_name=table_name,
                    max_avg_len=options.max_avg_str_len,
                    sample_rows=options.avg_str_len_sample_rows,
                )
            except Exception as e:
                self.logger.warning(
                    f"avg string length 계산 실패 - 제외 없이 진행 [{system_id}.{db_name}.{table_name}] {e}"
                )

            if excluded_cols:
                self.logger.info(
                    f'\t 제외 컬럼(평균 문자열 길이 >= {options.max_avg_str_len}) '
                    f'[{system_id}.{db_name}.{table_name}] {excluded_cols}'
                )

            query = self._build_query_with_exclusions(
                catalog_name=catalog_name,
                db_name=db_name,
                table_name=table_name,
                limit_rows=options.limit_rows,
                excluded_columns=excluded_cols,
            )

            try:
                df = fetch_as_dataframe(self.trino_connection, query)
                df = self.replace_null(df)
                if len(df) == 0 and query.strip().lower().startswith("select null where 1=0"):
                    self.logger.warning(
                        f"\t 모든 컬럼이 제외되어 스킵 [{system_id}.{db_name}.{table_name}]"
                    )
                    continue
                report = ProfileReport(
                    df,
                    title=f"{system_id}.{db_name}.{table_name}",
                    minimal=True,
                    explorative=options.explorative,
                )

                title = f"{system_id}.{db_name}.{table_name}"

                # 프로파일링 결과를 json 형태로 변환
                profile_dict = json.loads(report.to_json())

                # postgresql upsert (table/columns)
                try:
                    self.upsert_profile_table(meta_data, db_name, profile_dict)
                    self.upsert_profile_columns(meta_data, db_name, table_name, profile_dict)
                except Exception as e:
                    self.logger.error(f"\t postgresql upsert 실패 [{title}] {e}")

                # for key, value in profile_dict.items():
                #     print(f'{key} : {str(value)[:100]}')

                if options.preview:
                    # 옵션에 따라 출력 요약 제공
                    self._preview_top_k = options.preview_top_k  # local override
                    self._render_preview(title, df, report)

                if options.generate_report and output_dir is not None:
                    base = _safe_filename(meta_data.qualified_name or title)
                    out_path = os.path.join(output_dir, 'html', f"{base}.html")
                    out_path_json = os.path.join(output_dir, 'json', f"{base}.json")
                    report.to_file(out_path)
                    report.to_file(out_path_json)
                self.logger.info(
                    f"\t 프로파일링 완료 [{title}] rows={len(df)}"
                )
                profiling_save_count += 1
            except Exception as e:
                self.logger.error(f"data-profiling 실패 [{system_id}.{db_name}.{table_name}] {e}")

        duration = datetime.now() - start_time
        self.logger.info(
            f"data-profiling 종료 [{system_id}]\n"
            f"\t\t\t\t\t\t\t\t\t\t\t\t\t Total Table Count : {len(meta_data_list)}\n"
            f"\t\t\t\t\t\t\t\t\t\t\t\t\t Filter Count : {profiling_filter_count}\n"
            f"\t\t\t\t\t\t\t\t\t\t\t\t\t Saved Count : {profiling_save_count}\n"
            f"\t\t\t\t\t\t\t\t\t\t\t\t\t Total Time : {duration}"
        )

