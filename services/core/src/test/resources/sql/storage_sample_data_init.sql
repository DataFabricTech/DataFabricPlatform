insert into DataStorageType (name, icon)
values ('postgresql',
        lo_get('/Users/fwani/Documents/develop/DataFabricPlatform/services/core/src/test/resources/sql/icon-postgresql.png')::bytea);

insert into DataStorageAdaptor (id, storage_type_name, name, version, path, driver)
values ('9c761353-2963-4963-995d-844e24b80d93',
        'postgresql',
        'postgresql 어댑터 1',
        'test 버전',
        '/tmp/postgresql.jar',
        'org.postgresql.Driver');

INSERT INTO public.urlformat (adaptor_id, format)
VALUES ('9c761353-2963-4963-995d-844e24b80d93', 'jdbc:postgresql://{host}:{port}/{database}'),
       ('9c761353-2963-4963-995d-844e24b80d93', 'jdbc:postgresql://{user}:{password}@{host}:{port}/{database}');
INSERT INTO public.connectionschema (adaptor_id, key, type, "default", required, basic)
VALUES ('9c761353-2963-4963-995d-844e24b80d93', 'host', 'STRING', 'localhost', true, true),
       ('9c761353-2963-4963-995d-844e24b80d93', 'port', 'INT32', '5432', true, true),
       ('9c761353-2963-4963-995d-844e24b80d93', 'database', 'STRING', null, true, true),
       ('9c761353-2963-4963-995d-844e24b80d93', 'user', 'STRING', null, true, false),
       ('9c761353-2963-4963-995d-844e24b80d93', 'password', 'STRING', null, true, false);

insert into DataStorage (id, adaptor_id, name, url, status, last_connection_checked_at, last_sync_at, sync_enable,
                         monitoring_enable)
values ('3c761353-2963-4963-995d-844e24b80d93',
        '9c761353-2963-4963-995d-844e24b80d93',
        'postgresql 연결정보 1',
        'jdbc:postgresql://{host}:{port}/{database}',
        'INIT',
        current_timestamp,
        current_timestamp,
        false,
        false);

insert into DataStorage("id", "adaptor_id", "name", "url", "user_desc", "sync_enable", "sync_type", "sync_week",
                        "sync_run_time", "monitoring_enable", "monitoring_protocol", "monitoring_host",
                        "monitoring_port", "monitoring_sql", "monitoring_period", "monitoring_timeout",
                        "monitoring_success_threshold", "monitoring_fail_threshold", "auto_add_setting_enable")
values ('1b6c8550-a7f8-4c96-9d17-cd10770ace87', '9c761353-2963-4963-995d-844e24b80d93', 'postgresql',
        'jdbc:postgresql://{id}:{password}@{host}:{port}/{database}', 'postgresql 연결 정보', true, 1, 10, '02:30', true,
        'SQL', 'localhost', '3306', 'SELECT 1', 30, 30, 1, 2, true);
insert into StorageAutoAddSetting("datastorage_id", "regex", "data_type", "data_format", "min_size", "max_size",
                                  "start_date", "end_date")
values ('1b6c8550-a7f8-4c96-9d17-cd10770ace87', '*', 'STRUCTURED', 'TABLE', -1, -1, '', '2022-12-31'),
       ('1b6c8550-a7f8-4c96-9d17-cd10770ace87', '\w+_VIEW', 'STRUCTURED', 'VIEW', -1, -1, '2022-01-01', '');
insert into ConnInfo("datastorage_id", "key", "type", "value", "required")
values ('1b6c8550-a7f8-4c96-9d17-cd10770ace87', 'HOST', 'STRING', '192.168.107.28', true),
       ('1b6c8550-a7f8-4c96-9d17-cd10770ace87', 'PORT', 'STRING', '14632', true),
       ('1b6c8550-a7f8-4c96-9d17-cd10770ace87', 'DATABASE', 'STRING', 'testdb', true),
       ('1b6c8550-a7f8-4c96-9d17-cd10770ace87', 'ID', 'STRING', 'testUser', true),
       ('1b6c8550-a7f8-4c96-9d17-cd10770ace87', 'PASSWORD', 'STRING', 'testUser', true),
       ('1b6c8550-a7f8-4c96-9d17-cd10770ace87', 'charset', 'STRING', 'utf-8', false),
       ('1b6c8550-a7f8-4c96-9d17-cd10770ace87', 'timezone', 'STRING', 'asia/seoul', false);
insert into DataStorageTag("datastorage_id", "tag")
values ('1b6c8550-a7f8-4c96-9d17-cd10770ace87', 'IT'),
       ('1b6c8550-a7f8-4c96-9d17-cd10770ace87', '데이터패브릭'),
       ('1b6c8550-a7f8-4c96-9d17-cd10770ace87', '설계');
insert into DataStorageMetadata("datastorage_id", "key", "value", "is_system")
values ('1b6c8550-a7f8-4c96-9d17-cd10770ace87', '프로젝트', '데이터패브릭', false);