create table if not exists ingestion
(
    ingestion_id           uuid         not null
        primary key,
    deleted                boolean      not null,
    ingestion_display_name varchar(255) not null,
    ingestion_name         varchar(255) not null,
    service_fqn            varchar(255),
    service_id             uuid,
    type                   varchar(255) not null,
    updated_at             bigint
);

create table if not exists ingestion_history
(
    event_at     bigint not null,
    ingestion_id uuid   not null
        constraint fkk3ctujud584a8j7gqtm4criwn
            references ingestion,
    event        varchar(255),
    run_id       uuid,
    state        varchar(255),
    primary key (event_at, ingestion_id)
);

create table if not exists metadata
(
    metadata_name  varchar(255) not null
        primary key,
    metadata_value varchar(255)
);

create table if not exists model_registration
(
    service_id     uuid   not null
        primary key,
    model_count    integer,
    om_model_count integer,
    updated_at     bigint not null
);

create table if not exists services
(
    service_id           uuid                 not null
        primary key,
    connection_status    varchar(255),
    created_at           bigint               not null,
    deleted              boolean              not null,
    service_display_name varchar(255),
    service_name         varchar(255)         not null,
    owner_name           varchar(255),
    service_type         varchar(255)         not null,
    updated_at           bigint               not null,
    monitoring_period    integer default 30   not null,
    monitoring           boolean default true not null
);

create table if not exists connection
(
    execute_at           bigint       not null,
    execute_by           varchar(255) not null,
    query_execution_time bigint,
    service_id           uuid
        constraint fknrfvajve2r9axnuy1nfghqeye
            references services,
    connection_id        uuid         not null
        constraint connection_pk
            primary key
);

create table if not exists connection_history
(
    service_id            uuid   not null
        constraint fk49311oo05sf6lcaqogkrb8nwo
            references services,
    updated_at            bigint not null,
    connection_status     varchar(255),
    connection_history_id uuid   not null
        constraint connection_history_pk
            primary key
);

create table if not exists monitoring_history
(
    id              uuid        not null
        constraint monitoring_history_pk
            primary key,
    service_id      varchar(64),
    owner_name      varchar(20) not null,
    created_at      bigint      not null,
    cpu_used        double precision,
    memory_used     double precision,
    success_request integer,
    failed_request  integer
);

create index if not exists monitoring_history_service_id_index
    on monitoring_history (service_id);

create table if not exists slow_query_statistic
(
    id                    uuid             not null
        constraint slow_query_statistic_pk
            primary key,
    service_id            uuid             not null,
    query                 text             not null,
    total_count           integer          not null,
    average_executed_time double precision not null,
    created_at            bigint           not null
);

comment on table slow_query_statistic is 'slow query 통계';

comment on column slow_query_statistic.total_count is '쿼리 개수';

create index if not exists slow_query_statistic_service_id_index
    on slow_query_statistic (service_id);


