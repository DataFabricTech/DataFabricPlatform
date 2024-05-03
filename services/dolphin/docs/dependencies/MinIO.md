# MinIO

## Install

### Docker

- 아래 `<...>` 값 변경
    - `<Dophin Home>`

```bash
docker run -d \
   -p 9000:9000 \
   -p 9001:9001 \
   -v <Dolphin Home>/minio-data:/data \
   -e "MINIO_ROOT_USER=admin" \
   -e "MINIO_ROOT_PASSWORD=admin123" \
   --name minio \
   quay.io/minio/minio server /data --console-address ":9001"
```
