# Open VDAP Platform Core

## 개요

'IITP 분산된 데이터에 대한 논리적 데이터 통합과 복합분석을 지원하는 데이터 패브릭 기술 개발' 과제 수행을 통해 개발되었다.
데이터패브릭 기술을 적용한 서비스 명칭은 "Open VDAP" 이며, 본 저장소는 Open VDAP을 구성하는 요소 중 vdap-server 이다. 

## 기능

- 사용자 인증과 접근 제어 : Authentication / Authorization 
- 데이터 저장소 가상화
  - 등록 / 수정
    - 필터링 설정
      - 스케줄링
    - 샘플링 설정
      - 스케줄링
    - 프로파일링 설정
      - 스케줄링
  - 삭제
- 데이터 저장소 모니터링
  - 모니터링 설정
  - ...
- 데이터 탐색/검색
  - 데이터 저장소 레벨 탐색
  - 검색
- 데이터 카탈로그
  - 데이터 카테고리
  - 
- 표준메타데이터

## 스펙

- Java 21
- Gradle 8.12

## 구성  

```md
.
├── build-logic
│   └── build.gradle.kts        - build-logic을 위한 빌드 설정
│   └── settings.gradle.kts     - build-logic을 root로 하는 프로젝트 설정
│   └── src
│       └── main
│           └── kotlin
│               └── mobigen.java-common-conventions.gradle.kts                 - application, library 의 공통 설정
│               └── mobigen.java-application-conventions.gradle.kts            - application 공통 설정
│               └── mobigen.java-library-conventions.gradle.kts                - library 공통 설정
├── libs
│   └── settings.gradle.kts     - libs를 root로 하는 프로젝트 설정, build-logic을 로드 (includeBuild)
│   └── list                    - 멀티 프로젝트 구조와 Gradle Plugin 공유 구조 설명을 위한 Sample
│   └── list                    - 멀티 프로젝트 구조와 Gradle Plugin 공유 구조 설명을 위한 Sample
├── services
│   └── settings.gradle.kts     - 여타 설정과 다르게 내부에서 사용되는 라이브러리 의존성 설정이 추가됨(libs, utilities)
│   └── sample                  - 멀티 프로젝트 구조와 Gradle Plugin 공유 구조 설명을 위한 Sample
│   └── sample                  - 멀티 프로젝트 구조와 Gradle Plugin 공유 구조 설명을 위한 Sample
└── share
└── settings.gradle.kts
└── list                    - 멀티 프로젝트 구조와 Gradle Plugin 공유 구조 설명을 위한 Sample
└── list                    - 멀티 프로젝트 구조와 Gradle Plugin 공유 구조 설명을 위한 Sample
```

## Service 별 설명

## 동작 설명

### 사용자 요청과 요청 처리 흐름

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