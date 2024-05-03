# Dolphin Executor

작업 실행자 서비스

# 구성

- Spark 3.5.0
- spring boot
    - spring-boot-starter-data-jpa
    - spring-boot-starter-amqp
    - spring-boot-starter-logging
- Utils

# 기능

- MQ Consumer
    - MQ 에서 job 읽어서 처리
- SQL 실행
    - spark 를 이용해서 sql 실행
- Health check
    - DB 에 자신 상태를 없데이트 (value = 1)
- Update Job Status

# 연동 포인트

- Spark
    - Hive Metastore
    - Each DB
- DBMS
    - job 상태 업데이트 및 확인
    - worker health check
- MQ
    - RabbitMQ 사용 예상
    - Consumer 기능
- 결과 저장소
    - job 결과를 저장하고 path 업데이트 해야함
