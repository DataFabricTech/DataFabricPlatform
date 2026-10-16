import argparse
import os
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


def auto_workers(reserve_cores: int) -> int:
    """이 프로세스가 쓸 수 있는 코어 수에서 reserve_cores 개를 다른 작업용으로 남긴 수(최소 1)."""
    if hasattr(os, "sched_getaffinity"):  # 리눅스: 컨테이너 cpuset 제한을 반영
        cores = len(os.sched_getaffinity(0))
    else:
        cores = os.cpu_count() or 1
    return max(1, cores - reserve_cores)


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
    parser.add_argument(
        "--workers",
        type=int,
        default=int(os.getenv("PROFILING_WORKERS", "0")),
        help="테이블을 동시에 처리할 프로세스 수, 0이면 CPU 코어 수 - reserve_cores (기본: 환경변수 PROFILING_WORKERS, 없으면 0)",
    )
    parser.add_argument(
        "--reserve_cores",
        type=int,
        default=2,
        help="--workers 0 일 때 다른 작업용으로 남겨 둘 코어 수 (기본: 2)",
    )
    parser.add_argument(
        "--big_workers",
        type=int,
        default=int(os.getenv("PROFILING_BIG_WORKERS", "1")),
        help="지난 실행 행 수 10만 이상인 테이블을 동시에 처리할 최대 프로세스 수 (기본: 환경변수 PROFILING_BIG_WORKERS, 없으면 1)",
    )
    parser.add_argument(
        "--localize_report",
        action="store_true",
        help="리포트를 바로 한글화해 {meta_id}.html 로 저장 (기본: {meta_id}.html.raw 로 저장하고 별도 한글화 단계에서 처리)",
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
        workers=args.workers if args.workers > 0 else auto_workers(args.reserve_cores),
        big_workers=args.big_workers,
        localize_report=bool(args.localize_report),
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

