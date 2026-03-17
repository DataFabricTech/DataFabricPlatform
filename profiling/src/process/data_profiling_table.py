from __future__ import annotations

import os
from dataclasses import dataclass
from datetime import datetime

from profiling.process.profiling_common import FilterOption, ProfilingCommon
from profiling.utils.logger import get_logger

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


class DataProfilingTable(ProfilingCommon):
    logger = get_logger()

    def _ensure_output_dir(self, output_dir: str) -> str:
        os.makedirs(output_dir, exist_ok=True)
        return output_dir

    def _build_query(self, catalog_name: str, db_name: str, table_name: str, limit_rows: int) -> str:
        limit_rows = max(int(limit_rows), 1)
        return f'SELECT * FROM "{catalog_name}"."{db_name}"."{table_name}" LIMIT {limit_rows}'

    def profiling(self, system_id: str, filter_include, filter_exclude, options: DataProfilingOptions):
        start_time = datetime.now()
        self.logger.info(f"\t data-profiling 시작 [{system_id}]")

        output_dir = self._ensure_output_dir(options.output_dir)

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

            query = self._build_query(catalog_name, db_name, table_name, options.limit_rows)

            try:
                df = fetch_as_dataframe(self.trino_connection, query)
                report = ProfileReport(
                    df,
                    title=f"{system_id}.{db_name}.{table_name}",
                    explorative=options.explorative,
                )

                base = _safe_filename(meta_data.qualified_name or f"{system_id}.{db_name}.{table_name}")
                out_path = os.path.join(output_dir, f"{base}.html")
                report.to_file(out_path)

                self.logger.info(
                    f"\t 리포트 생성 완료 [{system_id}.{db_name}.{table_name}] rows={len(df)} file={out_path}"
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

