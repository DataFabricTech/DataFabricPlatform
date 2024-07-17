# Implements

## Implements

- Executor 에서 spark, trino 선택적 로딩 기능
- MQ 로 보내는 메세지의 명령 추가 (create, load, kill)
- job 상태 업데이트 기능
- sql parser 개발 및 연결
- 알람 기능
- 모니터링 UI

## Bugs

- Executor 에서 RabbitMQ 메세지에 대한 ack 가 안되는 문제
- 설치된 trino 프로세스가 간헐적으로 죽는 문제
