# 파이프라인

## 1. 개요

본 문서는 저장소로부터 메타데이터 수집하여 제공하는 프로세스의 설계 문서로 유스케이스,
인터페이스, 시퀀스, 클래스, 데이터베이스 설계서를 포함한다.

## 2. 요구사항

일반 요구사항

1. 다양한 데이터 저장소로부터 메타데이터를 수집하여 제공할 수 있는 기능  
   3차년도 목표 7종
   - MySQL
   - MariaDB
   - PostgreSQL
   - MinIO
   - Oracle
   - Hadoop
   - ...
2. 데이터 종류
   1. Table
   2. CSV
   3. WORD, HWP
   4. 이미지
   5. 영상
3. 메타데이터 수집
4. 데이터 메트릭 수집(프로파일링)
5. 데이터 퀄리티(테스트)
6. 샘플 데이터 수집
   1. 수집 방식
   2. 샘플 데이터 크기
7. 로그 수집(openmetadata 의 usage, lineage) - ??
   1. 이 기능의 경우 각 데이터베이스에 추가적인 설정이 필요함.

보안 요구사항

1. 거버넌스 : 접근 제어
   1. 저장소 관리(파이프라인) 기능에 대한 접근 제어  

- 데이터 샘플링  
  - 테이블
    - 라인, 퍼센트
    - random, system, ...
  - 문서
    - 페이지
    - first, random, ...
  - 이미지
    - ...
  - 영상
    - time
    - first, random, ...
  - 데이터 셋
    - ...
## 3. Usecase

```plantuml
@startuml
left to right direction
:사용자: as user

usecase "저장소 관리\n파이프라인" as management
usecase "파이프라인 조회" as list
usecase "파이프라인 열람" as get
usecase "파이프라인 추가" as add
usecase "파이프라인 재배포" as deploy
usecase "파이프라인 시작" as start
usecase "파이프라인 중지" as stop
usecase "파이프라인 로그 보기" as log
usecase "파이프라인 수정" as modify
usecase "파이프라인 삭제" as delete
management <|-- list
management <|-- get
management <|-- add
management <|-- deploy
management <|-- start
management <|-- stop
management <|-- log
management <|-- modify
management <|-- delete

user -> management

note "저장소 가상화 기능에 접근 가능한 사용자" as note_auth
user ... note_auth
@enduml
```

## 4. 클래스

**참고용 OpenMetadata Pipeline 객체**  

![metadata](/share/schema/src/main/resources/json/schema/metadataIngestion/databaseServiceMetadataPipeline.json)
![profiler](/share/schema/src/main/resources/json/schema/metadataIngestion/databaseServiceProfilerPipeline.json)
![usage](/share/schema/src/main/resources/json/schema/metadataIngestion/databaseServiceQueryUsagePipeline.json)
![lineage](/share/schema/src/main/resources/json/schema/metadataIngestion/databaseServiceQueryLineagePipeline.json)
![storage_metadata](/share/schema/src/main/resources/json/schema/metadataIngestion/storageServiceMetadataPipeline.json)
![apiservice_metadata](/share/schema/src/main/resources/json/schema/metadataIngestion/apiServiceMetadataPipeline.json)

| 유형                    | 기호    | 목적                                                                   |
| ----------------------- | ------- | ---------------------------------------------------------------------- |
| 의존성(Association)     | `-->`   | 객체가 다른 객체를 사용함. ( A `-->` B)                                |
| 확장(Inheritance)       | `<\|--` | 계층 구조에서 클래스의 특수화. (부모 `<\|--` 자식)                     |
| 구현(Implementation)    | `<\|..` | 클래스에 의한 인터페이스의 실현. (Interface `<\|..` Class)             |
| 약한 의존성(Dependency) | `..>`   | 더 약한 형태의 의존성. A 클래스 메소스 파라미터로 B를 사용( A `..>` B) |
| 집합(Aggregation)       | `o--`   | 부분이 전체와 독립적으로 존재할 수 있음( 클래스 `o--` 부분 클래스)     |
| 컴포지션(Composition)   | `*--`   | 부분이 전체 없이 존재할 수 없음( 클래스 `*--` 부분 클래스)             |

  - 메타데이터(프로파일링) 수집 설정  
    - Include / Exclude
  - 샘플 수집 설정  
    - 수집 방식
    - 샘플 데이터 사이즈  
  - 모니터링 설정  
    - 모니터링 방식  
    - 모니터링 주기  

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

enum PipelineType {
  MetadataIngestion
  Profiler

}

abstract class Workflow {
  PipelineType type
}

class  Metadata {
  PipelineType type
  Boolean markDeleted
  Boolean includeTables
  Boolean includeViews
  Boolean includeTags
  Boolean includeOwners
  FilterPattern databaseFilterPattern
  FilterPattern schemaFilterPattern
  FilterPattern tableFilterPattern
  FilterPattern bucketFilterPattern
  FilterPattern pathFilterPattern
  FilterPattern fileFilterPattern
  Integer queryLogDuration
  Integer queryParsingTimeoutLimit
}

class Profiler {
    "computeMetrics": {
      "description": "Option to turn on/off computing profiler metrics.",
      "type": "boolean",
      "default": true,
      "title": "Compute Metrics"
    },
    "computeTableMetrics": {
      "description": "Option to turn on/off table metric computation. If enabled, profiler will compute table level metrics.",
      "type": "boolean",
      "default": true,
      "title": "Compute Table Metrics"
    },
    "computeColumnMetrics": {
      "description": "Option to turn on/off column metric computation. If enabled, profiler will compute column level metrics.",
      "type": "boolean",
      "default": true,
      "title": "Compute Column Metrics"
    },
    "useStatistics": {
      "description": "Use system tables to extract metrics. Metrics that cannot be gathered from system tables will use the default methods. Using system tables can be faster but requires gathering statistics before running (for example using the ANALYZE procedure). More information can be found in the documentation: https://docs.openmetadata.org/latest/profler",
      "type": "boolean",
      "default": false,
      "title": "Use Gathered Statistics"
    },
    "profileSampleType": {
      "$ref": "../entity/data/table.json#/definitions/profileSampleType",
      "title": "Profile Sample Type"
    },
    "profileSample": {
      "description": "Percentage of data or no. of rows used to compute the profiler metrics and run data quality tests",
      "type": "number",
      "default": null,
      "title": "Profile Sample"
    },
    "samplingMethodType": {
      "$ref": "../entity/data/table.json#/definitions/samplingMethodType",
      "title": "Sampling Method Type"
    },
}

class Usage {

    "type": {
      "description": "Pipeline type",
      "$ref": "#/definitions/databaseUsageConfigType",
      "default": "DatabaseUsage"
    },
    "queryLogDuration": {
      "description": "Configuration to tune how far we want to look back in query logs to process usage data.",
      "type": "integer",
      "default": 1,
      "title": "Query Log Duration"
    },
    "stageFileLocation": {
      "description": "Temporary file name to store the query logs before processing. Absolute file path required.",
      "type": "string",
      "default": "/tmp/query_log",
      "title": "Stage File Location"
    },
    "filterCondition": {
      "description": "Configuration the condition to filter the query history.",
      "type": "string",
      "title": "Filter Condition"
    },
    "resultLimit": {
      "description": "Configuration to set the limit for query logs",
      "type": "integer",
      "default": 1000,
      "title": "Result Limit"
    },
    "queryLogFilePath": {
      "description": "Configuration to set the file path for query logs",
      "type": "string",
      "title": "Query Log File Path"
    }

  }

@enduml
```

```plantuml
@startuml
left to right direction

class CreateWorkflow {
  String name
  String displayName
  String description
    "workflowType": {
      "description": "Type of the workflow.",
      "$ref": "../../entity/automations/workflow.json#/definitions/workflowType"
    },
    "request": {
      "description": "Request body for a specific workflow type",
      "oneOf": [
        {
          "$ref": "../../entity/automations/testServiceConnection.json"
        }
      ]
    },
    "status": {
      "description": "Workflow computation status.",
      "$ref": "../../entity/automations/workflow.json#/definitions/workflowStatus",
      "default": "Pending"
    },
    "response": {
      "description": "Response to the request.",
      "oneOf": [
        {
          "$ref": "../../entity/services/connections/testConnectionResult.json"
        }
      ]
    },
    "owners": {
      "description": "Owners of this workflow.",
      "$ref": "../../type/entityReferenceList.json",
      "default": null
    },

  }
class  workflow {
  UUIR id
  String name
    "openMetadataWorkflowConfig": {
      "description": "OpenMetadata Ingestion Workflow Config.",
      "type": "object",
      "properties": {
        "source": {
          "$ref": "#/definitions/source"
        },
        "processor": {
          "$ref": "#/definitions/processor"
        },
        "sink": {
          "$ref": "#/definitions/sink"
        },
        "stage": {
          "$ref": "#/definitions/stage"
        },
        "bulkSink": {
          "$ref": "#/definitions/bulkSink"
        },
        "workflowConfig": {
          "$ref": "#/definitions/workflowConfig"
        },
        "ingestionPipelineFQN": {
          "description": "Fully qualified name of ingestion pipeline, used to identify the current ingestion pipeline",
          "type": "string"
        },
        "pipelineRunId": {
          "description": "Unique identifier of pipeline run, used to identify the current pipeline run",
          "$ref": "../type/basic.json#/definitions/uuid"
        }
      },
      "required": ["source", "workflowConfig"],
      "additionalProperties": false
    }
  },
}

@enduml
```

## 5. 시퀀스

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
