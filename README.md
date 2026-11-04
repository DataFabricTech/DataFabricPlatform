# Open VDAP Platform Core

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
![Java](https://img.shields.io/badge/Java-21-orange)
![Gradle](https://img.shields.io/badge/Gradle-8.12-02303A)

## 개요

'분산된 데이터에 대한 논리적 데이터 통합과 복합분석을 지원하는 데이터 패브릭 기술개발' 과제(RS-2023-00222191) 수행을 통해 개발되었다.

데이터패브릭 기술을 적용한 서비스 명칭은 **Open VDAP**이며, 본 저장소는 Open VDAP을 구성하는 요소 중 **vdap-server**이다.

## 기능

- **사용자 인증과 접근 제어** — Authentication / Authorization
- **데이터 저장소 가상화**
  - 등록 / 수정 / 삭제
  - 필터링·샘플링·프로파일링 설정과 스케줄링
- **데이터 저장소 모니터링**
- **데이터 탐색/검색** — 저장소 레벨 탐색, 검색
- **데이터 카탈로그** — 데이터 카테고리
- **표준메타데이터**
- **데이터 품질 지표 생성** → [profiling/](profiling/)

## 스펙

| 항목 | 버전 |
|---|---|
| Java | 21 |
| Gradle | 8.12 |

`profiling/`은 **Python 모듈**이며 Gradle 빌드 범위에 포함되지 않는다. 설치와 실행은 [profiling/README.md](profiling/README.md)를 참고한다.

## 구성

```
.
├── build-logic/            빌드 공통 설정 (Kotlin DSL)
│   ├── commons/              application·library 공통 설정
│   ├── java-library/         library 설정
│   ├── spring-boot/          Spring Boot application 설정
│   └── report-aggregation/   리포트 집계 설정
├── libs/                   내부 라이브러리
│   ├── common/
│   ├── encrypt/
│   └── rabbitmq/
├── share/                  서비스 간 공유 모듈
│   ├── schema/               JSON 스키마와 생성 모델 (650파일)
│   └── annotator/
├── platforms/              의존성 버전 플랫폼
│   ├── product-platform/
│   ├── plugins-platform/
│   └── test-platform/
├── services/               애플리케이션
│   ├── vdap-server/          플랫폼 코어 API (74파일)
│   ├── core/                 (69파일)
│   ├── dataRelationshipTrigger/  관계 분석 트리거 (61파일)
│   └── new-spring-boot-sample/   신규 서비스 생성용 샘플
├── aggregation/            테스트 커버리지 집계
├── profiling/              데이터 품질 지표 생성 (Python)
└── docker/
```

각 디렉토리의 `settings.gradle.kts`가 `includeBuild`로 `build-logic`을 로드한다. 루트 `settings.gradle.kts`는 `platforms`·`build-logic`·`aggregation`·`libs`·`share`·`services` 6개를 등록한다.

## Service 별 설명

| 서비스 | 역할 |
|---|---|
| `vdap-server` | 플랫폼 코어 API. 인증·인가, 저장소 가상화, 태그, 관계, 시크릿 관리 |
| `core` | 공통 처리 |
| `dataRelationshipTrigger` | 데이터 관계 분석 트리거 |
| `new-spring-boot-sample` | 신규 Spring Boot 서비스 생성용 템플릿 |

## 개발 가이드

Gradle 멀티 프로젝트 구조로, 빌드 공통 부분을 처리하기 위해 프로젝트 성격을 두 가지로 분류한다.

- **Lib** — 내부 라이브러리
- **Service (Application)** — 실행 가능한 애플리케이션

각 프로젝트(모듈) 내부에서는 상위의 `settings.gradle.kts`에 의해 `build-logic`을 로드하고, 모듈 성격(lib·service)에 따라 설정과 의존성을 동일하게 적용받는다.

동일한 빌드 로직은 로그와 같이 광범위하게 사용되는 의존 라이브러리의 변경을 용이하게 하며, 신규 모듈 생성을 신속하게 한다.

### Debugging

MSA 구조에 따른 오류(로그) 분석 어려움을 **Jaeger**로 보완한다. Spring Cloud Gateway와 각 Service에 적용하여 요청 처리 중 오류가 발생한 지점을 파악할 수 있다.

### CI/CD

- **SonarQube** — 정적 분석

  ```bash
  ./gradlew sonarqube \
      -Dsonar.projectKey=Data-Fabric-Platform \
      -Dsonar.host.url=$SONAR_HOST_URL \
      -Dsonar.login=$SONAR_TOKEN
  ```

  ```groovy
  plugins {
    id "org.sonarqube" version "3.0"
  }
  ```

  인증 정보는 환경변수로 전달한다. **저장소에 기록하지 않는다.**

- Dockerfile (Build)
- Jenkins
- Helm

## 라이선스

[Apache-2.0](LICENSE)

인증 정보와 개인 환경 설정은 저장소에 등록하지 않는다.

---

관련 저장소: [DataFabricVisual](https://github.com/DataFabricTech/DataFabricVisual) · [Recommender](https://github.com/DataFabricTech/Recommender) · [ingestion](https://github.com/DataFabricTech/ingestion)
