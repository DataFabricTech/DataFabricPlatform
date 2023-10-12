# DataFabricPlatform

## 스펙

- Java 11

## 소개

본 저장소는 데이터 패브릭 구성요소 중 Data Catalog 를 위한
플랫폼 서비스(모듈) 저장소이다.

- Data Catalog 주요 기능
    - Virtualization(Metadata)
    - Governance(Access Control(Access, View))
    - Monitoring
    - Search

## 저장소 구성  
Gradle (Kotlin DSL활용)이용한 멀티 프로젝트 구조로 구성되어 있다.

- data-fabric-platform
    - Libs
      - gRPC(Server, Client)
      - Configuration
    - Services
        - Core
        - Query
        - Storage-interface
        - Process-Manager
        - Monitoring
        - Metadata
    - Shared
      - ...
    
## Lib 설명

### gRPC

### Configuration

## Service 별 설명  
### Core
데이터 카탈로그를 위한 핵심 기능을 제공하는 서비스이다.
- 주요 기능
  - 검색
    - 사용자 검색 처리 : 저장소 인터페이스를 통해 요청 처리 
  - 원천 데이터 저장소 관리(추가, 수정, 삭제)
  - 원천 데이터 저장소 브라우징(탐색)
  - 데이터 관리(추가, 수정, 삭제)
    - 원천 데이터의 가상화(메타 데이터 추출)과 가상화(메타데이터)된 데이터의 관리  
  - 데이터 브라우징(탐색)
  - 서브 프로세스 연동
    - 메타 데이터 추출  
    - 모니터링
    - 사용자 쿼리

### Query
- 주요 기능
  - 사용자 쿼리 처리

### Storage Interface
- 주요 기능
  - Solr(검색엔진)
    - 사용자 검색 요청 처리를 위한 데이터 저장소 
  - Internal Storage(내부 저장소)
    - 데이터 패브릭 시스템의 내부 저장소

### Monitoring
- 주요 기능
  - Storage 상태 감시
  - Data 상태 감시

### Metadata
1차에서는 정적인 메타 데이터 추출을 수행한다. 이 후 
증강된 데이터 카탈로그(Augmented Data Catalog)를 위해 중요한 서비스이다.

- 주요 기능
  - 메타 데이터 추출

### Process Manager
반복적인 작업을 대량으로 수행하는 서비스들의 작업 관리와 더불어 작업 처리 속도에 따라
추가적인 컨테이너(프로세스)를 실행하고 관리한다.
- 주요 기능
  - 작업 관리(모니터링)
  - 프로세스(컨테이너) 관리

## 동작 설명 
### 사용자 요청과 요청 처리 흐름  
Client Request - (Using HTTP) - Spring Gateway - (Using gRPC) - Services 

### Debugging
MSA 구조에 따른 오류(로그) 분석 어려움을 Jaeger(예거) 활용한다.
Spring Cloud Gateway, Service 들에 적용하여 요청 처리 중 오류가 발생한 부분을 쉽게 파악할 수 있다.

### Logging



## 개발 가이드  

Gradle 을 활용한 멀티 프로젝트 구조로 빌드와 관련하여 공통된 부분을 처리하기 위해 프로젝트의 성격을 2가지로 분류하였습니다.
- Lib
- Service(Application)

각 프로젝트(모듈) 내부에서는 상위의 settings.gradle.kts에 의해 build-logic을 로드하고 
각 프로젝트(모듈)의 성격(lib, service)에 따라 설정과 의존성(dependencies)을 동일하게 적용받게 됩니다.

동일한 빌드 로직은 로그와 같이 광범위하게 사용되는 의존성 라이브러이의 변경을 용이하게 할 것이며,
신규 모듈(프로젝트)의 생성을 보다 신속하고 편리하게 할 것입니다.

추후 보다 발전된 형태의 빌드 로직 구축으로 이어져 정적 분석(코드 스타일, 보안, 유닛테스트 통합 업로드)과 
자동 배포(Maven Publish), 버전 관리를 수행할 수 있도록 할 것입니다.

### 빌드 로직 구성 
.
├── build-logic
│   └── build.gradle.kts        - build-logic을 위한 빌드 설정
│   └── settings.gradle.kts     - build-logic을 root로 하는 프로젝트 설정
│   └── src
│       └── main
│           └── kotlin
│               └── mobigen.java-application-conventions.gradle.kts            - application 공통 설정 
│               └── mobigen.java-common-conventions.gradle.kts                 - application, library 의 공통 설정 
│               └── mobigen.java-library-conventions.gradle.kts                - library 공통 설정 
├── libs
│   └── settings.gradle.kts     - libs를 root로 하는 프로젝트 설정, build-logic을 로드 (includeBuild)
│   └── list                    - 멀티 프로젝트 구조와 Gradle Plugin 공유 구조 설명을 위한 Sample 
├── services
│   └── settings.gradle.kts     - 여타 설정과 다르게 내부에서 사용되는 라이브러리 의존성 설정이 추가됨(libs, utilities)
│   └── sample                  - 멀티 프로젝트 구조와 Gradle Plugin 공유 구조 설명을 위한 Sample
└── utilities
    └── settings.gradle.kts
    └── list                    - 멀티 프로젝트 구조와 Gradle Plugin 공유 구조 설명을 위한 Sample

### CI/CD  
- SonarQube
    ```bash
    $./gradlew sonarqube \
        -Dsonar.projectKey=Data-Fabric-Platform \
        -Dsonar.host.url=https://sonarqube.iris.tools \
        -Dsonar.login=7f787dc2bb7f8aeb334537b0140c32c1a9eee8f9
    ```

    ```groovy
    plugins {
      id "org.sonarqube" version "3.0"
    }
    //...
    ```
- Dockerfile(Build)
- Jenkins
- Helm

## 배포