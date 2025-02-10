# 저장소 가상화

## 1. 개요

저장소 가상화(Storage Virtualization)는 물리적 데이터 저장소의 분산된 리소스를 하나의
논리적 저장소로 통합하여 관리하는 기술입니다.
본 문서는 저장소 가상화 설계 문서로 유스케이스, 인터페이스, 시퀀스, 클래스, 데이터베이스 설계서를 포함한다.

## 2. 요구사항

일반 요구사항

1. 다양한 데이터 저장소 정보를 등록(가상화)하여 관리할 수 있는 기능
   3차년도 목표 7종
   - MySQL
   - MariaDB
   - PostgreSQL
   - MinIO
   - Oracle
   - Hadoop
   - ...
2. 사용자 설정 가능한 메타데이터  
   1. 태그  
   2. 카테고리  
   3. 사전  
   4. 즐겨찾기  
3. 카탈로그  
   1. 저장소 내 데이터 정보를 바탕으로 데이터 카탈로그 생성  

4. 자동으로 메타 데이터를 생성하는 기능 개발  
5. 다양한 인증 방식 지원 - 지원 고려  

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
participant Server as server
participant Metadata as metadata
participant Monitoring as monitoring
database Database as database
database ElasticSearch as search 
end box
box "Storage"
database "Target\nStorage" as storage
end box

' 저장소 리스트 조회 
user -> server ++ : Get Storage List
user <- server -- : Res : []ResStorageInfo
|||

' 저장소 정보 조회 
user -> server ++ : Get Storage
server -> database ++ : Get Storage(id or name)
server <-- database --: Result
user <-- server --: Res : ResStorageInfo
' 저장소 연결 테스트
note over user : 저장소 정보 창에서 연결 테스트
user -> server ++ : Connect Test
server -> storage ++ : Connect Test
server <- storage --: Success or Fail
user <-- server --: Success or Fail
' 저장소 정보에 메타데이터 수집 파이프라인 정보와 모니터링 정보 포함 필요
|||

' 저장소 추가
-> user ++: 저장소 가상화
note over user : 저장소 정보 입력
' 저장소 연결 테스트
user -> server ++ : Connect Test
server -> storage ++ : Connect Test
server <- storage --: OK 
user <-- server --: Success
opt connect fail 
server -> storage ++ : Connect Test
note left : 저장소 연결 실패 시 다음 진행 불가
server <- storage --: Error
user <-- server : fail
<-- user : fail
end
user -> server ++: Create Storage
note right
데이터베이스 저장과 검색엔진 저장을 하나의 트랜잭션으로 처리
end note
server -> database ++: Save
server <-- database --: OK
server -> search ++: Save
server <-- search --: OK
group 메타데이터 수집, 모니터링 연동
server -> metadata ++: Create Default Pipeline
server <-- metadata --: OK
server -> monitoring ++: Create Default Monitoring
server <- monitoring --: OK
end
user <-- server --: Success
<-- user -- : 
|||

' 저장소 연결정보 수정
-> user ++: Modify Storage Info
note over user : 연결 정보 수정
user -> server ++ : Connect Test
server -> storage ++ : Connect Test
server <- storage --: OK 
user <-- server --: Success
opt connect fail 
server -> storage ++ : Connect Test
note over user : 저장소 연결 실패 시 다음 진행 불가
server <- storage --: Error
user <-- server : fail
<-- user : fail
end
user -> server ++: Modify Storage
note right
데이터베이스 저장과 검색엔진 저장을 하나의 트랜잭션으로 처리
end note
server -> database ++: Update
server <-- database --: OK
server -> search ++: Update
server <-- search --: OK
user <-- server --: Success
<-- user --: 
|||

' 저장소 메타데이터 수정
-> user ++: Modify Storage Metadata
note over user : 메타데이터 추가/수정/삭제
user -> server ++: Metadata Update
note right
데이터베이스 저장과 검색엔진 저장을 하나의 트랜잭션으로 처리
end note
server -> database ++: Update
server <-- database --: OK
server -> search ++: Update
server <-- search --: OK
user <-- server --: Success
<-- user --: 
|||

' 저장소 삭제
-> user ++: Delete
user -> server ++: Delete Storage
note right
데이터베이스 저장과 검색엔진 저장을 하나의 트랜잭션으로 처리
end note
server -> database ++: Delete
server <-- database --: OK
server -> search ++: Delete
server <-- search --: OK
server -> metdata ++ : Delete
server <- metdata -- : OK
server -> monitoring ++ : Delete
server <- monitoring -- : OK
user <-- server --: Success
<-- user -- : 
@enduml
```
  
- 설정  
  - 기본설정  
    - 조회  
    - 수정  
      - 메타데이터 수집  
      - 샘플링  
      - 모니터링  
  - 검색/공유 설정  
  - 메타데이터(프로파일링) 수집 설정  
  - 샘플 수집 설정  
  - 모니터링 설정  

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

' 저장소 공통 설정 정보 조회
user -> server ++ : 기본 설정 정보 조회
note right
자동 메타데이터 수집 설정 정보
샘플링 기본 설정 정보
모니터링 설정 정보
end note
user <-- server -- : 
|||

' 저장소 공통 설정 정보 변경
user -> server ++ : 저장소 설정 업데이트 요청
server -> database ++ : 저장소 설정 업데이트 
server <-- database -- : 
user <- server -- : Success
|||

' 저장소 별 설정
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

**참고용 OpenMetadata Service 객체**  

![databaseservice](/share/schema/src/main/resources/json/schema/entity/services/databaseService.json)

| 유형                    | 기호    | 목적                                                                   |
| ----------------------- | ------- | ---------------------------------------------------------------------- |
| 의존성(Association)     | `-->`   | 객체가 다른 객체를 사용함. ( A `-->` B)                                |
| 확장(Inheritance)       | `<\|--` | 계층 구조에서 클래스의 특수화. (부모 `<\|--` 자식)                     |
| 구현(Implementation)    | `<\|..` | 클래스에 의한 인터페이스의 실현. (Interface `<\|..` Class)             |
| 약한 의존성(Dependency) | `..>`   | 더 약한 형태의 의존성. A 클래스 메소스 파라미터로 B를 사용( A `..>` B) |
| 집합(Aggregation)       | `o--`   | 부분이 전체와 독립적으로 존재할 수 있음( 클래스 `o--` 부분 클래스)     |
| 컴포지션(Composition)   | `*--`   | 부분이 전체 없이 존재할 수 없음( 클래스 `*--` 부분 클래스)             |

```plantuml
@startuml
left to right direction

' abstract        abstract
' abstract class  "abstract class"
' annotation      annotation
' circle          circle
' ()              circle_short_form
' class           class
' class           class_stereo  <<stereotype>>
' diamond         diamond
' <>              diamond_short_form
' entity          entity
' enum            enum
' exception       exception
' interface       interface
' metaclass       metaclass
' protocol        protocol
' stereotype      stereotype
' struct          struct


enum DataType {
  Database
  DatabaseSchema
  Table
  File
}

class EntityReference {
  UUID id
  DataType type
  String name
  String fullyQualifiedName
  String description
  String displayName
  Boolean deleted
  ' Boolean inherited
  String href
}


enum StorageServiceType {
  Mysql
  MariaDB
  Postgres
  Mssql
  Oracle
  Hive
  Druid
  SQLite
  MongoDB
  S3
  MinIO
}

class StorageConnection {
  StorageServiceType type
  Connection Scheme
  String username
  String password
  String hostPort
  String databaseName
  String bucketNames
  String databaseSchema
  String prefix
  Map<String, String> connectionOptions
  Map<String, Object> connectionArguments
}

StorageConnection ..> StorageServiceType

enum TagSource {
  Classification
  Glossary
}

enum TagType {
  
}

class Tag {
  String tagFQN
  String  name
  String displayName
  String description
  TagSource source
  TagType labelType
  TagState state
  URI href
}

Tag -> TagSource

class  StorageService {
  UUID id
  String name
  String fullyQualifiedName
  String displayName
  StorageServiceType serviceType
  String description
  StorageConnection connection
  TestConnectionResult testConnectionResult
  Tag[] tags
  String version
  Datetime updatedAt
  String updatedBy
  EntityReference[] owners
  URI href
  String changeDescription
  Boolean deleted
  ..
  Settings 
  EntityReference[] pipelines
}

EntityReference ..> DataType
StorageService -> EntityReference
StorageService ..> StorageServiceType
StorageService -> StorageConnection

@enduml
```

## 6. 인터페이스 설계

> 본 문서에서는 현 시점(25.02.06)에서는 필요한 인터페이스만을 나열한다.
> 상세한 내용에 대해서는 Swagger를 활용하거나 본 문서에 내용을 업데이트 한다.  

### 6.1. 저장소 관리

**저장소 리스트**  
**저장소 정보 조회**  
**연결테스트**  
**추가**  
**연결정보 수정**  
**메타데이터 설정(업데이트)**  
**삭제**  

### 6.2. 저장소 설정

- 설정  
  - 기본설정  
    - 조회  
    - 수정  
      - 메타데이터 수집  
      - 샘플링  
      - 모니터링  
  - 검색/공유 설정  
  - 메타데이터(프로파일링) 수집 설정  
  - 샘플 수집 설정  
  - 모니터링 설정  

## 7. 데이터베이스

Database, Storage 가 분리되어 있었으나 통합.
UserDefine Driver를 사용할 수 있는 구조로 변경.

**StorageCommonConfiguration**  

| Column       | Data Type | Constraints                      | Desc                                   |
| ------------ | --------- | -------------------------------- | -------------------------------------- |
| `id`         | UUID      | PRIMARY_KEY                      | 아이디                                 |
| `json`       | JSON      | NOT NULL                         | 저장소 공통 설정 정보                  |
| `version`    | INT       | NOT NULL                         | 저장소 업데이트 정보 |
| `updated_at` | DATETIME  | NOT NULL, CURRENT_TIME ON UPDAET | 저장소 공통 설정 정보 업데이트 시간    |
| `updated_by` | UUID      | NOT NULL, FK('user.id')          | 저장소 공통 설정 정보 변경 사용자 정보 |

**Storage**  

| Column | Data Type | Constraints | Index | Desc               |
| ------ | --------- | ----------- | :---: | ------------------ |
| `id`   | CHAR(64)  | PRIMARY KEY |   v   | 저장소 고유 식별자 |
|        |           |             |       |                    |

**Driver**  
**Metadata**  
**Tag**  
**Glossary**  
