# DataLayer

## 스펙

- Java 11

## 소개

본 소프트웨어는 데이터 패브릭의 데이터 저장을 위한 소프트웨어이다.

- Data Layer의 주요 기능
    - 데이터 저장 및 불러오기
    - 데이터 동기화

## DataLayer 구성

Gradle (Kotlin DSL활용)이용한 멀티 프로젝트 구조로 구성되어 있다.

- data-fabric-platform
    - Services
        - DataLayerService
        - DataSyncService
        - PortalService

## Service 별 설명

### DataLayerService

- 저장을 관할하는 서비스로, 데이터를 RDBMS 저장소와 OpenSearch에 저장하는 역할을 한다.

### DataSyncService

- RDBMS에 저장된 데이터를 최신화하는 역할을 한다.
- RDBMS 저장소에 저장된 데이터와 OpenSearch에 저장된 데이터가 일치하는 지를 확인하는 역할을 한다.

### PortalService

- 검색을 관할하는 서비스로, 사용자의 검색을 지원하는 역할을 한다.



## 동작 설명

### DataLayer의 저장 요청과 요청 처리 흐름

1. Client Request -  Message Queue - DataLayerService - RDBMS

### DataSync의 처리 흐름

1. Client Request - Message Queue - DataSyncService - ExtractionService - DataLayerService - RDBMS/OpenSearch
2. CronJob - DataSyncService - ExtractionService - DataLayerService - RDBMS/OpenSearch

### PortalService의 처리 흐름

1. Client Request - Message Queue - PortalService - OpenSearch

### Debugging

MSA 구조에 따른 오류(로그) 분석 어려움을 Jaeger(예거) 활용한다.
Spring Cloud Gateway, Service 들에 적용하여 요청 처리 중 오류가 발생한 부분을 쉽게 파악할 수 있다.

### Logging

## 개발 가이드

저장 객체는 다른 모듈에서도 사용 가능해야하는 객체로, share 디렉토리에 두어, 다른 모듈에서도 사용할 수 있게하였습니다.

### 빌드 로직 구성

```md
.
├── services
│   └── dataLayer               - 본 소프트웨어가 존재하는 디렉토리
└── share
    └── models/src/main/java
        └── dto		              - RDBMS에 저장될 객체에 대한 정보가 모여있는 디렉토리
```