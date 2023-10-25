# Swagger
API 문서화 자동화 Tool  
보다 정확한 내용은 다음의 링크를 통해 확인한다.  
[swaggo](https://github.com/swaggo/swag)  

## 개요  
---
API 문서를 코드 개발과 분리하여 처리하는 것이 아닌 
코드 개발 단계에서 미리 작성하고, 자동화된 툴을 이용해
외부에 공개하는 방식 

## 패키지 다운로드  
---
```bash
$ go get github.com/swaggo/swag/cmd/swag 
$ go get github.com/swaggo/echo-swagger
```

## 서버에 스웨커 문서 페이지 Path 추가  
---
Echo Framework를 사용한다면 다음과 같이 docs 디렉토리 import와 
echoSwagger.WarpHandler 추가가 필요하다.  
```go
import (
	_ "github.com/mobigen/golang-web-platform/docs"
	echoSwagger "github.com/swaggo/echo-swagger"
)

...
...
...

// swagger setting
e.GET("/swagger/*", echoSwagger.WrapHandler)
```

## 작성 방법  
---
- Title  
    Swagger 문서의 시작 부분을 main 함수에 추가한다. 
    * Option list
        [main.go](https://github.com/swaggo/swag/blob/master/example/celler/main.go)
    * Sample 
        main.go
        ```go
        // @title Golang Web Template API
        // @version 1.0.0
        // @description This is a golang web template server.

        // @contact.name API Support
        // @contact.url http://mobigen.com  
        // @contact.email irisdev@mobigen.com 

        // @host localhost:8080
        func main(){
        ...
        }
        ```
- Path 
    handler 함수 위치에 작성
    * Option list  
        [caller.go](https://github.com/swaggo/swag/tree/master/example/celler/controller)
    * Sample  
        controllers/version.go
        ```go
        // GetVersion return app version
        // @Summary Get Server Version
        // @Description get server version info
        // @Tags version
        // @Accept  json
        // @Produce  json
        // @success 200 {object} HTTPResponse{data=appdata.VersionInfo} "app info(name, version, hash)"
        // @Router /version [get]
        func (controller *Version) GetVersion(c echo.Context) error {
            res := HTTPResponse{}.ReturnSuccess(
                &appdata.VersionInfo{
                    Name:      appdata.Name,
                    Version:   appdata.Version,
                    BuildHash: appdata.BuildHash})
            return c.JSON(http.StatusOK, res)
        }
        ```

## 문서 생성  
---
```bash
$ swag init
```
그러나 데이터 모델에 패키지 외부 참조가 있다면 다음과 같은 에러가 발생할 수 있다.  
```bash
 ParseComment error :cannot find type definition: ...
```
이런 경우 다음과 같이 명령을 입력한다.  
```bash
$ swag init --parseDependency --parseInternal
```

## 빌드
---
```bash
$ make
```

## 문서 확인  
---
프로그램을 실행하고 다음과 같이 Swagger를 위해 할당한 Path로 접속하면 문서를 확인할 수 있다.  
```
http://{서버 접속 정보}/swagger/index.html
```
