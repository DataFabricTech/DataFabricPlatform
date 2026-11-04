import argparse
import ctypes
import os
import sys
from typing import Optional

from src.configs.database_session import create_session as db_create_session
from src.configs.process_config import process_config
from src.configs.trino_session import create_session as trino_create_session
from src.process.profiling_common import FilterOption
from src.utils.logger import get_logger

from src.process.data_profiling_table import (
    DataProfilingOptions,
    DataProfilingTable,
)

_RESERVE_MEMORY_GB = 2.0


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


def available_cores() -> int:
    if hasattr(os, "sched_getaffinity"):
        return len(os.sched_getaffinity(0))
    return os.cpu_count() or 1


def cpu_quota_cores() -> Optional[float]:
    """컨테이너 CPU 한도(cgroup v2, v1)를 코어 수로. 한도가 없으면 None."""
    try:
        with open("/sys/fs/cgroup/cpu.max") as f:
            quota, period = f.read().split()
        return None if quota == "max" else int(quota) / int(period)
    except (OSError, ValueError):
        pass
    try:
        with open("/sys/fs/cgroup/cpu/cpu.cfs_quota_us") as f:
            quota = int(f.read())
        with open("/sys/fs/cgroup/cpu/cpu.cfs_period_us") as f:
            period = int(f.read())
        return None if quota <= 0 else quota / period
    except (OSError, ValueError):
        return None


def available_memory_gb() -> Optional[float]:
    """가용 메모리(GB). 호스트 여유와 컨테이너 한도 안의 여유 중 작은 값, 모르면 None."""
    candidates = []
    try:
        with open("/proc/meminfo") as f:
            for line in f:
                if line.startswith("MemAvailable:"):
                    candidates.append(int(line.split()[1]) / 2**20)
                    break
    except OSError:
        pass

    for limit_path, usage_path in (
        ("/sys/fs/cgroup/memory.max", "/sys/fs/cgroup/memory.current"),
        ("/sys/fs/cgroup/memory/memory.limit_in_bytes", "/sys/fs/cgroup/memory/memory.usage_in_bytes"),
    ):
        try:
            with open(limit_path) as f:
                limit = f.read().strip()
            with open(usage_path) as f:
                usage = int(f.read().strip())
        except (OSError, ValueError):
            continue
        if limit.isdigit() and int(limit) < 2**60:
            candidates.append((int(limit) - usage) / 2**30)
        break

    if not candidates and os.name == "nt":
        class _MemoryStatus(ctypes.Structure):
            _fields_ = [("dwLength", ctypes.c_uint32), ("dwMemoryLoad", ctypes.c_uint32),
                        ("ullTotalPhys", ctypes.c_uint64), ("ullAvailPhys", ctypes.c_uint64),
                        ("ullTotalPageFile", ctypes.c_uint64), ("ullAvailPageFile", ctypes.c_uint64),
                        ("ullTotalVirtual", ctypes.c_uint64), ("ullAvailVirtual", ctypes.c_uint64),
                        ("ullAvailExtendedVirtual", ctypes.c_uint64)]

        status = _MemoryStatus()
        status.dwLength = ctypes.sizeof(status)
        if ctypes.windll.kernel32.GlobalMemoryStatusEx(ctypes.byref(status)):
            candidates.append(status.ullAvailPhys / 2**30)

    return min(candidates) if candidates else None


def worker_memory_gb(limit_rows: int) -> float:
    """워커 하나의 메모리 추정(GB): 기본 0.5GB + 100만 행당 3GB."""
    return 0.5 + 3.0 * limit_rows / 1_000_000


def auto_workers(cpu_ratio: float, limit_rows: int) -> int:
    """코어 기준(컨테이너 CPU 한도, 없으면 코어 수 x cpu_ratio)과 메모리 기준 중 작은 값."""
    quota = cpu_quota_cores()
    if quota is not None:
        by_cores = max(1, int(quota))
        cores_rule = f"컨테이너 CPU 한도 {quota:g}코어"
    else:
        cores = available_cores()
        by_cores = max(1, int(cores * cpu_ratio))
        cores_rule = f"코어 {cores}개 x {cpu_ratio:g}"

    available = available_memory_gb()
    if available is None:
        get_logger().info(f"\t 워커 수 자동 결정: {by_cores}개 ({cores_rule}, 메모리 정보 없음)")
        return by_cores
    per_worker = worker_memory_gb(limit_rows)
    by_memory = max(1, int((available - _RESERVE_MEMORY_GB) / per_worker))
    workers = min(by_cores, by_memory)
    get_logger().info(
        f"\t 워커 수 자동 결정: {workers}개 (코어 기준 {by_cores}: {cores_rule} / 메모리 기준 {by_memory}: "
        f"가용 {available:.1f}GB - 예비 {_RESERVE_MEMORY_GB:.0f}GB, 워커당 {per_worker:.1f}GB 추정)"
    )
    return workers


def main():
    parser = argparse.ArgumentParser(
        description="Trino 테이블 샘플 기반 data-profiling 리포트 생성",
        epilog="* include, exclude 설정 가능한 인자 : database, table",
    )

    conf = process_config.profiling

    parser.add_argument("--system_id", required=True, help="시스템 ID (필수)")
    parser.add_argument("--limit_rows", type=int, default=conf.limit_rows,
                        help=f"테이블당 샘플 row 수 (설정: {conf.limit_rows})")
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
        default=conf.workers,
        help=f"테이블을 동시에 처리할 프로세스 수, 0이면 자동: 코어 수 x cpu_ratio 와 가용 메모리 중 작은 쪽 (설정: {conf.workers})",
    )
    parser.add_argument(
        "--cpu_ratio",
        type=float,
        default=conf.cpu_ratio,
        help=f"--workers 0 일 때 쓸 코어 비율. 컨테이너 CPU 한도(docker cpus)가 있으면 그 한도를 대신 쓴다 (설정: {conf.cpu_ratio})",
    )
    parser.add_argument(
        "--big_workers",
        type=int,
        default=conf.big_workers,
        help=f"지난 실행 행 수 10만 이상인 테이블을 동시에 처리할 최대 프로세스 수 (설정: {conf.big_workers})",
    )
    parser.add_argument(
        "--localize_report",
        action="store_true",
        help="리포트를 바로 한글화해 {meta_id}.html 로 저장 (기본: {meta_id}.html.raw 로 저장하고 별도 한글화 단계에서 처리)",
    )
    parser.add_argument(
        "--incremental",
        action="store_true",
        default=conf.incremental,
        help=f"스키마·행 수가 그대로인 테이블은 건너뛴다 (설정: {conf.incremental})",
    )
    parser.add_argument(
        "--no_incremental",
        dest="incremental",
        action="store_false",
        help="설정이 켜져 있어도 이번 실행은 전체를 처리한다",
    )
    parser.add_argument(
        "--refresh_days",
        type=int,
        default=conf.refresh_days,
        help=f"증분이어도 이 주기마다 한 번은 다시 처리한다 (설정: {conf.refresh_days}일)",
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
        workers=args.workers if args.workers > 0 else auto_workers(args.cpu_ratio, args.limit_rows),
        big_workers=args.big_workers,
        localize_report=bool(args.localize_report),
        incremental=bool(args.incremental),
        refresh_days=max(1, args.refresh_days),
    )

    if args.workers > 1:
        available = available_memory_gb()
        needed = args.workers * worker_memory_gb(args.limit_rows) + _RESERVE_MEMORY_GB
        if available is not None and available < needed:
            get_logger().warning(
                f"\t 워커 {args.workers}개는 메모리 약 {needed:.1f}GB 가 필요한데 가용 메모리가 {available:.1f}GB 입니다. "
                f"--workers 를 줄이거나 --limit_rows 를 낮추세요."
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

