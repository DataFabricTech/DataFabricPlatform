from typing import Dict, List, Optional

from sqlalchemy import Text, ForeignKey
from sqlalchemy.dialects.postgresql import ARRAY, JSONB
from sqlalchemy.orm import Mapped, mapped_column

from datetime import datetime
from decimal import Decimal

from DataProfiling.models.base import SQLModel


class MetaData(SQLModel):
    __tablename__ = "tb_meta_data"

    meta_id: Mapped[Optional[str]] = mapped_column(primary_key=True)
    qualified_name: Mapped[str]
    system_type: Mapped[str]
    system_id: Mapped[str]
    meta_type: Mapped[Optional[str]]
    meta_name: Mapped[str]
    display_name: Mapped[Optional[str]]
    description: Mapped[Optional[str]]
    user_description: Mapped[Optional[str]]
    classifications: Mapped[Optional[List[str]]] = mapped_column(ARRAY(Text))
    attributes: Mapped[str]
    business_attributes: Mapped[str]
    labels: Mapped[Optional[List[str]]] = mapped_column(ARRAY(Text))
    tags: Mapped[Optional[List[str]]] = mapped_column(ARRAY(Text))
    status: Mapped[Optional[str]]
    life_cycle: Mapped[Optional[int]]
    create_time: Mapped[Optional[datetime]]
    create_by: Mapped[Optional[str]]
    update_time: Mapped[Optional[datetime]]
    update_by: Mapped[Optional[str]]
    last_access_time: Mapped[Optional[datetime]]
    version: Mapped[Optional[float]]
    meta_sync_time: Mapped[Optional[datetime]]
    release: Mapped[Optional[int]]


class MetaSystemInfo(SQLModel):
    __tablename__ = "tb_meta_system_info"

    system_id: Mapped[str] = mapped_column(primary_key=True)
    system_type: Mapped[Optional[str]]
    system_name: Mapped[Optional[str]]
    collect_flag: Mapped[Optional[bool]]
    host: Mapped[Optional[str]]
    port: Mapped[Optional[int]]
    login: Mapped[Optional[str]]
    password: Mapped[Optional[str]]
    database: Mapped[Optional[str]]
    filter_config: Mapped[Dict] = mapped_column(JSONB)
    interval: Mapped[Optional[str]]
    create_date: Mapped[Optional[datetime]]
    create_user: Mapped[Optional[str]]
    update_date: Mapped[Optional[datetime]]
    update_user: Mapped[Optional[datetime]]
    meta_sync_time: Mapped[Optional[datetime]]

class ProfileTable(SQLModel):
    __tablename__ = "tb_profile_table_v2"

    meta_id: Mapped[str] = mapped_column(primary_key=True)
    name: Mapped[Optional[str]]
    meta_type: Mapped[Optional[str]]
    qualified_name: Mapped[Optional[str]]
    status: Mapped[Optional[str]]
    db_name: Mapped[Optional[str]]
    update_date: Mapped[Optional[datetime]]
    last_modified_time: Mapped[Optional[str]]
    err_description: Mapped[Optional[str]]
    sample_data_url: Mapped[Optional[str]]
    n_rows: Mapped[Optional[int]]
    n_columns: Mapped[Optional[int]]
    memory_size: Mapped[Optional[int]]
    record_size: Mapped[Optional[Decimal]]
    n_cells_missing: Mapped[Optional[int]]
    n_vars_with_missing: Mapped[Optional[int]]
    n_vars_all_missing: Mapped[Optional[int]]
    p_cells_missing: Mapped[Optional[Decimal]]
    column_types: Mapped[Optional[Dict]] = mapped_column(JSONB)


class ProfileColumn(SQLModel):
    __tablename__ = "tb_profile_column_v2"

    column_id: Mapped[str] = mapped_column(primary_key=True)
    name: Mapped[Optional[str]]
    meta_type: Mapped[Optional[str]]
    data_type: Mapped[Optional[str]]
    qualified_name: Mapped[Optional[str]]
    status: Mapped[Optional[str]]
    db_name: Mapped[Optional[str]]
    table_name: Mapped[Optional[str]]
    update_date: Mapped[Optional[datetime]]
    err_description: Mapped[Optional[str]]
    hashable: Mapped[Optional[bool]]
    ordering: Mapped[Optional[bool]]
    n_missing: Mapped[Optional[int]]
    n: Mapped[Optional[int]]
    p_missing: Mapped[Optional[Decimal]]
    count: Mapped[Optional[int]]
    memory_size: Mapped[Optional[int]]
    n_distinct: Mapped[Optional[int]]
    p_distinct: Mapped[Optional[Decimal]]
    is_unique: Mapped[Optional[bool]]
    n_unique: Mapped[Optional[int]]
    p_unique: Mapped[Optional[Decimal]]
    top_value_counts: Mapped[Optional[Dict]] = mapped_column(JSONB)
    attributes: Mapped[Optional[Dict]] = mapped_column(JSONB)
