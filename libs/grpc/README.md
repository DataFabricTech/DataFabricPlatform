# gRPC Library

## 소개
Gateway 와 각 서비스들간의 통신과 서비스들 간 통신을 위한 gRPC 라이브러리입니다.

## 트러블슈팅
1. generateProto Error
```bash
xecution failed for task ':libs:grpc:generateProto'.
> protoc: stdout: . stderr: /Users/jblim/.gradle/caches/modules-2/files-2.1/io.grpc/protoc-gen-grpc-java/1.56.1/1b90759143cb250c73ac85ac83f096b308344ca4/protoc-gen-grpc-java-1.56.1-osx-aarch_64.exe: program not found or is not executable
  Please specify a program using absolute path or make sure the program is available in your PATH system variable
  --grpc_out: protoc-gen-grpc: Plugin failed with status code 1.


BUILD FAILED in 7s
13 actionable tasks: 1 executed, 12 up-to-date
```

Apple Silicon(M1, M2)을 사용으로 에러가 발생하는 것으로 
다음과 같이 Rosetta를 설치해야 합니다.

```bash
$ softwareupdate --install-rosetta --agree-to-license
```