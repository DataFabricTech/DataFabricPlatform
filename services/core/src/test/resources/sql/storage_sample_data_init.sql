insert into DataStorageType (name, icon)
values ('postgresql', lo_get('/Users/fwani/Documents/develop/DataFabricPlatform/services/core/src/test/resources/sql/icon-postgresql.png')::bytea);

insert into DataStorageAdaptor (id, storage_type_name, name, version, path, driver)
values ('9c761353-2963-4963-995d-844e24b80d93',
        'postgresql',
        'postgresql 어댑터 1',
        'test 버전',
        '/tmp/postgresql.jar',
        'org.postgresql.Driver');

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
