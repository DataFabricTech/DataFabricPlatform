from __future__ import annotations

import json
import os
import math
import copy
import re
from pathlib import Path
from dataclasses import dataclass
from datetime import datetime
from decimal import Decimal
from typing import Any, Dict, List, Optional, Sequence, Tuple
from itertools import islice

from sqlalchemy import select

from DataProfiling.models.postgres import MetaData, ProfileColumn, ProfileTable
from DataProfiling.process.profiling_common import FilterOption, ProfilingCommon
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
    report_language: str = "en"
    preview: bool = True
    preview_top_k: int = 20
    max_avg_str_len: int = 500
    avg_str_len_sample_rows: int = 200
class DataProfilingTable(ProfilingCommon):
    logger = get_logger()

    # ydata-profiling 4.3.2는 한국어 로케일을 제공하지 않는다. 리포트에 포함되는
    # 화면용 고정 문구만 바꾸며, 실제 컬럼명·데이터 값은 변경하지 않는다.
    _REPORT_KO_LABELS = {
        "Toggle navigation": "탐색 메뉴 열기/닫기",
        "Overview": "개요",
        "Variables": "변수",
        "Alerts": "알림",
        "Reproduction": "재현 정보",
        "Correlations": "상관관계",
        "Interactions": "상호작용",
        "Missing values": "결측값",
        "Columns": "컬럼",
        "Table": "표",
        "Heatmap": "히트맵",
        "Scatter": "산점도",
        "Scatter plot": "산점도",
        "Dataset statistics": "데이터셋 통계",
        "Variable types": "변수 유형",
        "Number of variables": "변수 수",
        "Number of observations": "관측치 수",
        "Missing cells (%)": "결측 셀 비율(%)",
        "Missing cells": "결측 셀 수",
        "Duplicate rows (%)": "중복 행 비율(%)",
        "Duplicate rows": "중복 행 수",
        "Total size in memory": "메모리 내 전체 크기",
        "Average record size in memory": "평균 레코드 메모리 크기",
        "Analysis started": "분석 시작 시각",
        "Analysis finished": "분석 종료 시각",
        "Duration": "소요 시간",
        "Software version": "소프트웨어 버전",
        "Download configuration": "설정 내려받기",
        "Dataset": "데이터셋",
        "Variable descriptions": "변수 설명",
        "Copyright": "저작권",
        "Name": "이름",
        "Parent": "상위 경로",
        "Anchor": "앵커",
        "Root": "루트",
        "Select Columns": "컬럼 선택",
        "More details": "상세 정보",
        "Statistics": "통계",
        "Histogram": "히스토그램",
        "Histogram(s)": "히스토그램",
        "Common values": "빈도 높은 값",
        "Common Values": "빈도 높은 값",
        "Common Values (Table)": "빈도 높은 값(표)",
        "Common Values (Plot)": "빈도 높은 값(그래프)",
        "Common words": "빈도 높은 단어",
        "Common prefix": "공통 접두사",
        "Most frequently occurring": "가장 자주 발생한 값",
        "Extreme values": "극값",
        "Quantile statistics": "분위수 통계",
        "Descriptive statistics": "기술 통계",
        "Distinct (%)": "고유값 비율(%)",
        "Distinct": "고유값 수",
        "CONSTANT": "상수값",
        "Constant": "상수값",
        "MISSING": "결측값",
        "Missing (%)": "결측 비율(%)",
        "Missing": "결측 수",
        "Infinite (%)": "무한값 비율(%)",
        "Infinite": "무한값 수",
        "Unique (%)": "유일값 비율(%)",
        "Unique": "유일값 수",
        "Mean": "평균",
        "Median": "중앙값",
        "Minimum": "최솟값",
        "Maximum": "최댓값",
        "Zeros (%)": "0값 비율(%)",
        "Zeros": "0값 수",
        "Negative (%)": "음수 비율(%)",
        "Negative": "음수 수",
        "Memory size": "메모리 크기",
        "Autocorrelation": "자기상관",
        "Augmented Dickey-Fuller test p-value": "확장 Dickey-Fuller 검정 p값",
        "Standard deviation": "표준편차",
        "Coefficient of variation (CV)": "변동계수(CV)",
        "Kurtosis": "첨도",
        "Median Absolute Deviation (MAD)": "중앙절대편차(MAD)",
        "Skewness": "왜도",
        "Sum": "합계",
        "Variance": "분산",
        "Monotonicity": "단조성",
        "Strictly increasing": "엄격 증가",
        "Increasing": "증가",
        "Not monotonic": "비단조",
        "Decreasing": "감소",
        "Strictly decreasing": "엄격 감소",
        "Interquartile range (IQR)": "사분위 범위(IQR)",
        "Range": "범위",
        "Real number (&Ropf;)": "실수(&Ropf;)",
        "5-th percentile": "5백분위수",
        "95-th percentile": "95백분위수",
        "Q1": "제1사분위수",
        "median": "중앙값",
        "Q3": "제3사분위수",
        "Value": "값",
        "Count": "개수",
        "Frequency (%)": "빈도(%)",
        "Minimum 5 values": "최솟값 5개",
        "Maximum 5 values": "최댓값 5개",
        "Words": "단어 수",
        "Characters and Unicode": "문자 및 유니코드",
        "Unicode": "유니코드",
        "Characters": "문자 수",
        "Length": "길이",
        "File": "파일",
        "File size": "파일 크기",
        "Extension": "확장자",
        "Image": "이미지",
        "Dimensions": "크기",
        "Exif keys": "Exif 키",
        "Exif data": "Exif 데이터",
        "Min width": "최소 너비",
        "Median width": "중앙 너비",
        "Max width": "최대 너비",
        "Min height": "최소 높이",
        "Median height": "중앙 높이",
        "Max height": "최대 높이",
        "Min area": "최소 면적",
        "Median area": "중앙 면적",
        "Max area": "최대 면적",
        "Max length": "최대 길이",
        "Median length": "중앙 길이",
        "Mean length": "평균 길이",
        "Min length": "최소 길이",
        "Total characters": "전체 문자 수",
        "Distinct characters": "고유 문자 수",
        "Distinct categories": "고유 범주 수",
        "Distinct scripts": "고유 문자 체계 수",
        "Distinct blocks": "고유 블록 수",
        "Categories": "범주",
        "Scripts": "문자 체계",
        "Blocks": "블록",
        "Most occurring characters": "가장 자주 등장한 문자",
        "Most occurring categories": "가장 자주 등장한 범주",
        "Most occurring scripts": "가장 자주 등장한 문자 체계",
        "Most occurring blocks": "가장 자주 등장한 블록",
        "Most frequent character per category": "범주별 최빈 문자",
        "Most frequent character per script": "문자 체계별 최빈 문자",
        "Most frequent character per block": "블록별 최빈 문자",
        "The Unicode Standard assigns character properties to each code point, which can be used to analyse textual variables.": "유니코드 표준은 각 코드 포인트에 문자 속성을 부여하며, 이를 통해 텍스트형 변수를 분석할 수 있습니다.",
        "Other Letter": "기타 문자",
        "Hangul": "한글",
        "Sample": "표본",
        "URL": "URL",
        "url stats": "URL 통계",
        "Full": "전체 URL",
        "Scheme": "스킴",
        "Netloc": "네트워크 위치",
        "Path": "경로",
        "Query": "쿼리",
        "Fragment": "프래그먼트",
        "Stem": "파일명(확장자 제외)",
        "Unique anchors": "고유 앵커 수",
        "Unique directories": "고유 디렉터리 수",
        "Unique extensions": "고유 확장자 수",
        "Unique names": "고유 파일명 수",
        "Unique stems": "고유 파일명(확장자 제외) 수",
        "1st row": "1번째 행",
        "2nd row": "2번째 행",
        "3rd row": "3번째 행",
        "4th row": "4번째 행",
        "5th row": "5번째 행",
        "Numeric": "수치형",
        "Text": "텍스트",
        "Boolean": "불리언",
        "Date": "날짜",
        "Categorical": "범주형",
        "Unsupported": "지원되지 않는 유형",
        "Real number (ℝ)": "실수(ℝ)",
        "UNIQUE": "유일값",
        "has unique values": "고유값을 가집니다",
        "Histogram with fixed size bins": "고정 크기 구간 히스토그램",
        "(bins=": "(구간 수=",
        "Report generated by": "리포트 생성 도구",
        "Dataset is empty": "데이터셋이 비어 있습니다",
        "Dataset has duplicated values": "데이터셋에 중복 값이 있습니다",
        " has a constant value": "는 상수 값입니다",
        " has a high cardinality": "는 고유값 수가 많습니다",
        " has infinite values": "에 무한값이 있습니다",
        " has missing values": "에 결측값이 있습니다",
        " is non stationary": "는 비정상 시계열입니다",
        " is seasonal": "는 계절성을 가집니다",
        " is uniformly distributed": "는 균등 분포를 따릅니다",
        " has unique values": "는 유일값을 가집니다",
        " is an unsupported type, check if it needs cleaning or further analysis": "는 지원되지 않는 유형입니다. 정제 또는 추가 분석이 필요한지 확인하세요",
        " has predominantly zeros": "는 대부분 0값입니다",
        " was rejected": "는 제외되었습니다",
    }

    @classmethod
    def _localize_report_html_to_korean(cls, report_path: str) -> None:
        path = Path(report_path)
        html = path.read_text(encoding="utf-8")

        replacements = sorted(
            cls._REPORT_KO_LABELS.items(), key=lambda item: len(item[0]), reverse=True
        )

        def localize_text_node(match: re.Match) -> str:
            text = match.group(0)

            # ydata-profiling 경고는 컬럼명·건수 등이 조합된 동적 문장이다.
            # 텍스트 노드에서만 처리해 스크립트 식별자에는 영향을 주지 않는다.
            text = re.sub(
                r'(.+?) has constant value ""',
                r"\1: 상수값",
                text,
            )
            text = re.sub(
                r"(.+?) has (\d[\d,]*) \(([\d.]+%)\) missing values",
                r"\1: 결측값 \2개(\3)",
                text,
            )
            for english, korean in replacements:
                text = text.replace(english, korean)
            return text

        blocks = re.split(
            r"(<(?:script|style)\b[^>]*>.*?</(?:script|style)\s*>)",
            html,
            flags=re.IGNORECASE | re.DOTALL,
        )
        for index in range(0, len(blocks), 2):
            blocks[index] = re.sub(r"(?<=>)[^<]+(?=<)", localize_text_node, blocks[index])
        html = "".join(blocks)
        path.write_text(html, encoding="utf-8")

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
            f = os.path.join(output_dir, f"{m.meta_id}.html")
            if os.path.exists(f):
                os.remove(f)
                removed += 1
        if removed:
            self.logger.info(f"\t stale HTML {removed}개 정리 [{system_id}]")

    def profiling(self, system_id: str, filter_include, filter_exclude, options: DataProfilingOptions):
        start_time = datetime.now()
        self.logger.info(f"\t data-profiling 시작 [{system_id}]")

        output_dir = self._ensure_output_dir(options.output_dir) if options.generate_report else None

        if options.generate_report and output_dir is not None:
            self._cleanup_deleted_html(system_id, output_dir)

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
                    catalog_name=catalog_name, db_name=db_name, table_name=table_name,
                    max_avg_len=options.max_avg_str_len, sample_rows=options.avg_str_len_sample_rows,
                )
            except Exception as e:
                self.logger.warning(
                    f"avg string length 계산 실패 - 제외 없이 진행 [{system_id}.{db_name}.{table_name}] {e}"
                )

            query = self._build_query_with_exclusions(
                catalog_name=catalog_name, db_name=db_name, table_name=table_name,
                limit_rows=options.limit_rows, excluded_columns=excluded_cols,
            )
            title = f"{system_id}.{db_name}.{table_name}"
            try:
                df = self.replace_null(fetch_as_dataframe(self.trino_connection, query))
                if len(df) == 0 and query.strip().lower().startswith("select null where 1=0"):
                    self.logger.warning(f"\t 모든 컬럼이 제외되어 스킵 [{title}]")
                    continue

                report = ProfileReport(df, title=title, minimal=True, explorative=options.explorative)
                profile_dict = json.loads(report.to_json())
                self.upsert_profile_table(meta_data, db_name, profile_dict)
                self.upsert_profile_columns(meta_data, db_name, table_name, profile_dict)

                if options.preview:
                    self._preview_top_k = options.preview_top_k
                    self._render_preview(title, df, report)

                if options.generate_report and output_dir is not None:
                    report_path = os.path.join(output_dir, f"{meta_data.meta_id}.html")
                    report.to_file(report_path)
                    if options.report_language == "ko":
                        self._localize_report_html_to_korean(report_path)

                self.logger.info(f"\t 프로파일링 완료 [{title}] rows={len(df)}")
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

