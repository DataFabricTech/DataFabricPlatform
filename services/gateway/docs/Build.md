# Build & Run

## 개요  
---
Golang의 빌드는 간단하게 go build 명령으로 쉽게 처리할 수 있다. 
하지만 추가적인 기능을 필요로 할 수 있다. 이 문서에서는 Makefile을 
이용해 코드 정적 분석, 코드 스타일, 빌드 타임 변수 설정에 대해서 설명한다.  
또한, 실행 테스트를 할 수 있도록 설정되어 있는 명령에 대해서 설명한다.  

## Makefile 
---
Makefile에 사용자가 매번 확인하고 수정해줘야 하는 부분에 대해서 설명한다.  
```sh
## 바이너리 이름이자 이미지의 이름이다.  
TARGET := test
## 버전 정보  
VERSION := v1.0.0
## 컨테이너 생성 시 최종 이미지 이름  
IMAGE := repo.iris.tools/iris/$(TARGET):$(VERSION)
```
위와 같이 설정된 정보는 빌드 타임 변수로 바이너리에 설정되어 
바이너리 자체로 버전 정보를 확인할 수 있도록 해 준다.  
(ldflags -X 옵션으로 변수 설정)  

## Command( Build )  
---
- Check-Style  
코드 정적 분석과 함께 패키지를 점검한다.  
```bash
$ make check-style
```

- Build  
바이너리 빌드  
```bash
$ make dist
```

## Output  
바이너리는 build/bin 디렉토리에 생성된다.  

## Config  
기본 Configuration 정보 
```yaml
log:
  output: "stdout"          # stdout, file
  level: "debug"            # debug, info, warn, error
  # savePath: "logs"        # 로그 저장 위치 ($HOME 기준)
  # sizePerFileMb: 50       # 로그 파일 당 최대 크기(Mb)
  # maxOfDay: 100           # 하루 최대 생성 로그 파일 수
  # maxAge: 7               # 로그 파일 보관 기간  
  # compress: true          # 로그 파일 보관 시 압축 여부 
datastore:
  database: "sqlite3"       # mysql, postgres, sqlite3
  endPoint:
    path: "db/store.db"     # $HOME 기준 DB위치 정보(sqlite3)
    # host: "1.2.3.4"
    # port: 80
    # user: "database user"
    # pass: "database password of user"
    # dbName: "database name"
    # option: "database connect option"
  connPool:
    maxIdleConns: 1         # sqlite3 일 경우 의미 없음 
    maxOpenConns: 2         # slqite3 일 경우 의미 없음  
  debug:
    logLevel: "info"        # silent, error, warn, info
    slowThreshold : "1sec"  # 1min(minute), 1sec(second), 1ms(1millisecond)
server:
  debug: true               # http framework(echo) log level
  host: "0.0.0.0"           # http listen address
  port: 8080                # http listen port
```
config file은 $HOME 기준 configs 디렉토리이며, 파일 이름은 
$PROFILE 환경 변수의 이름과 동일해야 한다.  
*default : prod*
자신이 원하는 옵션으로 config파일을 설정하고, 다음 단락을 진행한다.  

## Run  
config file을 작성했다면, Makefile 을 수정하고 실행할 수 있다.  
```sh
# 상대 경로가 아닌 절대 경로 정보를 입력한다.  
# 다음은 make 명령 실행 위치를 $HOME으로 설정한다.  
HOME = $(shell pwd)
# config 파일 이름으로 설정한다.  
PROFILE = "prod"
```
- 실행   
```bash
$ make run
```

- 확인  
설정된 이름과 버전, build hash 값으로 실행된 것을 확인할 수 있다.  
```log
2021-07-30 13:41:30.798 [ERRO] [main.go          :  44] HOME : /home/jblim/workspace/golang-web-template
2021-07-30 13:41:30.798 [ERRO] [main.go          :  56] PROFILE : prod
2021-07-30 13:41:30.798 [ERRO] [main.go          :  82] [ Env ] Read ...................................................................... [ OK ]
2021-07-30 13:41:30.799 [ERRO] [main.go          : 107] [ Configuration ] Read ............................................................ [ OK ]
2021-07-30 13:41:30.799 [ERRO] [logger.go        :  87] ==========================================================================================
2021-07-30 13:41:30.799 [ERRO] [logger.go        :  88]
2021-07-30 13:41:30.799 [ERRO] [logger.go        :  89]                          START. TEST:v1.0.0-be1019c
2021-07-30 13:41:30.799 [ERRO] [logger.go        :  91]
2021-07-30 13:41:30.799 [ERRO] [logger.go        :  92]                                                   Copyright(C) 2021 Mobigen Corporation.
2021-07-30 13:41:30.799 [ERRO] [logger.go        :  93]
2021-07-30 13:41:30.799 [ERRO] [logger.go        :  94] ==========================================================================================
```

