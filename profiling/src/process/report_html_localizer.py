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

_UNICODE_SCRIPT_KO_LABELS = {
    "Common": "공통 문자 체계",
}

_REPORT_KO_LABELS = {
    **_UNICODE_GENERAL_CATEGORY_KO_LABELS, **_UNICODE_BLOCK_KO_LABELS, **_UNICODE_SCRIPT_KO_LABELS,
    "Toggle navigation": "탐색 메뉴 열기/닫기", "Overview": "개요", "Variables": "변수",
    "Alerts": "알림", "Reproduction": "재현 정보", "Correlations": "상관관계",
    "Interactions": "상호작용", "Missing values": "결측값", "Columns": "컬럼", "Table": "표",
    "Heatmap": "히트맵", "Scatter plot": "산점도", "Scatter": "산점도",
    "Dataset statistics": "데이터셋 통계", "Variable types": "변수 유형",
    "Number of variables": "변수 수", "Number of observations": "관측치 수",
    "Missing cells (%)": "결측 셀 비율(%)", "Missing cells": "결측 셀 수",
    "Duplicate rows (%)": "중복 행 비율(%)", "Duplicate rows": "중복 행 수",
    "Total size in memory": "메모리 내 전체 크기",
    "Average record size in memory": "평균 레코드 메모리 크기",
    "Analysis started": "분석 시작 시각", "Analysis finished": "분석 종료 시각",
    "Duration": "소요 시간", "Software version": "소프트웨어 버전",
    "Download configuration": "설정 내려받기", "Select Columns": "컬럼 선택",
    "Statistics": "통계", "Histogram(s)": "히스토그램", "Histogram": "히스토그램",
    "Histogram with fixed size bins": "고정 크기 구간 히스토그램",
    "Common Values (Table)": "빈도 높은 값(표)", "Common Values (Plot)": "빈도 높은 값(그래프)",
    "Common values": "빈도 높은 값", "Distinct (%)": "고유값 비율(%)", "Distinct": "고유값 수",
    "Missing (%)": "결측 비율(%)", "Missing": "결측 수", "Unique (%)": "유일값 비율(%)",
    "Unique": "유일값 수", "Mean": "평균", "Median": "중앙값", "Minimum": "최솟값",
    "Maximum": "최댓값", "Standard deviation": "표준편차", "Sum": "합계", "Variance": "분산",
    "Memory size": "메모리 크기", "Infinite (%)": "무한값 비율(%)", "Infinite": "무한값 수",
    "Zeros (%)": "0값 비율(%)", "Zeros": "0값 수", "Negative (%)": "음수 비율(%)",
    "Negative": "음수 수", "Extreme values": "극값", "Quantile statistics": "분위수 통계",
    "Descriptive statistics": "기술 통계", "5-th percentile": "5백분위수", "95-th percentile": "95백분위수",
    "Q1": "제1사분위수", "median": "중앙값", "Q3": "제3사분위수",
    "Interquartile range (IQR)": "사분위 범위(IQR)", "Range": "범위",
    "Coefficient of variation (CV)": "변동계수(CV)", "Kurtosis": "첨도",
    "Median Absolute Deviation (MAD)": "중앙 절대 편차(MAD)", "Skewness": "왜도",
    "Monotonicity": "단조성", "Strictly increasing": "엄격 증가", "Increasing": "증가",
    "Not monotonic": "비단조", "Decreasing": "감소", "Strictly decreasing": "엄격 감소",
    "Minimum 5 values": "최솟값 5개", "Maximum 5 values": "최댓값 5개",
    "Value": "값", "Count": "개수", "Frequency (%)": "빈도(%)", "Words": "단어 수",
    "Characters and Unicode": "문자 및 유니코드", "Unicode": "유니코드",
    "Unicode categories (click for more information)": "유니코드 범주(자세한 정보는 클릭)",
    "Unicode scripts (click for more information)": "유니코드 문자 체계(자세한 정보는 클릭)",
    "Unicode blocks (click for more information)": "유니코드 블록(자세한 정보는 클릭)",
    "Characters": "문자 수", "Length": "길이", "Categories": "범주", "Scripts": "문자 체계",
    "Blocks": "블록", "Numeric": "수치형", "Text": "텍스트", "Boolean": "불리언", "Date": "날짜",
    "DateTime": "날짜/시간", "Categorical": "범주형", "Unsupported": "지원되지 않는 유형", "Sample": "표본",
    "Max length": "최대 길이", "Median length": "중앙 길이", "Mean length": "평균 길이",
    "Min length": "최소 길이", "Total characters": "전체 문자 수",
    "Distinct characters": "고유 문자 수", "Distinct categories": "고유 범주 수",
    "Distinct scripts": "고유 문자 체계 수", "Distinct blocks": "고유 블록 수",
    "Most occurring characters": "가장 자주 등장한 문자", "Most occurring categories": "가장 자주 등장한 범주",
    "Most occurring scripts": "가장 자주 등장한 문자 체계", "Most occurring blocks": "가장 자주 등장한 블록",
    "Most frequent character per category": "범주별 최빈 문자",
    "Most frequent character per script": "문자 체계별 최빈 문자",
    "Most frequent character per block": "블록별 최빈 문자",
    "The Unicode Standard assigns character properties to each code point, which can be used to analyse textual variables.": "유니코드 표준은 각 코드 포인트에 문자 속성을 부여하며, 이를 통해 텍스트형 변수를 분석할 수 있습니다.",
    "The number of unique values (all values that occur exactly once in the dataset).": "유일값 수(데이터셋에서 정확히 한 번만 나타나는 모든 값의 수)",
    "1st row": "1번째 행", "2nd row": "2번째 행", "3rd row": "3번째 행",
    "4th row": "4번째 행", "5th row": "5번째 행", "More details": "상세 정보",
    "Real number (ℝ)": "실수(ℝ)", "Report generated by": "리포트 생성 도구",
    "CONSTANT": "상수값", "Constant": "상수값", "MISSING": "결측값", "UNIQUE": "유일값",
    "ZEROS": "0값", "REJECTED": "제외됨", "UNSUPPORTED": "지원되지 않음",
    " has unique values": "는 유일값을 가집니다",
    " is an unsupported type, check if it needs cleaning or further analysis": "는 지원되지 않는 유형입니다. 정제 또는 추가 분석이 필요한지 확인하세요",
    " has predominantly zeros": "는 대부분 0값입니다", " has a high cardinality": "는 고유값 수가 많습니다",
    " has infinite values": "에 무한값이 있습니다", " is non stationary": "는 비정상 시계열입니다",
    " is seasonal": "는 계절성을 가집니다", " is uniformly distributed": "는 균등 분포를 따릅니다",
    " was rejected": "는 제외되었습니다",
    "히스토그램 with fixed size bins": "고정 크기 구간 히스토그램",
    "중앙값 Absolute Deviation (MAD)": "중앙 절대 편차(MAD)",
    "최솟값 5 values": "최솟값 5개", "최댓값 5 values": "최댓값 5개",
    "중앙값 length": "중앙 길이", "평균 length": "평균 길이",
    "고유값 수 characters": "고유 문자 수", "고유값 수 categories": "고유 범주 수",
    "고유값 수 scripts": "고유 문자 체계 수", "고유값 수 blocks": "고유 블록 수",
    "날짜Time": "날짜/시간", "Select 컬럼": "컬럼 선택",
    "문자 수 and Unicode": "문자 및 유니코드",
}

_REPLACEMENTS = sorted(_REPORT_KO_LABELS.items(), key=lambda item: len(item[0]), reverse=True)


def _replace_labels(text: str) -> str:
    for english, korean in _REPLACEMENTS:
        text = text.replace(english, korean)
    return text


def _localize_text(text: str) -> str:
    text = re.sub(r'(.+?) has constant value ""', r"\1: 상수값", text)
    text = re.sub(r"(.+?) has (\d[\d,]*) \(([\d.]+%)\) missing values", r"\1: 결측값 \2개(\3)", text)
    text = re.sub(r"(.+?) has (\d[\d,]*) \(([\d.]+%)\) zeros", r"\1: 0값 \2개(\3)", text)
    text = re.sub(r"(.+?) has unique values", r"\1: 유일값", text)
    text = re.sub(r"(.+?) is highly skewed \(γ1 = ([^)]+)\)", r"\1: 왜도가 높음(γ1 = \2)", text)
    return _replace_labels(re.sub(r"Other values \((\d+)\)", r"기타 값 (\1개)", text))


def _localize_script_display_values(script: str) -> str:
    """Translate only known display strings; never apply UI labels to all JS."""
    script = re.sub(r'(?P<column>[^"\r\n]+?) has constant value (?:""|\\"\\")', r"\g<column>: 상수값", script)
    script = re.sub(r"(?P<column>[^\"\r\n]+?) has (?P<count>\d[\d,]*) \((?P<percent>[\d.]+%)\) missing values", r"\g<column>: 결측값 \g<count>개(\g<percent>)", script)
    script = re.sub(r"(?P<column>[^\"\r\n]+?) has (?P<count>\d[\d,]*) \((?P<percent>[\d.]+%)\) zeros", r"\g<column>: 0값 \g<count>개(\g<percent>)", script)
    script = re.sub(r"(?P<column>[^\"\r\n]+?) has unique values", r"\g<column>: 유일값", script)
    script = re.sub(r"(?P<column>[^\"\r\n]+?) has high cardinality", r"\g<column>: 고유값 수가 많음", script)
    script = re.sub(r"(?P<column>[^\"\r\n]+?) has infinite values", r"\g<column>: 무한값 포함", script)
    script = re.sub(r"(?P<column>[^\"\r\n]+?) has predominantly zeros", r"\g<column>: 대부분 0값", script)
    script = re.sub(r"(?P<column>[^\"\r\n]+?) is highly skewed \(γ1 = (?P<skew>[^)]+)\)", r"\g<column>: 왜도가 높음(γ1 = \g<skew>)", script)
    script = re.sub(r"(?P<column>[^\"\r\n]+?) is non stationary", r"\g<column>: 비정상 시계열", script)
    script = re.sub(r"(?P<column>[^\"\r\n]+?) is seasonal", r"\g<column>: 계절성 있음", script)
    script = re.sub(r"(?P<column>[^\"\r\n]+?) is uniformly distributed", r"\g<column>: 균등 분포", script)
    script = re.sub(r"(?P<column>[^\"\r\n]+?) is an unsupported type, check if it needs cleaning or further analysis", r"\g<column>: 지원되지 않는 유형(정제 또는 추가 분석 필요)", script)
    script = re.sub(r"(?P<column>[^\"\r\n]+?) was rejected", r"\g<column>: 제외됨", script)
    script = re.sub(r"Other values \((\d+)\)", r"기타 값 (\1개)", script)
    for english, korean in {
        **_UNICODE_GENERAL_CATEGORY_KO_LABELS,
        **_UNICODE_BLOCK_KO_LABELS,
        **_UNICODE_SCRIPT_KO_LABELS,
    }.items():
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
