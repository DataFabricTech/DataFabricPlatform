# RabbitMQ

## Install

### Docker

- port 5672 : exchange 연동
- port 15672 : rabbitmq UI

```bash
docker run -d \
    --hostname my-rabbit \
    --name some-rabbit \
    -p 5672:5672 \
    -p 15672:15672 \
    -e RABBITMQ_DEFAULT_USER=admin \
    -e RABBITMQ_DEFAULT_PASS=admin123 \
    rabbitmq:3-management
```
