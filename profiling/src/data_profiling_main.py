import argparse
import sys

from profiling.configs.database_session import create_session as db_create_session
from profiling.configs.trino_session import create_session as trino_create_session
from profiling.process.profiling_common import FilterOption

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
    parser.add_argument("--limit_rows", type=int, default=10000, help="테이블당 샘플 row 수 (기본: 10000)")
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

    options = DataProfilingOptions(limit_rows=args.limit_rows, output_dir=args.output_dir)

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

