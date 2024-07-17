# Trino

## Install

### Docker

- 아래 `<...>` 값 변경
    - `<Dophin Home>`

```bash
docker run --name trino -d \
    -p 8888:8888 \
    -v <Dophin Home>/config/trino:/etc/trino \
    trinodb/trino
```
