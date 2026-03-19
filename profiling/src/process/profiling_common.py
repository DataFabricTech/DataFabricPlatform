from sqlalchemy import select
from sqlalchemy.dialects.postgresql import insert
from typing import List
from enum import Enum
import math

from DataProfiling.utils.database_manager import DataBaseManager
from DataProfiling.models.postgres import MetaSystemInfo, MetaData


class FilterOption(Enum):
    DATABASE = "database"
    TABLE = "table"


class SystemType(Enum):
    Trino = 'Trino'


class ProfilingCommon(DataBaseManager):

    def get_meta_system_info(self) -> List[MetaSystemInfo]:

        return self.get_all(
            select(MetaSystemInfo)
            .where(MetaSystemInfo.collect_flag == True)
        )

    def get_meta_data_by_system_id(self, meta_type, system_id) -> List[MetaData]:

        return self.get_all(
            select(MetaData)
            .where(MetaData.status == "ACTIVE",
                   MetaData.meta_type == meta_type,
                   MetaData.qualified_name.like(f"{system_id}%"))
        )

    def upsert_data(self, update_table, update_data: dict, update_key: str):

        stmt = insert(update_table).values(update_data)

        del update_data[update_key]

        stmt = stmt.on_conflict_do_update(
            index_elements=[update_key],
            set_=update_data
        )
        try:
            self.update_commit(stmt)
        except Exception as e:
            self.db_session.rollback()
            raise e

    def check_filter(self, filter_include, filter_exclude, filter_key, name):
        if not filter_include and not filter_exclude:
            return False  # 필터가 없으면 -> 프로세스 진행

        if filter_include is not None and filter_key in filter_include:
            if name in filter_include[filter_key]:
                return False  # include 매칭 -> 프로세스 진행
            else:
                return True  # 매칭 안됨

        if filter_exclude is not None and filter_key in filter_exclude:
            if name in filter_exclude[filter_key]:
                return True  # exclude 매칭
            else:
                return False  # exclude에 매칭 안됨 -> 프로세스 진행

        return False

    def get_trino_info(self, meta_data: MetaData):

        qualified_name_arr = meta_data.qualified_name.split(".")
        schema_name = qualified_name_arr[1]  # schema
        db_name = qualified_name_arr[2]  # database
        table_name = meta_data.meta_name

        if meta_data.system_type == SystemType.Trino.value:
            catalog_name = schema_name
        else:
            catalog_name = meta_data.system_id

        return catalog_name, db_name, table_name
    
    def replace_nan(self, obj):
        if isinstance(obj, float):
            if math.isnan(obj) or math.isinf(obj):
                return None
            return obj
        elif isinstance(obj, dict):
            return {k: self.replace_nan(v) for k, v in obj.items()}
        elif isinstance(obj, list):
            return [self.replace_nan(v) for v in obj]
        else:
            return obj
