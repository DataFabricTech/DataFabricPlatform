# StorageService API

## 기능

- 저장소 유형
  - 리스트
  - 등록
- 연결정보
  - 등록
  - 조회
  - 리스트
- 히스토리
  - 조회

## 데이터 구조

```
// Use DBML to define your database structure
// Docs: https://dbml.dbdiagram.io/docs

Table DataStorageType {
  name text [primary key]
  icon blob
}

Table DefaultConnSchema {
  storage_type_name text [note: 'fk']
  key text
  type text
  default blob
  required bool

  note: 'url, host, port, database, bucket, ...'
} 

Ref: DefaultConnSchema.storage_type_name > DataStorageType.name


Table DataStorageAdaptor {
  id uuid [primary key]
  storage_type_name varchar [note: 'fk']
  name text [unique]
  version text [unique]
  path text [note: 'jdbc file path']
  driver text [note: 'jdbc driver class']
  created_by uuid
  created_at timestamp
  updated_by uuid
  updated_at timestamp
  deleted_by uuid
  deleted_at timestamp
}
Ref: DataStorageType.name < DataStorageAdaptor.storage_type_name

Table ConnectionSchema {
  adaptor_id uuid [note: 'fk']
  key text [note: '연결 할때 필요한 옵션 키']
  type text [note: '옵션 값의 타입']
  default blob
  required bool
}
Ref: DataStorageAdaptor.id < ConnectionSchema.adaptor_id

Enum AuthType {
  NO_AUTH
  USER_PASSWORD
  CERTIFICATE
}

Table AuthSchema {
  storage_type_name text [note: 'fk']
  auth_type AuthType
  key text
  type text [note: '옵션 값의 타입']
  required bool
}

Ref: AuthSchema.storage_type_name > DataStorageType.name

Table AdaptorUsableAuth {
  adaptor_id uuid [note: 'fk']
  auth_type AuthType
}

Ref: AdaptorUsableAuth.adaptor_id > DataStorageAdaptor.id

Table UrlFormat {
  adaptor_id uuid [note: 'fk']
  format text
}
Ref: UrlFormat.adaptor_id > DataStorageAdaptor.id

Table DataStorage {
  id uuid [primary key]
  adpator uuid [note: 'fk']
  name text
  url text
  user_desc text [note: '????']
  total_data int [note: '확인되는 전체 데이터(테이블?) 수']
  regi_data int [note: '패브릭 시스템에 등록(가상화)된 데이터(테이블?) 수']
  created_by uuid
  created_at timestamp
  updated_by uuid
  updated_at timestamp
  deleted_by uuid
  deleted_at timestamp
  status enum
  last_connection_checked_at timestamp
  last_sync_at timestamp

  sync_enable bool
  sync_type int
  sync_week int
  sync_run_time text

  monitoring_enable bool
  monitoring_protocol text
  monitoring_host text
  monitoring_port text
  monitoring_sql text
  monitoring_period int
  monitoring_timeout int [note: 'sec 단위']
  monitoring_success_threshold int
  monitoring_fail_threshold int
}
Ref: DataStorage.adpator > DataStorageAdaptor.id

Table ConnInfo {
  datastorage_id uuid [note: 'fk']
  "key" text
  "type" text
  "value" blob [note: 'decord by type of connection schema']
}

Table StorageAutoAddSetting {
  datastorage_id uuid [note: 'fk']
  regex text [note: '???']
  data_type text
  data_format text
  min_size int
  max_size int
  start_date text
  end_date text

  // 아래는 참고
  // file_type enum
  // table_type enum
  // begin timestamp
  // end timestamp
  // row_limit int
  // size_limit int [note: 'MB 단위']
  // recursive bool
  // auto_import bool [note: '새로운 데이터 확인 시 자동 추가']
  // auto_update bool [note: '데이터 변경 확인시 자동 업데이트']
  // read_size int [note: 'Line/Byte 단위 ??']
  // cache_enable bool
  // cache_max_size int
  // cache_mem_size int [note: 'MB 단위']
}

Table DataStorageMetadata {
  datastorage_id uuid [note: 'fk']
  key text
  value text
}

Table DataStorageTag {
  datastorage_id uuid [note: 'fk']
  user_id uuid
  tag text
}

Ref: DataStorage.id < ConnInfo.datastorage_id
Ref: DataStorage.id < StorageAutoAddSetting.datastorage_id
Ref: DataStorage.id < DataStorageMetadata.datastorage_id
Ref: DataStorage.id < DataStorageTag.datastorage_id

Table FileType {
  name text
  extension text
}

Table TableType {
  name text
  type text
}

Table ConnectionCheckProtocol {
  protocol text [note: 'TCP, UDP, HTTP, HTTPS, ICMP, SQL']
}
```

## API

- `/storage/v1`
  - `/storage-type`
    - `GET /`
      - 저장소 타입 리스트
    - `POST /`
      - 저장소 타입 등록
  - `/adaptor`
    - `GET /?type={type}`
      - Jdbc list
    - `GET /?id={id}`
      - jdbc 정보 한개만
    - `GET /schema/{id}`
      - Jdbc 의 연결할때 옵션들
  - `/connection`
    - `GET /info`
      - 연결정보 리스트
    - `GET /info/{id}?order=&limit=&offset=`
      - 연결정보 조회
    - `POST /info`
      - 연결정보 등록
    - `PATCH /info/{id}`
      - 연결정보 업데이트
    - ``POST /test with body`
      - 연결 테스트
  - `/history`
    - `GET /`
      - 히스토리 조회

## 구조

- 내부 db
  - ORM (Hibernates)
  - DB 조회
- JDBC 연결 파트
  - Jdbc 를 이용한 연결
  - 쿼리 수행
  - 결과 반환
- 