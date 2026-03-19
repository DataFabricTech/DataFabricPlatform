import argparse
import sys

from DataProfiling.configs.database_session import create_session as db_create_session
from DataProfiling.configs.trino_session import create_session as trino_create_session
from DataProfiling.process.profiling_common import FilterOption

from DataProfiling.process.data_profiling_table import (
    DataProfilingOptions,
    DataProfilingTable,
)


def parse_key_value_list(kv_list):
    result = {}
    for item in kv_list or []:
        if "=" not in item:
            raise ValueError(f"인자 '{item}'은 'key=value' 형식이어야 합니다.")
        key, value = item.split("=", 1)
        if key not in (option.value for option in FilterOption):
            raise ValueError(f"'{key}'는 허용되지 않는 인자입니다.")

        if key in result:
            result[key].append(value)
        else:
            result[key] = [value]

    return result if len(result) > 0 else None


def main():
    parser = argparse.ArgumentParser(
        description="Trino 테이블 샘플 기반 data-profiling 리포트 생성",
        epilog="* include, exclude 설정 가능한 인자 : database, table",
    )

    parser.add_argument("--system_id", required=True, help="시스템 ID (필수)")
    parser.add_argument("--limit_rows", type=int, default=1000000, help="테이블당 샘플 row 수 (기본: 1000000)")
    parser.add_argument(
        "--generate_report",
        action="store_true",
        help="HTML 리포트 파일 생성 (기본: 생성 안 함)",
    )
    parser.add_argument(
        "--no_preview",
        action="store_true",
        help="콘솔 요약 출력 비활성화 (기본: 요약 출력)",
    )
    parser.add_argument(
        "--preview_top_k",
        type=int,
        default=20,
        help="요약 출력 시 변수(top) 개수 (기본: 20)",
    )
    parser.add_argument(
        "--max_avg_str_len",
        type=int,
        default=5000,
        help="문자열 컬럼 평균 길이 기준(이상은 제외) (기본: 5000)",
    )
    parser.add_argument(
        "--avg_str_len_sample_rows",
        type=int,
        default=200,
        help="평균 문자열 길이 계산 시 샘플 row 수 (기본: 200)",
    )
    parser.add_argument(
        "--output_dir",
        default="reports/data_profiling",
        help="리포트 저장 디렉토리 (기본: reports/data_profiling)",
    )
    parser.add_argument("--include", nargs="*", help="포함할 인자들 (예: database=public table=users)")
    parser.add_argument("--exclude", nargs="*", help="제외할 인자들 (예: table=data1 table=data2)")

    args = parser.parse_args()

    try:
        filter_include = parse_key_value_list(args.include)
        filter_exclude = parse_key_value_list(args.exclude)
    except ValueError as e:
        print(e)
        parser.print_help()
        sys.exit(1)

    options = DataProfilingOptions(
        limit_rows=args.limit_rows,
        output_dir=args.output_dir,
        generate_report=bool(args.generate_report),
        preview=not bool(args.no_preview),
        preview_top_k=args.preview_top_k,
        max_avg_str_len=args.max_avg_str_len,
        avg_str_len_sample_rows=args.avg_str_len_sample_rows,
    )

    try:
        with db_create_session() as db_session:
            with trino_create_session() as trino_session:
                dp = DataProfilingTable(db_session, trino_session)
                dp.profiling(args.system_id, filter_include, filter_exclude, options)

    except Exception as e:
        print("error")
        print(e)
        sys.exit(1)


if __name__ == "__main__":
    main()

