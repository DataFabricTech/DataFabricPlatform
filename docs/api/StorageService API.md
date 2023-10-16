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

Table DataStorage {
  id uuid [primary key]
  type uuid [note: 'fk']
  adpator uuid [note: 'fk']
  name text
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
}

Table DataStorageType {
  id uuid [primary key]
  name varchar
}
Table DataStorageConnectionSchema {
  datastorage_adaptor_id uuid [note: 'fk']
  "key" text [note: '연결 할때 필요한 옵션 키']
  "type" text [note: '옵션 값의 타입']
  required bool
}

Table DataStorageAdaptor {
  id uuid [primary key]
  datastorage_type_id uuid [note: 'fk']
  name text
  version text
  url text [note: '연결 url 포멧']
  path text [note: 'jdbc file path']
  driver text [note: 'jdbc driver class']
  created_by uuid
  created_at timestamp
  updated_by uuid
  updated_at timestamp
  deleted_by uuid
  deleted_at timestamp
}

Table DataStorageConnectionInfo {
  datastorage_id uuid [note: 'fk']
  "key" text
  "value" text
  created_by uuid
  created_at timestamp
  updated_by uuid
  updated_at timestamp
}

Table DataStorageSetting {
  datastorage_id uuid [note: 'fk']
  regex text [note: '???']
  file_type enum
  table_type enum
  begin timestamp
  end timestamp
  row_limit int
  size_limit int [note: 'MB 단위']
  recursive bool
  sync bool
  sync_period text [note: 'type-day=>1,2,3,4,..  type-dow=>mon,tue,...']
  sync_period_type enum [note: 'day, dow(day of the week)']
  sync_time time
  auto_import bool [note: '새로운 데이터 확인 시 자동 추가']
  auto_update bool [note: '데이터 변경 확인시 자동 업데이트']
  connection_check bool
  connection_check_period text
  connection_check_time time
  connection_check_protocol enum
  connection_check_port int
  connection_check_cmd_sql text
  connection_check_timeout int [note: 'sec 단위']
  connection_check_success_threshold int
  connection_check_fail_threshold int
  read_size int [note: 'Line/Byte 단위 ??']
  cache_enable bool
  cache_max_size int
  cache_mem_size int [note: 'MB 단위']
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


Ref: DataStorage.type > DataStorageType.id
Ref: DataStorage.adpator > DataStorageAdaptor.id
Ref: DataStorageAdaptor.id < DataStorageConnectionSchema.datastorage_adaptor_id
Ref: DataStorageType.id < DataStorageAdaptor.datastorage_type_id
Ref: DataStorage.id < DataStorageConnectionInfo.datastorage_id
Ref: DataStorage.id - DataStorageSetting.datastorage_id
Ref: DataStorage.id < DataStorageMetadata.datastorage_id
Ref: DataStorage.id < DataStorageTag.datastorage_id

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