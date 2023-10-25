#!/bin/bash

# Protobuf generation for Golang

# Check Go installation
if ! command -v go &> /dev/null
then
    echo "go could not be found"
    echo "Need install golang"
    exit 1
fi

# Check protoc installation
if ! command -v protoc &> /dev/null
then
    echo "protoc could not be found"
    echo "apt install -y protobuf-compiler"
    echo "brew install protobuf"
    exit 1
fi

# Check protoc-gen-go installation
if ! command -v protoc-gen-go &> /dev/null
then
    echo "protoc-gen-go could not be found"
    echo "install cmd : go install google.golang.org/protobuf/cmd/protoc-gen-go@latest"
    exit 1
fi

# Get Current Path
current_path=$(pwd)
# Get Protofile From proto folder
proto_files=$(find "$current_path"/src/main/proto -name "*.proto")

# Loop proto_files
for proto_file in $proto_files
do
  # print proto_file
  echo "Generate Source From Proto[ $proto_file ]"

  # run protoc command for golang
  protoc --proto_path="$current_path"/src/main/proto \
    --go_out="$current_path"/src/main/golang \
    --go_opt=paths=source_relative \
    --go-grpc_out="$current_path"/src/main/golang \
    --go-grpc_opt=paths=source_relative \
    "$proto_file"

  # command run check success or error ?
  # shellcheck disable=SC2181
  if [ $? -eq 0 ]; then
    echo "Success"
  else
    echo "Error"
    exit 1
  fi
done
