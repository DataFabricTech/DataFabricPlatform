create table storage_service_entity (
    `id`         varchar(36)  primary key,
    name         varchar(256),
    kind         varchar(128),
    service_type varchar(256),
    json         json         not null,
    updated_at   datetime(3),
    updated_by   varchar(256),
    deleted      tinyint(1),
    constraint unique_name
        unique (name)
);

create index storage_service_entity_name_index on storage_service_entity (name);
create index index_storage_service_entity_deleted on storage_service_entity (id, deleted);

create table classification
(
    id        varchar(36)   primary key,
    name      varchar(256),
    json      json          not     null,
    updatedAt datetime(3),
    updatedBy varchar(256),
    deleted   tinyint(1),
    constraint unique_name
        unique (name)
);

create index classification_entity_name_index on classification (name);
create index index_classification_deleted on classification (`id`, deleted);


create table tag (
    `id`              varchar(36)   primary key,
    classification_id varchar(36)   not null,
    name              varchar(256),
    json              json          not null,
    updatedAt         datetime(3),
    updatedBy         varchar(256),
    deleted           tinyint(1),
    constraint unique_name
        unique (classification_id, name)
);

create index index_tag_deleted on tag (`id`, deleted);