# PostgreSQL

## Install

### Docker

```bash
docker run -d \
  --name postgres \
  -p 5432:5432 \
  -e POSTGRES_PASSWORD=admin123 \
  -e PGDATA=/var/lib/postgresql/data/pgdata \
  postgres:16.0
```
