create table storage_service_entity
(
    id              varchar(36)         as (json_unquote(json_extract(`json`, _utf8mb4'$.id'))) stored primary key,
    name            varchar(256)        as (json_unquote(json_extract(`json`, _utf8mb4'$.name'))),
    kind            varchar(256)        as (json_unquote(json_extract(`json`, _utf8mb4'$.kindOfService'))),
    service_type    varchar(256)        as (json_unquote(json_extract(`json`, _utf8mb4'$.serviceType'))),
    json            json                not null,
    updated_at      bigint unsigned     as (json_unquote(json_extract(`json`, _utf8mb4'$.updatedAt'))),
    updated_by      varchar(256)        as (json_unquote(json_extract(`json`, _utf8mb4'$.updatedBy'))),
    deleted         tinyint(1)          as (json_extract(`json`, _utf8mb4'$.deleted'))
);

create index storage_service_entity_name_index on storage_service_entity (name);

create index index_storage_service_entity_deleted on storage_service_entity (id, deleted);

