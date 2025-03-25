# 저장소 가상화

## 1. 개요

저장소 가상화(Storage Virtualization)는 물리적 데이터 저장소의 분산된 리소스를 하나의
논리적 저장소로 통합하여 관리하는 기술입니다.
본 문서는 저장소 가상화 설계 문서로 유스케이스, 인터페이스, 시퀀스, 클래스, 데이터베이스 설계서를 포함한다.

## 2. 요구사항

일반 요구사항

1. 다양한 데이터 저장소 정보를 등록(가상화)하여 관리할 수 있는 기능
   3차년도 목표 7~8종
   - MySQL
   - MariaDB
   - PostgreSQL
   - MinIO
   - Oracle
   - MongoDB
   - MS-SQL
   - Hive
   - ...
2. 사용자 설정 가능한 메타데이터  
   1. 카테고리/태그  
   2. 사전  

보안 요구사항

1. 거버넌스 : 접근 제어
   1. 저장소 관리 기능에 대한 접근 제어  
   2. 저장소 정보(하위 데이터)에 대한 접근 제어 - 공개 / 비공개  

## 3. Usecase

```plantuml
@startuml
left to right direction
:사용자: as user

usecase "저장소 관리" as storage_management
usecase "저장소 리스트 조회" as list
usecase "저장소 정보 열람" as get
usecase "저장소 추가(가상화)" as add
usecase "연결 테스트" as con_test
usecase "저장소 정보(연결정보) 수정" as conn_modify
usecase "저장소 정보(메타데이터) 수정" as modify
usecase "저장소 정보 삭제" as delete
usecase "저장소 설정" as setup
storage_management <|-- list
storage_management <|-- get
storage_management <|-- add
storage_management <|-- conn_modify
storage_management <|-- modify
storage_management <|-- delete
storage_management <|-- setup
get <|-down- con_test
add <|-down- con_test
conn_modify <|-down- con_test
usecase "공유 및 검색 설정" as share
usecase "메타데이터(프로파일링) 수집 설정" as metadata
usecase "데이터 샘플링 설정" as sample
usecase "모니터링 설정" as monitoring
setup <|-- share
setup <|-- metadata
setup <|-- sample
setup <|-- monitoring

user -> storage_management

note "저장소 가상화 기능에 접근 가능한 사용자" as note_auth
user ... note_auth
@enduml
```

## 4. 시퀀스

- 저장소 관리  
  - 리스트 조회
  - 저장소 정보
    - 연결테스트  
  - 추가
    - 연결테스트  
  - 수정
    - 연결테스트  
  - 저장소 메타데이터 수정
  - 삭제  
  
```plantuml
@startuml
Actor 사용자 as user
box "OpenVDAP Service" #Lightblue
participant OVP as ovp
participant FabricServer as server
participant Metadata as metadata
participant Monitoring as monitoring
database Database as database
database ElasticSearch as search 
end box
box "Storage"
database "Target\nStorage" as storage
end box

' Storage
' List Page Request
user -> ovp ++ : 저장소 관리 창 입장(저장소 리스트)
ovp -> server ++ : List(PageRequest, WithFields)
server -> database ++ : Select Storage OrderBy / Offset / Limit
server <-- database --: Res
ovp <- server -- : Res : []StorageService
user <- ovp -- : 저장소 리스트 정보 

' GetById/Name
user -> ovp ++ : 저장소 정보 상세 보기
ovp -> server ++ : Get Storage
server -> database ++ : Get Storage(id or name)
server <-- database -- : Res
ovp <-- server -- : Res : StorageService
user <-- ovp -- : 저장소 상세 정보

' Create
user -> ovp ++ : 저장소 등록 창
note over ovp : 저장소 정보 입력
' 저장소 연결 테스트
user -> ovp : Connect Test Click
ovp -> server ++ : Connect Test
server -> metadata ++ : Connect Test
metadata -> storage ++ : Connect Test
metadata <- storage --: Success or Fail
server <- metadata -- : Success or Fail
ovp <-- server --: Success or Fail
opt fail
note over ovp : 연결 실패 할 경우 저장 불가 알림  
user <-- ovp : Fail
end
user -> ovp : Create/Save Click
note over server
데이터베이스 저장과 검색엔진 저장을 하나의 트랜잭션으로 처리
end note
ovp -> server ++: Save
server -> database ++: Save
server <-- database --: OK
server -> search ++: Save
server <-- search --: OK
group 메타데이터 수집, 모니터링 연동
server -> monitoring ++: Send Noti 
server <-- monitoring --: OK
end
ovp <-- server -- : Success
user <-- ovp -- : Success
|||

' 저장소 연결정보 수정
user -> ovp ++: 저장소 연결 정보 수정
note over ovp : 연결 관련 정보 수정으로 연결 테스트 필요
ovp -> server ++: Update StorageService 
server -> database ++: Save
server <-- database --: OK
server -> search ++: Save
server <-- search --: OK
group 메타데이터 수집, 모니터링 연동
server -> metadata ++: Update Storageservice
server <-- metadata --: OK
server -> monitoring ++: Send Noti 
server <-- monitoring --: OK
end
ovp <-- server -- : Success
user <-- ovp -- : Success

|||

' 저장소 메타데이터 수정
user -> ovp ++: 저장소 메타데이터 수정
note over user : 메타데이터 추가/수정/삭제
user -> ovp : Update 
ovp -> server ++ : Update Metadata
note right
데이터베이스 저장과 검색엔진 저장을 하나의 트랜잭션으로 처리
end note
server -> database ++: Update
server <-- database --: OK
server -> search ++: Update
server <-- search --: OK
ovp <-- server --: Success
user <-- ovp -- : Success

|||

' 저장소 삭제
user -> ovp ++: 저장소 삭제
ovp -> server ++: Delete Storage
note right
트랜잭션처리
end note
server -> database ++: Delete
server <-- database --: OK
server -> search ++: Delete
server <-- search --: OK
server -> metadata ++ : Delete
server <- metadata -- : OK
server -> monitoring ++ : Delete
server <- monitoring -- : OK
ovp <-- server --: Success
user <-- ovp -- : Success
@enduml
```
  
- 저장소 설정
  - 검색/공유 설정  
  - 메타데이터(프로파일링) 수집 설정  
    - 자세한 내용은 virtualzation-pipeline.md 을 참고
  - 샘플 수집 설정  
    - 자세한 내용은 virtualzation-pipeline.md 파일을 참고
  - 모니터링 설정  
    - 자세한 내용은 monitoring.md 파일을 참고

```plantuml
@startuml
Actor 사용자 as user
box "OpenVDAP Service" #Lightblue
participant Server as server
participant Metadata as metadata
participant Monitoring as monitoring
database Database as database
database ElasticSearch as search 
end box

' 저장소 설정
' 저장소 공유 및 검색 설정  
-> user : 저장소 공유 설정
user -> server ++ : 저장소 설정 정보
server -> database ++ : 저장소 설정 정보 조회  
server <-- database -- :
user <-- server --: 저장소 설정 정보  
user -> server ++ : 저장소 공유 설정 업데이트
server -> database ++ : 공유 설정 업데이트
server <-- database -- : Success
alt 공유 on 
server -> search ++ : 공개 설정
server <-- search -- : Success
else 공유 off
server -> search ++ : 비공개 설정
server <-- search -- : Success
end
user <-- server --: Success

' 메타데이터, 프로파일링, 샘플링, 모니터링 수집 설정  
-> user : 저장소 정보 수집 설정  
user -> server ++ : 저장소 정보 수집 설정 조회  
server -> database ++ : 저장소 정보 수집 설정 조회  
server <-- database -- 
user <-- server --: 저장소 정보 수집 설정

user -> server ++: 저장소 정보 수집 설정 정보 업데이트
server -> database ++ : 수집 설정 업데이트
server <-- database -- : Success
server -> metadata ++ : 메타, 프로파일링, 샘플링 수집 설정 변경\n(스케쥴링 수정만 진행됨)
server <-- metadata -- : Success
server -> monitoring ++ : 모니터링 설정 변경 알림
server <-- monitoring -- : Success
user <- server -- : Success

@enduml
```

## 5. 클래스 다이어그램

**참고용**  

| 유형                    | 기호    | 목적                                                                   |
| ----------------------- | ------- | ---------------------------------------------------------------------- |
| 의존성(Association)     | `-->`   | 객체가 다른 객체를 사용함. ( A `-->` B)                                |
| 확장(Inheritance)       | `<\|--` | 계층 구조에서 클래스의 특수화. (부모 `<\|--` 자식)                     |
| 구현(Implementation)    | `<\|..` | 클래스에 의한 인터페이스의 실현. (Interface `<\|..` Class)             |
| 약한 의존성(Dependency) | `..>`   | 더 약한 형태의 의존성. A 클래스 메소스 파라미터로 B를 사용( A `..>` B) |
| 집합(Aggregation)       | `o--`   | 부분이 전체와 독립적으로 존재할 수 있음( 클래스 `o--` 부분 클래스)     |
| 컴포지션(Composition)   | `*--`   | 부분이 전체 없이 존재할 수 없음( 클래스 `*--` 부분 클래스)             |

- 저장소 연결 정보  

```plantuml
@startuml
enum StorageType {
  Mysql
  Postgres
  MariaDB
  Oracle
  Mssql
  MinIO
  Trino
  Hive
  SQLite
  MongoDB
  ElasticSearch
  OpenSearch
}

enum SslMode {
  disable
  allow
  require
}

class SslConfig {
  File caCertificate
  File sslCertificate
  File sslKey
}

class StorageConnection {
  String type
  String username
  String password
  String hostPort
  String database
  Map<String, String> connectionOptions
  Map<String, Object> connectionArguments
  SslMode sslMode
  SslConfig sslCnofig
}

StorageConnection -up-> StorageType
StorageConnection -up-> SslMode
StorageConnection -up-> SslConfig

enum KindOfStorage {
  Database
  ObjectStorage
  Search
}

StorageService --> StorageType
StorageService -right-> KindOfStorage
StorageService -left-> StorageConnection 

class StorageService {
  String id
  String name
  String displayName
  KindOfStorage kindOfStorage
  StorageType serviceType
  String description
  StorageConnection connection
  Pipeline pipelines
  String testConnectionResult
  EntityReference[] tags
  Double version
  LocalDateTime updatedAt
  String updatedBy
  EntityReference[] owners
  String href
  String changeDescription
  Boolean deleted
}

class EntityReference {
  UUID id
  DataType type
  String name
  String description
  String displayName
  Boolean deleted
  String href
}

StorageService --> EntityReference

@enduml
```

---

```plantuml
@startuml
class StorageServiceController {
  list(page, size, withFields)
  getById()
  getByName()
  create()
  update()
  delete()
  versionHistory()
  version(version)
}

class StorageServiceApp {
  EntityRelationshipRepository relationshipRepo
  list()
  getById()
  getByName()
  create()
  update()
  versionHistory()
  version(version)
  delete()
}

interface JpaRepository {
}

JpaRepository <-- StorageServiceRepository
interface StorageServiceRepository {
  
}
@enduml
```

## 6. 인터페이스 설계

> 본 문서에서는 현 시점(25.03.24)에서는 인터페이스 리스트만을 작성한다.  
> 상세한 내용에 대해서는 Swagger를 활용하거나 본 문서에 내용을 업데이트하여 제공한다.  

### 6.1. 저장소 관리

1. List
2. GetById, getByName
3. Create
4. Update
5. ConnectionTest
6. DeleteById/Name
7. VersionHistory

### 6.2. 저장소 설정

1. Search
   1. Enable/Disable
2. Monitoring
  [모니터링 설계 문서](../monitoring/monitoring.md)
3. Pipeline
  [파이프라인 설계 문서](./virtualization-pipeline.md)

## 7. 데이터베이스

- StorageService

| Column         | Data Type   | Constraints      | Index | Desc                                 |
| -------------- | ----------- | ---------------- | :---: | ------------------------------------ |
| `id`           | UUID        | PRIMARY KEY      |   v   | 저장소 고유 식별자                   |
| `name`         | CHAR(256)   | UNIQUE, NOT NULL |   v   | 저장소 이름                          |
| `kind`         | CHAR(128)   | NOT NULL         |       | 저장소 종류(database, objectstorage) |
| `storage_type` | CHAR(256)   | NOT NULL         |       | 저장소 타입                          |
| `json`         | JSON        | NOT NULL         |       | StorageService JSON String           |
| `updated_at`   | DATETIME(3) | NOT NULL         |       | 저장소 데이터 변경 시간              |
| `udpated_by`   | CHAR(256)   | NOT NULL         |       | 저장소 정보 변경 사용자              |
| `deleted`      | BOOLEAN     |                  |   v   | 저장소 삭제 여부                     |
