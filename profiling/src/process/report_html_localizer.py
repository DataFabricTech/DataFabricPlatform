"""Safe, presentation-only localization for ydata-profiling HTML reports.

The report contains user data, JavaScript, and embedded ``data:`` images.  This
module intentionally limits replacements to HTML text/selected attributes and
the small set of quoted JavaScript display values described below.
"""

from __future__ import annotations

import re
from pathlib import Path


_UNICODE_GENERAL_CATEGORY_KO_LABELS = {
    "Uppercase Letter": "대문자", "Lowercase Letter": "소문자",
    "Titlecase Letter": "제목형 대문자", "Modifier Letter": "수식 문자",
    "Other Letter": "기타 문자", "Nonspacing Mark": "비공백 결합 기호",
    "Spacing Combining Mark": "간격 결합 기호", "Enclosing Mark": "둘러싸는 결합 기호",
    "Decimal Number": "10진 숫자", "Letter Number": "문자 숫자", "Other Number": "기타 숫자",
    "Connector Punctuation": "연결 구두점", "Dash Punctuation": "대시 구두점",
    "Open Punctuation": "여는 구두점", "Close Punctuation": "닫는 구두점",
    "Initial Punctuation": "여는 인용 구두점", "Final Punctuation": "닫는 인용 구두점",
    "Other Punctuation": "기타 구두점", "Math Symbol": "수학 기호",
    "Currency Symbol": "통화 기호", "Modifier Symbol": "수식 기호", "Other Symbol": "기타 기호",
    "Space Separator": "공백 구분자", "Line Separator": "줄 구분자",
    "Paragraph Separator": "문단 구분자", "Control": "제어 문자", "Format": "형식 문자",
    "Surrogate": "대리 문자", "Private Use": "사용자 정의 영역", "Unassigned": "미할당 문자",
}

_UNICODE_BLOCK_KO_LABELS = {
    "ASCII": "ASCII 기본 라틴", "Basic Latin": "기본 라틴", "None": "없음",
    "Punctuation": "일반 문장부호", "Geometric Shapes": "기하 도형", "Math Operators": "수학 연산자",
    "CJK": "한중일 통합 한자", "CJK Compat": "한중일 호환 문자", "CJK Compatibility": "한중일 호환 문자",
    "Compat Jamo": "호환 자모", "Hangul Compatibility Jamo": "한글 호환 자모",
    "Misc Symbols": "기타 기호", "Miscellaneous Symbols": "기타 기호", "Hangul": "한글",
}

_REPORT_KO_LABELS = {
    **_UNICODE_GENERAL_CATEGORY_KO_LABELS, **_UNICODE_BLOCK_KO_LABELS,
    "Toggle navigation": "탐색 메뉴 열기/닫기", "Overview": "개요", "Variables": "변수",
    "Alerts": "알림", "Reproduction": "재현 정보", "Correlations": "상관관계",
    "Interactions": "상호작용", "Missing values": "결측값", "Columns": "컬럼", "Table": "표",
    "Heatmap": "히트맵", "Scatter plot": "산점도", "Scatter": "산점도",
    "Dataset statistics": "데이터셋 통계", "Variable types": "변수 유형",
    "Number of variables": "변수 수", "Number of observations": "관측치 수",
    "Missing cells (%)": "결측 셀 비율(%)", "Missing cells": "결측 셀 수",
    "Duplicate rows (%)": "중복 행 비율(%)", "Duplicate rows": "중복 행 수",
    "Statistics": "통계", "Histogram(s)": "히스토그램", "Histogram": "히스토그램",
    "Common Values (Table)": "빈도 높은 값(표)", "Common Values (Plot)": "빈도 높은 값(그래프)",
    "Common values": "빈도 높은 값", "Distinct (%)": "고유값 비율(%)", "Distinct": "고유값 수",
    "Missing (%)": "결측 비율(%)", "Missing": "결측 수", "Unique (%)": "유일값 비율(%)",
    "Unique": "유일값 수", "Mean": "평균", "Median": "중앙값", "Minimum": "최솟값",
    "Maximum": "최댓값", "Standard deviation": "표준편차", "Sum": "합계", "Variance": "분산",
    "Value": "값", "Count": "개수", "Frequency (%)": "빈도(%)", "Words": "단어 수",
    "Characters and Unicode": "문자 및 유니코드", "Unicode": "유니코드",
    "Unicode categories (click for more information)": "유니코드 범주(자세한 정보는 클릭)",
    "Unicode scripts (click for more information)": "유니코드 문자 체계(자세한 정보는 클릭)",
    "Unicode blocks (click for more information)": "유니코드 블록(자세한 정보는 클릭)",
    "Characters": "문자 수", "Length": "길이", "Categories": "범주", "Scripts": "문자 체계",
    "Blocks": "블록", "Numeric": "수치형", "Text": "텍스트", "Boolean": "불리언", "Date": "날짜",
    "Categorical": "범주형", "Unsupported": "지원되지 않는 유형", "Sample": "표본",
}

_REPLACEMENTS = sorted(_REPORT_KO_LABELS.items(), key=lambda item: len(item[0]), reverse=True)


def _replace_labels(text: str) -> str:
    for english, korean in _REPLACEMENTS:
        text = text.replace(english, korean)
    return text


def _localize_text(text: str) -> str:
    text = re.sub(r'(.+?) has constant value ""', r"\1: 상수값", text)
    text = re.sub(r"(.+?) has (\d[\d,]*) \(([\d.]+%)\) missing values", r"\1: 결측값 \2개(\3)", text)
    return _replace_labels(re.sub(r"Other values \((\d+)\)", r"기타 값 (\1개)", text))


def _localize_script_display_values(script: str) -> str:
    """Translate only known display strings; never apply UI labels to all JS."""
    script = re.sub(r'(?P<column>[^"\r\n]+?) has constant value (?:""|\\"\\")', r"\g<column>: 상수값", script)
    script = re.sub(r"(?P<column>[^\"\r\n]+?) has (?P<count>\d[\d,]*) \((?P<percent>[\d.]+%)\) missing values", r"\g<column>: 결측값 \g<count>개(\g<percent>)", script)
    script = re.sub(r"Other values \((\d+)\)", r"기타 값 (\1개)", script)
    for english, korean in {**_UNICODE_GENERAL_CATEGORY_KO_LABELS, **_UNICODE_BLOCK_KO_LABELS}.items():
        script = re.sub(rf'(?P<quote>["\']){re.escape(english)}(?P=quote)', lambda m: f"{m.group('quote')}{korean}{m.group('quote')}", script)
    return script


def localize_report_html_to_korean(report_path: str | Path) -> None:
    """Localize a report in place. Calling this repeatedly is safe."""
    path = Path(report_path)
    html = path.read_text(encoding="utf-8")
    blocks = re.split(r"(<(?:script|style)\b[^>]*>.*?</(?:script|style)\s*>)", html, flags=re.IGNORECASE | re.DOTALL)
    for index, block in enumerate(blocks):
        if index % 2 == 0:
            block = re.sub(r"(?<=>)[^<]+(?=<)", lambda m: _localize_text(m.group(0)), block)
            blocks[index] = re.sub(
                r"(?P<attribute>\b(?:title|aria-label)\s*=\s*)(?P<quote>[\"'])(?P<value>.*?)(?P=quote)",
                lambda m: f"{m.group('attribute')}{m.group('quote')}{_replace_labels(m.group('value'))}{m.group('quote')}",
                block, flags=re.IGNORECASE,
            )
        elif re.match(r"<script\b", block, flags=re.IGNORECASE):
            blocks[index] = _localize_script_display_values(block)
    path.write_text("".join(blocks), encoding="utf-8")
