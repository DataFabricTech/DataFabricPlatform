# Hive

## Install

### Hive MetaStore Docker

#### Dependencies

- RDBMS (아래는 [postgresql](./PostgreSQL.md) 사용)
  - RDBMS 데몬
  - jdbc driver
- FileSystem (아래는 [MinIO](./MinIO.md) 사용)
  - 데몬
  - 연동을 위한 jar 파일
- Spark 3.x 연동을 위한 hadoop jar 파일

- 아래 `<...>` 값 변경 및 `config/hive-site.xml` 의 `<...>` 값 변경
  - `<Host IP>`
  - `<Dophin Home>`

```bash
# standalone 모드 메타스토어 실행
docker run --rm -d -p 9083:9083 --env SERVICE_NAME=metastore \
   --env DB_DRIVER=postgres \
   --env SCHEMA_COMMAND=upgradeSchema \
   --env SERVICE_OPTS="-Djavax.jdo.option.ConnectionDriverName=org.postgresql.Driver -Djavax.jdo.option.ConnectionURL=jdbc:postgresql://<Host IP>:5432/metastore_db -Djavax.jdo.option.ConnectionUserName=postgres -Djavax.jdo.option.ConnectionPassword=test" \
   --mount type=bind,source=<Dophin Home>/lib/postgresql-42.7.1.jar,target=/opt/hive/lib/postgres.jar \
   --mount type=bind,source=<Dophin Home>/lib/aws-java-sdk-core-1.12.657.jar,target=/opt/hive/lib/aws-java-sdk-core-1.12.657.jar \
   --mount type=bind,source=<Dophin Home>/lib/aws-java-sdk-s3-1.12.657.jar,target=/opt/hive/lib/aws-java-sdk-s3-1.12.657.jar \
   --mount type=bind,source=<Dophin Home>/lib/hadoop-client-3.3.6.jar,target=/opt/hive/lib/hadoop-client-3.3.6.jar \
   --mount type=bind,source=<Dophin Home>/lib/hadoop-common-3.3.6.jar,target=/opt/hive/lib/hadoop-common-3.3.6.jar \
   --mount type=bind,source=<Dophin Home>/lib/hadoop-aws-3.3.6.jar,target=/opt/hive/lib/hadoop-aws-3.3.6.jar \
   --mount type=bind,source=<Dophin Home>/lib/hadoop-auth-3.3.6.jar,target=/opt/hive/lib/hadoop-auth-3.3.6.jar \
   --mount type=bind,source=<Dophin Home>/lib/hadoop-shaded-guava-1.1.1.jar,target=/opt/hive/lib/hadoop-shaded-guava-1.1.1.jar \
   -v <Dophin Home>/config/hive-site.xml:/opt/hive/conf/hive-site.xml \
   --name metastore-standalone apache/hive:3.1.3
## db 에 이미 스키마가 생성된 경우 
docker run -d -p 9083:9083 --env SERVICE_NAME=metastore \
   --env DB_DRIVER=postgres \
   --env IS_RESUME="true" \
   --env SERVICE_OPTS="-Djavax.jdo.option.ConnectionDriverName=org.postgresql.Driver -Djavax.jdo.option.ConnectionURL=jdbc:postgresql://<Host IP>:5432/metastore_db -Djavax.jdo.option.ConnectionUserName=postgres -Djavax.jdo.option.ConnectionPassword=test" \
   --mount type=bind,source=<Dophin Home>/lib/postgresql-42.7.1.jar,target=/opt/hive/lib/postgres.jar \
   --mount type=bind,source=<Dophin Home>/lib/aws-java-sdk-core-1.12.657.jar,target=/opt/hive/lib/aws-java-sdk-core-1.12.657.jar \
   --mount type=bind,source=<Dophin Home>/lib/aws-java-sdk-s3-1.12.657.jar,target=/opt/hive/lib/aws-java-sdk-s3-1.12.657.jar \
   --mount type=bind,source=<Dophin Home>/lib/hadoop-client-3.3.6.jar,target=/opt/hive/lib/hadoop-client-3.3.6.jar \
   --mount type=bind,source=<Dophin Home>/lib/hadoop-common-3.3.6.jar,target=/opt/hive/lib/hadoop-common-3.3.6.jar \
   --mount type=bind,source=<Dophin Home>/lib/hadoop-aws-3.3.6.jar,target=/opt/hive/lib/hadoop-aws-3.3.6.jar \
   --mount type=bind,source=<Dophin Home>/lib/hadoop-auth-3.3.6.jar,target=/opt/hive/lib/hadoop-auth-3.3.6.jar \
   --mount type=bind,source=<Dophin Home>/lib/hadoop-shaded-guava-1.1.1.jar,target=/opt/hive/lib/hadoop-shaded-guava-1.1.1.jar \
   -v <Dophin Home>/config/hive-site.xml:/opt/hive/conf/hive-site.xml \
   --name metastore-standalone apache/hive:3.1.3
```
