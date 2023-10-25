
THIS_PKG_NAME=github.com/datafabric/gateway
OUT_PATH=proto

protoc --proto_path=./proto \
  --go_out=${OUT_PATH}/datamodel --go_opt=paths=source_relative \
  --go-grpc_out=${OUT_PATH}/datamodel --go-grpc_opt=paths=source_relative \
  --go_opt=Mdatamodel.proto=${THIS_PKG_NAME}/${OUT_PATH}/datamodel \
  hello.proto
