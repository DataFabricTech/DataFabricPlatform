# 데이터패브릭 - 데이터 카탈로그

## 개요

우리는 카탈로그를 주변에서 쉽게 마주합니다. 마트의 제품 할인 정보와 판매 중인 상품에 대한 정보가 담긴 카탈로그,
자동차 판매를 목적으로 자동차 외부, 내부, 엔진의 스펙 등 다양한 정보가 담긴 카탈로그 같은 것들을 말이죠.

데이터 패브릭에서 데이터 카탈로그는 자동차 카탈로그와 유사합니다.
하나의 데이터에 대해 이름, 데이터의 구조, 데이터 샘플, 데이터 관계, 데이터의 특성 등 다양항 데이터(메타데이터)를 수집하고 관리합니다.
이를 통해 사용자는 데이터 탐색 시 보다 쉽게 찾고자 하는 데이터를 사용할 수 있는 이점이 생깁니다.

## 데이터 가상화

데이터 가상화는 다음과 같은 절차를 거쳐 이루어집니다.

1. 저장소(다양한 저장소 - MySQL, MariaDB, PostgreSQL, S3, MinIO, Hadoop 등)의 가상화  
    1. 사용자로부터 저장소 정보를 입력 받아 저장  
      - 연결정보 : IP, Port  
      - 인증정보 : ID, Password  
      - 가상화 대상 정보 : Database(include/exclude), Bucket(include/exclude)  
2. 데이터 가상화 - 데이터 정보 수집
  메타데이터 단락의 자동 수집 데이터를 참고
3. 검색 엔진 적재

## 메타데이터

다음은 데이터패브릭의 메타데이터 구성입니다.

| DataName:Depth - 1 | Depth - 2   | Depth - 3 | 데이터타입       | 설명                                                                         | 자동 수집 가능 여부 |
| ------------------ | ----------- | --------- | ---------------- | ---------------------------------------------------------------------------- | :-----------------: |
| ID                 | -           | -         | UUID             | 자원 식별 고유 아이디(OpenVDAP 부여 아이디)                                  |          o          |
| Name               | -           | -         | String           | 자원에 부여된 이름 ex : TableName, FileName, DatasetName                     |          o          |
| Description        | -           | -         | String           | 자원에 설정된 설명 혹은 OpenVDAP 에서 사용자가 입력한 설명                   |          △          |
| Subject(주제)      | -           | -         | String           | 데이터의 주제 혹은 데이터의 내용을 설명하는 키워드 혹은 구 (phrases)         |          △          |
| Version            | -           | -         | Double           | 자원(실데이터)의 변경 or 사용자에 의한 데이터 변경에 따라 증가하는 버전 정보 |          -          |
| Creator(생산자)    | -           | -         | String           | 데이터를 생성한 개체 정보(기관 혹은 개인 식별 정보)                          |          -          |
| LifeCycle          | -           | -         | -                | 데이터의 생성, 변경, 유효기간, 이용가능기한 정보                             |          -          |
| -                  | Created     | -         | DateTime         | 생성일                                                                       |          o          |
| -                  | Modified    | -         | DateTime         | 마지막 변경 시간                                                             |          o          |
| -                  | Valid       | -         | DateTime         | 유효 기간                                                                    |          x          |
| -                  | Available   | -         | DateTime         | 이용 가능 기간                                                               |          x          |
| Language           | -           | -         | String           | 데이터에 사용된 언어                                                         |          o          |
| TagLabels          | -           | -         | Taglabel[]       | 데이터 관련 키워드/사전 정보                                                 |          -          |
| -                  | id          | -         | UUID             | 데이터 관련 키워드/사전 정보                                                 |          -          |
| -                  | name        | -         | String           | 태그/사전 이름                                                               |          -          |
| -                  | description | -         | String           | 태그/사전 설명                                                               |          -          |
| -                  | source      | -         | String           | 태그/사전의 부모 개체 타입 ex : Classification/Glossary/GlossaryTerms        |          -          |
| -                  | sourceId    | -         | String           | 태그/사전의 부모 개체 아이디                                                 |          -          |
| DataType           | -           | -         | String           | 데이터의 유형 ex: Dataset/Structured/Unstructured/Semi-Structured            |          o          |
| DataSize           | -           | -         | Long             | 테이블, 파일 데이터의 크기(Byte)                                             |          o          |
| TableType          | -           | -         | String           | 테이블 데이터의 유형(Regular, View, ...) Regular, View                       |          o          |
| FileFormat         | -           | -         | String           | 파일 데이터의 유형 ex : CSV, DOC, JPG, ...                                   |          o          |
| DataStoreType      | -           | -         | String           | 자원 저장소 타입( Mysql, Mariadb, Mssql, Postgresql, ...)                    |          o          |
| DataStore          | -           | -         | ReferenceModel   | 자원 저장소 정보                                                             |          -          |
| -                  | source      | -         | String           | 타입 (DataStore, Database, DatabaseSchema, Bucket, Folder)                   |          o          |
| -                  | id          | -         | UUID             | DataStore 의 아이디                                                          |          o          |
| -                  | name        | -         | String           | DataStore 의 이름                                                            |          o          |
| -                  | description | -         | String           | DataStore 의 설명                                                            |          o          |
| Database           | -           | -         | ReferenceModel   | 데이터베이스 정보                                                            |          -          |
| -                  | source      | -         | String           | 타입 (DataStore, Database, DatabaseSchema, Bucket, Folder)                   |          o          |
| -                  | id          | -         | UUID             | Database 의 아이디                                                           |          o          |
| -                  | name        | -         | String           | Database 의 이름                                                             |          o          |
| -                  | description | -         | String           | Database 의 설명                                                             |          o          |
| DatabaseSchema     | -           | -         | ReferenceModel   | 데이터베이스 스키마 정보                                                     |          -          |
| -                  | source      | -         | String           | 타입 (DataStore, Database, DatabaseSchema, Bucket, Folder)                   |          o          |
| -                  | id          | -         | UUID             | DatabaseSchema 의 아이디                                                     |          o          |
| -                  | name        | -         | String           | DatabaseSchema 의 이름                                                       |          o          |
| -                  | description | -         | String           | DatabaseSchema 의 설명                                                       |          o          |
| Bucket             | -           | -         | ReferenceModel   | S3, MinIO 의 저장소 버켓 정보                                                |          -          |
| -                  | source      | -         | String           | 타입 (DataStore, Database, DatabaseSchema, Bucket, Folder)                   |          o          |
| -                  | id          | -         | UUID             | Bucket 의 아이디                                                             |          o          |
| -                  | name        | -         | String           | Bucket 의 이름                                                               |          o          |
| -                  | description | -         | String           | Bucket 의 설명                                                               |          o          |
| -                  | href        | -         | String           | Bucket  링크 URI                                                             |          o          |
| Location           | -           | -         | String           | 데이터 위치 정보 : Service - database - databaseschema - Table               |          o          |
| LocationPath       | -           | -         | String           | 데이터 위치 정보 : Path                                                      |
| Parent             | -           | -         | ReferenceModel[] | 상위 데이터모델(데이터 셋에 포함된 모델일 경우 데이터 셋) 정보               |          o          |
| Children           | -           | -         | ReferenceModel[] | 하위 데이터모델(데이터셋에 포함된 데이터 모델) 정보                          |          o          |

**테이블 데이터 상세**  

| DataName:Depth - 1 | Depth - 2             | Depth - 3                       | 데이터타입         | 설명                                                                   | 자동 수집 가능 여부 |
| ------------------ | --------------------- | ------------------------------- | ------------------ | ---------------------------------------------------------------------- | :-----------------: |
| TableProfile       | -                     | -                               | TableProfile       | 테이블 데이터 프로파일                                                 |          o          |
| -                  | SchemaDefinition      | -                               | String             | 테이블 DDL 쿼리                                                        |          o          |
| -                  | TableConstraints      | -                               | TableConstraints   | 테이블 제약사항                                                        |          -          |
| -                  | -                     | ConstraintType                  | String             | 제약사항 (UNIQUE, PK, FK, SORT)                                        |          o          |
| -                  | -                     | Columns                         | String[]           | 컬럼                                                                   |          o          |
| -                  | -                     | ReferredColumns                 | String[]           | 레퍼런스 컬럼 정보(DataID.ColumnName)                                  |          o          |
| -                  | TableProfileConfig    | -                               | TableProfileConfig | 테이블 프로파일을 위한 설정                                            |          -          |
| -                  | -                     | profileSampleType               | String             | 프로파일 작성을 위한 샘플 데이터 수집 방식                             |          -          |
| -                  | -                     | profileSample                   | Long               | 프로파일 작성을 위한 샘플 데이터의 크기                                |          -          |
| -                  | -                     | excludeColumns                  | String[]           | 프로파일 작성에서 제외할 컬럼들                                        |          -          |
| -                  | -                     | includeColumns                  | String[]           | 프로파일 작성에서 포함할 컬럼들                                        |          -          |
| -                  | -                     | computeTableMetrics             | Boolean            | 프로파일 작성을 위한 샘플 데이터의 크기                                |          o          |
| -                  | -                     | computeColumnMetrics            | Boolean            | 프로파일 작성을 위한 샘플 데이터의 크기                                |          o          |
| -                  | -                     | samplingMethodType              | String             | 샘플 데이터 수집 방식                                                  |          -          |
| -                  | -                     | sampleDataCount                 | Long               | 샘플 데이터의 크기                                                     |          -          |
| -                  | -                     | profileQuery                    | String             | 프로파일 작성과 샘플 수집에 사용할 사용자 정의 SQL                     |          o          |
| -                  | TableStatistics(통계) | -                               | TableStatistics    | 테이블 데이터의 통계 정보                                              |          -          |
| -                  | -                     | Timestamp                       | DateTime           | 테이블 통게 수집 시간                                                  |          o          |
| -                  | -                     | ProfileSampleType               | String             | 테이블 통계 작성에 사용된 샘플 데이터 수집 크기 종류 Percentage, Line  |          o          |
| -                  | -                     | ProfileSample                   | Integer            | 테이블 통계 작성에 사용된 샘플 데이터의 크기                           |          o          |
| -                  | -                     | SamplingMethodType              | String             | 테이블 통계 작성에 사용된 샘플 데이터의 수집 방식  System, Random, ... |          o          |
| -                  | -                     | ColumnCount                     | Integer            | 테이블의 컬럼 수                                                       |          o          |
| -                  | -                     | RowCount                        | Integer            | 테이블 라인 수                                                         |          o          |
| -                  | -                     | Size                            | Integer            | 테이블 데이터의 크기(byte)                                             |          o          |
| -                  | -                     | CreateDateTime                  | DateTime           | 테이블 생성 시간                                                       |          o          |
| -                  | Columns (컬럼 정보)   | -                               | Columns            | 테이블의 컬럼 구조를 나타내는 메타 정보                                |          -          |
| -                  | -                     | Name (컬럼 이름)                | String             | 컬럼의 식별자 이름                                                     |          o          |
| -                  | -                     | DisplayName (표시 이름)         | String             | 화면 또는 UI 상에 노출되는 컬럼 이름                                   |          o          |
| -                  | -                     | DataType (데이터 타입)          | String             | INT, FLOAT, DATETIME, TEXT 등 컬럼의 데이터 타입                       |          o          |
| -                  | -                     | DataLength (데이터 길이)        | Integer            | 문자열, 숫자 등의 길이 또는 크기                                       |          o          |
| -                  | -                     | Precision (정밀도)              | Integer            | 숫자 타입 전체 유효 자릿수 (예: 소수점 포함 총 자리 수)                |          o          |
| -                  | -                     | Scale (소수 자릿수)             | Integer            | 소수점 이하 자릿수 (예: 2 → 0.01 단위까지)                             |          o          |
| -                  | -                     | Description (컬럼 설명)         | String             | 컬럼에 대한 부가 설명                                                  |          o          |
| -                  | -                     | IsPrivate (개인정보 여부)       | Boolean            | 해당 컬럼이 개인정보인지 여부 (true/false)                             |          o          |
| -                  | -                     | Tags (태그)                     | TagLabels          | 컬럼에 부여된 태그 혹은 도메인 사전 정보                               |          o          |
| -                  | -                     | Constraint (제약 조건)          | String             | NULL, NOT_NULL, UNIQUE, PK 등 제약조건 정의                            |          o          |
| -                  | -                     | OrdinalPosition (순서)          | Integer            | 컬럼이 테이블에서 나타나는 순서                                        |          o          |
| -                  | -                     | Profile (통계 정보)             | ColumnProfile      | 컬럼에 대한 통계 기반 프로파일 정보                                    |          -          |
| -                  | -                     | - Name (컬럼 이름)              | String             | 프로파일 대상 컬럼 이름                                                |          o          |
| -                  | -                     | - Timestamp (분석 시각)         | DateTime           | 프로파일링 실행 시각                                                   |          o          |
| -                  | -                     | - ValueCount (데이터 수)        | Long               | 해당 컬럼에 존재하는 값의 개수                                         |          o          |
| -                  | -                     | - ValuePercentage (데이터 비율) | Float(%)           | 전체 대비 데이터 존재 비율 (0~100%)                                    |          o          |
| -                  | -                     | - ValidCount (유효 데이터 수)   | Long               | 유효한 값의 수 (예: 포맷 오류 없는 값들)                               |          o          |
| -                  | -                     | - DuplicateCount (중복 수)      | Long               | 중복된 값들의 개수                                                     |          o          |
| -                  | -                     | - NullCount (NULL 수)           | Long               | NULL 값의 개수                                                         |          o          |
| -                  | -                     | - MissingPercentage (결측률)    | Float(%)           | NULL/미싱값 비율 (0~100%)                                              |          o          |
| -                  | -                     | - UniqueCount (고유 수)         | Long               | 고유한 값의 수                                                         |          o          |
| -                  | -                     | - DistinctCount (중복제거 수)   | Long               | 중복 제거 후의 유일한 값 개수                                          |          o          |
| -                  | -                     | - Min (최소값)                  | String             | 최소 값                                                                |          o          |
| -                  | -                     | - Max (최대값)                  | String             | 최대 값                                                                |          o          |
| -                  | -                     | - MinLength (최소 길이)         | Long               | 문자열 혹은 리스트 값의 최소 길이                                      |          o          |
| -                  | -                     | - MaxLength (최대 길이)         | Long               | 문자열 혹은 리스트 값의 최대 길이                                      |          o          |
| -                  | -                     | - Mean (평균)                   | Float              | 수치 데이터의 평균값                                                   |          o          |
| -                  | -                     | - Sum (합계)                    | Float              | 수치 데이터 총합                                                       |          o          |
| -                  | -                     | - Stddev (표준편차)             | Float              | 수치 데이터의 표준편차                                                 |          o          |
| -                  | -                     | - Variance (분산)               | Float              | 데이터의 분산                                                          |          o          |
| -                  | -                     | - Median (중앙값)               | Float              | 중앙값                                                                 |          o          |
| -                  | -                     | - FirstQuartile (1사분위수)     | Float              | 하위 25%에 해당하는 값                                                 |          o          |
| -                  | -                     | - ThirdQuartile (3사분위수)     | Float              | 상위 25% 경계값 (75% 지점)                                             |          o          |
| -                  | -                     | - InterQuartileRange (IQR)      | Float              | 사분위수 범위 = Q3 - Q1                                                |          o          |
| -                  | -                     | - NonParametricSkew (비대칭도)  | Float              | 비모수적 왜도 지표                                                     |          o          |
| -                  | -                     | - Histogram (히스토그램)        | Object/Chart       | 값의 분포 정보를 담은 히스토그램                                       |          o          |
| TableSample        | -                     | -                               | TableSample        | 테이블 샘플 데이터                                                     |          o          |
| -                  | Columns               | -                               | String[]           | 컬럼 이름 리스트                                                       |          o          |
| -                  | Rows                  | -                               | Object[][]         | 테이블 데이터 로우 리스트                                              |          o          |

**문서 타입 데이터 상세**  

| DataName:Depth - 1 | Depth - 2      | Depth - 3 | 데이터타입      | 설명                                                | 자동 수집 가능 여부 |
| ------------------ | -------------- | --------- | --------------- | --------------------------------------------------- | :-----------------: |
| DocumentProfile    | -              | -         | DocumentProfile | 문서 타입 데이터의 메타데이터 정보                  |          -          |
| -                  | MLModelInfo    | -         | String          | 문서 데이터의 메타데이터 수집에 사용된 ML 모델 정보 |          o          |
| -                  | Title          | -         | String          | 제목                                                |          o          |
| -                  | Author         | -         | String          | 작성자                                              |          o          |
| -                  | Company        | -         | String          | 회사                                                |          o          |
| -                  | Abstractive    | -         | String          | 문서 요약                                           |          o          |
| -                  | Keywords       | -         | String          | 주요 키워드                                         |          o          |
| -                  | Domain         | -         | String          | 문서 카테고리(금융, 법률, 의료, 기술 등)            |          o          |
| -                  | LastModifiedBy | -         | String          | 마지막 저장 사용자 정보                             |          o          |
| -                  | Created        | -         | DateTime        | 작성일                                              |          o          |
| -                  | Modified       | -         | DateTime        | 수정일                                              |          o          |
| -                  | PageCount      | -         | Integer         | 페이지 수                                           |          o          |
| -                  | WordCount      | -         | Integer         | 단어 수                                             |          o          |
| DocumentSample     | -              | -         | String          | 문서 데이터의 샘플                                  |          o          |

**사진 타입 데이터 상세**  

| DataName:Depth - 1             | Depth - 2                   | Depth - 3 | 데이터타입         | 설명                                                 | 자동 수집 가능 여부 |
| ------------------------------ | --------------------------- | --------- | ------------------ | ---------------------------------------------------- | :-----------------: |
| PictureProfile (사진 프로파일) | -                           | -         | PictureProfile     | 사진 데이터의 프로파일                               |          -          |
| -                              | MLModelInfo (ML 모델 정보)  | -         | String             | 사진 메타데이터 수집에 사용된 머신러닝 모델 정보     |          o          |
| -                              | Camera (카메라 정보)        | -         | String             | 촬영에 사용된 카메라 기종                            |          o          |
| -                              | PictureDateTime (촬영일시)  | -         | DateTime           | 사진이 촬영된 날짜 및 시간                           |          o          |
| -                              | ShutterSpeed (셔터속도)     | -         | String             | 사진 촬영 시 사용된 셔터 속도                        |          o          |
| -                              | Aperture (조리개값)         | -         | String             | 조리개 수치 (F 값)                                   |          o          |
| -                              | ISO (ISO 감도)              | -         | Integer            | 촬영 시 설정된 ISO 감도 값                           |          o          |
| -                              | FocalLength (초점거리)      | -         | Integer            | 렌즈 초점 거리 (mm)                                  |          o          |
| -                              | WhiteBalance (화이트밸런스) | -         | String (또는 Enum) | 자동 / 수동 등 화이트 밸런스 설정                    |          o          |
| -                              | Flash (플래시 사용 여부)    | -         | Boolean/String     | 플래시 사용 여부 (사용 / 미사용 등)                  |          o          |
| -                              | Latitude (위도)             | -         | String             | GPS 위도 좌표                                        |          o          |
| -                              | Longitude (경도)            | -         | String             | GPS 경도 좌표                                        |          o          |
| -                              | Accuracy (정확도)           | -         | String             | GPS 정확도 값                                        |          o          |
| -                              | EditorApp (편집 소프트웨어) | -         | String             | 사진을 편집한 애플리케이션 또는 소프트웨어 이름      |          o          |
| -                              | AverageColor (평균색상)     | -         | String             | 사진의 전체 평균 색상                                |          o          |
| -                              | DominantColors (주요색상)   | -         | String[]           | 주요 색상 목록                                       |          o          |
| -                              | Contrast (대비)             | -         | Float              | 이미지의 대비 정도                                   |          o          |
| -                              | Brightness (밝기)           | -         | Float              | 이미지의 밝기 값                                     |          o          |
| -                              | Saturation (채도)           | -         | Float              | 색상의 선명도 또는 채도 값                           |          o          |
| -                              | Resolution (해상도)         | -         | String             | 이미지 해상도 (예: 1920x1080)                        |          o          |
| -                              | PixelDensity (픽셀밀도)     | -         | Float              | 인치당 픽셀 수 (PPI)                                 |          o          |
| -                              | ObjectInfo (객체 정보)      | -         | String[]           | 인물, 차량, 동물, 사물 등 이미지 내 인식된 객체 정보 |          o          |
| -                              | LandscapeInfo (풍경 정보)   | -         | String[]           | 산, 바다, 도시, 실내 등 이미지 내 풍경 정보          |          o          |
| -                              | TextDetection (텍스트 검출) | -         | String             | 사진에 포함된 텍스트를 인식하고 변환한 내용          |          o          |
| PictureSample (사진 샘플)      | -                           | -         | Byte               | 사진 원본의 샘플 데이터 (Raw 또는 Thumbnail 등)      |          o          |

**음성(오디오) 타입 데이터 상세**  

| DataName:Depth - 1             | Depth - 2                        | Depth - 3  | 데이터타입         | 설명                                                     | 자동 수집 가능 여부 |
| ------------------------------ | -------------------------------- | ---------- | ------------------ | -------------------------------------------------------- | :-----------------: |
| AudioProfile (오디오 프로파일) | -                                | -          | AudioProfile       | 오디오 데이터의 프로파일                                 |          -          |
| -                              | MLModelInfo (ML 모델 정보)       | -          | String             | 메타데이터 추출에 사용된 머신러닝 모델명                 |          o          |
| -                              | Duration (재생시간)              | -          | Integer (ms)       | 오디오 전체 재생 시간 (밀리초 단위)                      |          o          |
| -                              | Speakers (발화자)                | -          | Speakers[]         | 화자 정보                                                |          -          |
| -                              | -                                | SpeakerID  | String             | 화자 구분 아이디                                         |          o          |
| -                              | SpeakerDiarization (발화자 구분) | -          | SpeakerDiarization | 서로 다른 화자들의 음성 영역 구분                        |          -          |
| -                              | -                                | SpeakerID  | String             | 화자 구분 아이디                                         |          o          |
| -                              | -                                | StartTime  | Long               | 시작 시간(밀리초) ex: 1초100 -> 1100                     |          o          |
| -                              | -                                | EndTime    | Long               | 끝 시간(밀리초)                                          |          o          |
| -                              | -                                | Transcript | String             | 음성 -> 텍스트                                           |          o          |
| -                              | EmotionAnalysis (감정 분석)      | -          | String             | 주요 감정 상태 (예: 기쁨, 슬픔, 분노 등)                 |          o          |
| -                              | BackgroundNoise (배경 소음)      | -          | String             | 배경 환경의 소리 정보 (예: 자연, 거리 소음 등)           |          o          |
| -                              | STT (음성 → 텍스트)              | -          | String             | 음성을 텍스트로 변환한 전체 내용                         |          o          |
| -                              | Language (언어)                  | -          | String             | 사용된 언어 코드 (예: "ko", "en")                        |          o          |
| -                              | Dialect (방언)                   | -          | String             | 지역 방언 정보 (예: "경상도", "전라도")                  |          o          |
| -                              | Keywords (키워드)                | -          | String[]           | 오디오에서 추출된 주요 키워드 목록                       |          o          |
| -                              | NamedEntities (개체명)           | -          | String[]           | 사람, 장소, 기관, 날짜 등의 정보                         |          o          |
| -                              | Intent (의도)                    | -          | String             | 화자의 발화 의도 (예: 질문, 요청, 명령 등)               |          o          |
| -                              | Summary (요약)                   | -          | String             | 전체 오디오 내용의 요약 문장                             |          o          |
| AudioSample(오디오 샘플)       | -                                | -          | String             | 오디오 샘플(STT를 이용해 텍스트로 변환된 데이터 중 일부) |          -          |

## 데이터 적재 - 검색엔진  

한글 처리 토크나이저와 데이터 검색 성능 향상을 위한 필터(인덱싱)를 고려한 데이터 적재 설계

```json
{
  "settings": {
    "analysis": {
      "analyzer": {
        "fabric_analyzer": {
          "tokenizer": "nori_tokenizer",
          "filter": [
            "lowercase",
            "word_delimiter_filter",
            "om_stemmer"
          ]
        },
        "fabric_ngram": {
          "type": "custom",
          "tokenizer": "edge_ngram_tokenizer",
          "filter": ["lowercase"]
        }
      },
      "tokenizer": {
        "edge_ngram_tokenizer": {
          "type": "edge_ngram",
          "min_gram": 1,
          "max_gram": 20,
          "token_chars": ["letter", "digit", "hangul"]
        }
      },
      "filter": {
        "om_stemmer": {
          "type": "stemmer",
          "name": "kstem"
        },
        "word_delimiter_filter": {
          "type": "word_delimiter",
          "preserve_original": true
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": {
        "type": "text"
      },
      "name": {
        "type": "text",
        "analyzer": "fabric_analyzer",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256,
            "normalizer": "lowercase_normalizer"
          },
          "ngram": {
            "type": "text",
            "analyzer": "fabric_ngram"
          }
        }
      },
      "displayName": {
        "type": "text",
        "analyzer": "fabric_analyzer",
        "fields": {
          "keyword": {
            "type": "keyword",
            "normalizer": "lowercase_normalizer",
            "ignore_above": 256
          },
          "ngram": {
            "type": "text",
            "analyzer": "fabric_ngram"
          }
        }
      },
      "subject": {
        "type": "text",
        "analyzer": "fabric_analyzer",
        "index_options": "docs"
      },
      "description": {
        "type": "text",
        "analyzer": "fabric_analyzer",
        "index_options": "docs"
      },
      "version": {
        "type": "float"
      },
      "updatedAt": {
        "type": "date",
      },
      "updatedBy": {
        "type": "text"
      },
      "href": {
        "type": "text"
      },
      "fileFormat": {
        "type": "keyword",
        "normalizer": "lowercase_normalizer"
      },
      "tableType": {
        "type": "keyword",
        "normalizer": "lowercase_normalizer"
      },
      "tableProfile": {
        "schemaDefinition": {
          "type": "text"
        },
        "tableStatistics": {
          "properties": {
            "time": {
              "type": "date"
            },
            "columnCount": {
              "type": "float"
            },
            "rowCount": {
              "type": "float"
            },
            "size": {
              "type": "float"
            }
          }
        },
        "columns": {
          "properties": {
            "name": {
              "type": "text",
              "analyzer": "fabric_analyzer",
              "fields": {
                "keyword": {
                  "type": "keyword",
                  "ignore_above": 256,
                  "normalizer": "lowercase_normalizer"
                },
                "ngram": {
                  "type": "text",
                  "analyzer": "fabric_ngram"
                }
              }
            },
            "dataType": {
              "type": "text"
            },
            "dataTypeDisplay": {
              "type": "text"
            },
            "description": {
              "type": "text",
              "analyzer": "fabric_analyzer",
              "index_options": "docs"
            },
            "tags": {
              "properties": {
                "tagFQN": {
                  "type": "keyword",
                  "normalizer": "lowercase_normalizer"
                },
                "labelType": {
                  "type": "keyword"
                },
                "description": {
                  "type": "text",
                  "index_options": "docs"
                },
                "source": {
                  "type": "keyword"
                },
                "state": {
                  "type": "keyword"
                }
              }
            },
            "ordinalPosition": {
              "type": "integer"
            }
          }
        },
        "columnNames": {
          "type": "keyword"
        },
      },
      "databaseSchema": {
        "properties": {
          "id": {
            "type": "keyword",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 36
              }
            }
          },
          "type": {
            "type": "text"
          },
          "name": {
            "type": "keyword",
            "normalizer": "lowercase_normalizer",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "displayName": {
            "type": "keyword",
            "fields": {
              "keyword": {
                "type": "keyword",
                "normalizer": "lowercase_normalizer",
                "ignore_above": 256
              }
            }
          },
          "description": {
            "type": "text"
          },
          "deleted": {
            "type": "boolean"
          },
          "href": {
            "type": "text"
          }
        }
      },
      "database": {
        "properties": {
          "id": {
            "type": "keyword",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 36
              }
            }
          },
          "type": {
            "type": "keyword"
          },
          "name": {
            "type": "keyword",
            "normalizer": "lowercase_normalizer",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "displayName": {
            "type": "keyword",
            "fields": {
              "keyword": {
                "type": "keyword",
                "normalizer": "lowercase_normalizer",
                "ignore_above": 256
              }
            }
          },
          "description": {
            "type": "text"
          },
          "deleted": {
            "type": "boolean"
          },
          "href": {
            "type": "text"
          }
        }
      },
      "service": {
        "properties": {
          "id": {
            "type": "keyword",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 36
              }
            }
          },
          "type": {
            "type": "keyword"
          },
          "name": {
            "type": "keyword",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "displayName": {
            "type": "keyword",
            "fields": {
              "keyword": {
                "type": "keyword",
                "normalizer": "lowercase_normalizer",
                "ignore_above": 256
              }
            }
          },
          "description": {
            "type": "text"
          },
          "deleted": {
            "type": "boolean"
          },
          "href": {
            "type": "text"
          }
        }
      },
      "owners": {
        "properties": {
          "id": {
            "type": "keyword",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 36
              }
            }
          },
          "type": {
            "type": "keyword"
          },
          "name": {
            "type": "keyword",
            "normalizer": "lowercase_normalizer",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "displayName": {
            "type": "keyword",
            "fields": {
              "keyword": {
                "type": "keyword",
                "normalizer": "lowercase_normalizer",
                "ignore_above": 256
              }
            }
          },
          "description": {
            "type": "text"
          },
          "deleted": {
            "type": "boolean"
          },
          "href": {
            "type": "text"
          }
        }
      },
      "lifeCycle": {
        "type": "object"
      },
      "location": {
        "type": "text"
      },
      "locationPath": {
        "type": "keyword"
      },
      "deleted": {
        "type": "boolean"
      },
      "followers": {
        "type": "keyword"
      },
      "tags": {
        "properties": {
          "tagFQN": {
            "type": "keyword",
            "normalizer": "lowercase_normalizer"
          },
          "labelType": {
            "type": "keyword"
          },
          "description": {
            "type": "text"
          },
          "source": {
            "type": "keyword"
          },
          "state": {
            "type": "keyword"
          }
        }
      },
      "lineage": {
        "type" : "object"
      },
      "entityRelationship": {
        "type" : "object"
      },
      "serviceType": {
        "type": "keyword",
        "normalizer": "lowercase_normalizer"
      },
      "dataTypeType": {
        "type": "keyword"
      },
      "totalVotes": {
        "type": "long",
        "null_value": 0
      },
      "votes" : {
        "type": "object"
      }
    }
  }
}
```
