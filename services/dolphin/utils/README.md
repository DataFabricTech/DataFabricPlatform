# Dolphin Manager

작업 매니저 서비스

# 구성

- spring boot
    - spring-boot-starter-web
    - spring-boot-starter-logging
    - spring-boot-starter-data-jpa
    - spring-boot-starter-amqp
- Utils

# 기능

- Query
    - job(sql) 실행
    - job 상태 확인
    - job 결과 확인
- Notification
    - SSE (Server-Sent Events) 방식 사용 예정
- Worker Management
    - worker health check

# 연동 포인트

- SqlParser
    - 요청 들어온 sql을 확인하는 parser 와 연동 필요
- DBMS
    - job 생성/상태 업데이트 및 확인
    - worker health check
- MQ
    - RabbitMQ 사용 예상
    - publisher 기능
- 결과 저장소
    - job 결과를 저장 해놓은 저장소에서 결과 읽어야 함
