create table storage_service_entity (
    `id`         varchar(36)  primary key,
    name         varchar(256),
    kind         varchar(128),
    service_type varchar(256),
    json         json         not null,
    updated_at   datetime(3),
    updated_by   varchar(256),
    deleted      tinyint(1),
    constraint unique_service unique (name)
);
create index storage_service_entity_name_index on storage_service_entity (name);
create index storage_service_entity_deleted_index on storage_service_entity (id, deleted);

create table classification
(
    id         varchar(36)   primary key,
    name       varchar(256),
    json       json          not     null,
    updated_at datetime(3),
    updated_by varchar(256),
    constraint unique_classification unique(name)
);
create index classification_entity_name_index on classification (name);

create table tag (
    `id`              varchar(36)   primary key,
    classification_id varchar(36)   not     null,
    name              varchar(256),
    json              json          not     null,
    updated_at        datetime(3),
    updated_by        varchar(256),
    constraint unique_tag unique (classification_id, name)
);
create index tag_entity_classification_name_index on tag(classification_id, name);
create index tag_entity_name_index on tag(name);

create table tag_usage (
    source      int          not null,      # 0 : classification, 1 : glossary
    source_id   varchar(256) not null,
    tag_id      varchar(256) not null,
    label_type  int          not null,      # 0 : Manual, 1 : Propagated, 2 : Automated, 3 : Derived
    state       int          not null,      # 0 : Suggested, 1 : Confirmed
    target_type varchar(32)  not null,
    target_id   varchar(36)  not null,
    constraint tag_usage_key
        unique (source, source_id, tag_id, target_type, target_id)
);

create table entity_relationship (
    from_id     varchar(36)  not     null,
    to_id       varchar(36)  not     null,
    from_entity varchar(256) not     null,
    to_entity   varchar(256) not     null,
    relation    int          not     null,
    json_schema varchar(256) null,
    json        json         null,
    deleted     tinyint(1)   default 0     not null,
    primary key (from_id, to_id, relation)
);
create index from_entity_type_index on entity_relationship (from_id, from_entity);
create index from_index on entity_relationship (from_id, relation);
create index idx_entity_relationship_fromEntity_fromId_relation
    on entity_relationship (from_entity, from_id, relation);
create index idx_er_fromEntity_fromId_toEntity_relation
    on entity_relationship (from_entity, from_id, to_entity, relation);
create index to_entity_type_index on entity_relationship (to_id, to_entity);
create index to_index on entity_relationship (to_id, relation);

# Version History And ...
create table entity_extension (
    id          varchar(36)  not null,
    extension   varchar(256) not null,
    entity_type varchar(256) not null,
    json        json         not null,
    primary key (id, extension)
);
create index extension_index on entity_extension (extension);


