# DataProfiling 프로세스 흐름

## 개요

Trino에 연결된 테이블의 데이터를 샘플링하여 `ydata-profiling`으로 통계 분석하고, 결과를 PostgreSQL에 저장하는 파이프라인.

---

## 전체 흐름

```
[CLI 실행] data_profiling_main.py
    │
    ├─ argparse 인자 파싱
    │       --system_id (필수)
    │       --limit_rows, --generate_report, --no_preview
    │       --max_avg_str_len, --avg_str_len_sample_rows
    │       --output_dir, --include, --exclude
    │
    ├─ DataProfilingOptions 생성
    │
    ├─ PostgreSQL 세션 생성 (SQLAlchemy)
    ├─ Trino 세션 생성 (trino.dbapi)
    │
    └─ DataProfilingTable.profiling() 실행
```

---

## DataProfilingTable.profiling() 상세 흐름

```
profiling(system_id, filter_include, filter_exclude, options)
    │
    ├─ [1] 메타데이터 조회
    │       ProfilingCommon.get_meta_data_by_system_id("table", system_id)
    │       → tb_meta_data 에서 status=ACTIVE, meta_type=table,
    │         qualified_name LIKE '{system_id}%' 조건으로 목록 조회
    │
    └─ [2] 테이블 루프 (meta_data in meta_data_list)
            │
            ├─ get_trino_info(meta_data)
            │       qualified_name 파싱 → catalog_name, db_name, table_name 추출
            │
            ├─ [필터 체크] check_filter()
            │       include/exclude 옵션으로 database, table 단위 필터링
            │       → 필터에 걸리면 skip (profiling_filter_count++)
            │
            ├─ [문자열 컬럼 사전 필터] _get_excluded_string_columns_by_avg_len()
            │       │
            │       ├─ Trino information_schema.columns 조회 → varchar/char/string 컬럼 추출
            │       └─ 샘플 rows (avg_str_len_sample_rows)의 AVG(LENGTH()) 계산
            │           → 평균 길이 >= max_avg_str_len 컬럼은 excluded_cols에 추가
            │           (OOM 방지 목적)
            │
            ├─ [쿼리 빌드] _build_query_with_exclusions()
            │       excluded_cols가 있으면 해당 컬럼 제외한 SELECT 쿼리 생성
            │       없으면 SELECT * 쿼리
            │
            ├─ [데이터 수집] fetch_as_dataframe()
            │       Trino cursor.execute(query) → fetchall() → pandas.DataFrame
            │
            ├─ replace_null(df)
            │       "", " ", "-", "NA", "N/A", "NULL", "null" → np.nan 치환
            │
            ├─ ProfileReport 생성 (ydata-profiling)
            │       minimal=True, explorative=options.explorative
            │
            ├─ report.to_json() → profile_dict 변환
            │
            ├─ [DB 저장] PostgreSQL Upsert
            │       ├─ upsert_profile_table()
            │       │       → tb_profile_table_v2 (테이블 수준 통계)
            │       │         n_rows, n_columns, memory_size, p_cells_missing 등
            │       │
            │       └─ upsert_profile_columns()
            │               → tb_profile_column_v2 (컬럼 수준 통계)
            │                 각 컬럼별 n_missing, p_missing, n_distinct, top_value_counts 등
            │                 * value_counts_without_nan, value_counts_index_sorted, word_counts 는 용량 이슈로 제거
            │                 * top_value_counts: 상위 5개 값만 저장
            │
            ├─ [선택] _render_preview()
            │       콘솔에 결측치 비율 상위 컬럼 요약 출력 (--no_preview 미사용 시)
            │
            └─ [선택] report.to_file()
                    HTML + JSON 파일 저장 (--generate_report 사용 시)
                    경로: {output_dir}/html/{qualified_name}.html
                         {output_dir}/json/{qualified_name}.json
```

---

## 세션 및 연결

| 세션 | 모듈 | 대상 | 비고 |
|------|------|------|------|
| PostgreSQL | `configs/database_session.py` | `tb_meta_data`, `tb_profile_table_v2`, `tb_profile_column_v2` | SQLAlchemy Session |
| Trino | `configs/trino_session.py` | 원천 데이터 | `trino.dbapi` cursor 반환 |

---

## DB 테이블

| 테이블 | 역할 | 키 |
|--------|------|-----|
| `tb_meta_data` | 수집 대상 메타데이터 (입력) | `meta_id` |
| `tb_meta_system_info` | 시스템 연결 정보 | `system_id` |
| `tb_profile_table_v2` | 테이블 단위 프로파일링 결과 | `meta_id` |
| `tb_profile_column_v2` | 컬럼 단위 프로파일링 결과 | `column_id` (= qualified_name) |

---

## 필터 동작 방식

```
filter_include = {"database": ["public"], "table": ["users"]}
filter_exclude = {"table": ["log_table"]}
```

- `include` 지정 시: 해당 key-value에 매칭되는 항목만 처리
- `exclude` 지정 시: 해당 key-value에 매칭되는 항목은 스킵
- 필터 기준: `database`, `table` (FilterOption enum)

---

## 클래스 계층

```
DataBaseManager (SessionMixin)
    └─ ProfilingCommon
            └─ DataProfilingTable
```

| 클래스 | 파일 | 역할 |
|--------|------|------|
| `SessionMixin` | `utils/database_manager.py` | db_session, trino_connection 보유 |
| `DataBaseManager` | `utils/database_manager.py` | SQLAlchemy/Trino 공통 CRUD |
| `ProfilingCommon` | `process/profiling_common.py` | 메타데이터 조회, 필터, upsert, Trino 정보 파싱 |
| `DataProfilingTable` | `process/data_profiling_table.py` | 프로파일링 핵심 로직 |

---

## 주요 옵션 (DataProfilingOptions)

| 옵션 | 기본값 | 설명 |
|------|--------|------|
| `limit_rows` | 1,000,000 | 테이블당 샘플 row 수 |
| `max_avg_str_len` | 5,000 | 문자열 컬럼 평균 길이 제한 (초과 시 제외) |
| `avg_str_len_sample_rows` | 200 | 평균 길이 계산용 샘플 row 수 |
| `generate_report` | False | HTML/JSON 파일 저장 여부 |
| `preview` | True | 콘솔 요약 출력 여부 |
| `preview_top_k` | 20 | 콘솔 출력 컬럼 수 |
| `output_dir` | `reports/data_profiling` | 리포트 저장 경로 |
