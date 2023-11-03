create table DataStorageType
(
    name text primary key,
    icon bytea
);

create table DefaultConnSchema
(
    storage_type_name text not null,
    "key"             text,
    "type"            text,
    "default"         text,
    required          bool,
    foreign key (storage_type_name) references DataStorageType (name) on delete cascade
);

create table AuthSchema
(
    storage_type_name text not null,
    auth_type         text,
    "key"             text,
    "type"            text,
    required          bool,
    foreign key (storage_type_name) references DataStorageType (name) on delete cascade
);

create table DataStorageAdaptor
(
    id                uuid primary key default gen_random_uuid(),
    storage_type_name text not null,
    name              text,
    version           text,
    path              text,
    driver            text,
    created_by        uuid,
    created_at        timestamp        default current_timestamp,
    updated_by        uuid,
    updated_at        timestamp        default current_timestamp,
    deleted_by        uuid,
    deleted_at        timestamp,
    foreign key (storage_type_name) references DataStorageType (name)
);

create table UrlFormat
(
    adaptor_id uuid not null,
    format     text,
    foreign key (adaptor_id) references DataStorageAdaptor (id) on delete cascade
);

create table AdaptorUsableAuth
(
    adaptor_id uuid not null,
    auth_type  text,
    foreign key (adaptor_id) references DataStorageAdaptor (id) on delete cascade
);

create table ConnectionSchema
(
    adaptor_id uuid not null,
    "key"      text,
    "type"     text,
    "default"  text,
    required   bool,
    foreign key (adaptor_id) references DataStorageAdaptor (id) on delete cascade
);

create table DataStorage
(
    id                           uuid primary key default gen_random_uuid(),
    adaptor_id                   uuid not null,
    name                         text,
    url                          text,
    user_desc                    text,
    total_data                   int,
    regi_data                    int,
    created_by                   uuid,
    created_at                   timestamp        default current_timestamp,
    updated_by                   uuid,
    updated_at                   timestamp        default current_timestamp,
    deleted_by                   uuid,
    deleted_at                   timestamp,
    status                       text,
    last_connection_checked_at   timestamp,
    last_sync_at                 timestamp,

    sync_enable                  bool,
    sync_type                    int,
    sync_week                    int,
    sync_run_time                text,

    monitoring_enable            bool,
    monitoring_protocol          text,
    monitoring_host              text,
    monitoring_port              text,
    monitoring_sql               text,
    monitoring_period            int,
    monitoring_timeout           int,
    monitoring_success_threshold int,
    monitoring_fail_threshold    int,

    auto_add_setting_enable      bool,

    foreign key (adaptor_id) references DataStorageAdaptor (id)
);

create table StorageAutoAddSetting
(
    datastorage_id uuid not null,
    regex          text,
    data_type      text,
    data_format    text,
    min_size       int,
    max_size       int,
    start_date     text,
    end_date       text,
    foreign key (datastorage_id) references DataStorage (id) on delete cascade
);

create table ConnInfo
(
    datastorage_id uuid not null,
    "key"          text,
    "type"         text,
    "value"        text,
    required       bool,
    foreign key (datastorage_id) references DataStorage (id) on delete cascade
);

create table DataStorageMetadata
(
    datastorage_id uuid not null,
    "key"          text,
    "value"        text,
    "is_system"    bool default true,
    foreign key (datastorage_id) references DataStorage (id) on delete cascade
);

create table DataStorageTag
(
    datastorage_id uuid not null,
    user_id        uuid,
    tag            text,
    foreign key (datastorage_id) references DataStorage (id) on delete cascade
);
