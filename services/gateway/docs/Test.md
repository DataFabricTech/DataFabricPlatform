# Test  
---
Golang에서 Test   

## 개요  
* golang/mock 프레임워크 이용하기  
* stretchr/testify/mock 프레임워크 이용하기  

Golang은 투명한 동작과 엄격한 타입이 특징인 만큼 Go에서는 Mocking을 
이용하는 것은 다른 언어에 비해 쉬운 편은 아닌 것처럼 느껴지기도 합니다.  

* interface로 선언된 변수에만 mock type을 할당할 수 있다.  
( 그래서 우리는 레이어 구조를 만들면서 각 레이거간 의존성을 인터페이스로 만들었습니다. )   
* mock type을 직접 정의하거나 mocking framework을 이용해 코드를 생성한다.  
( framework를 사용하더라도 내부는 직접 작성하고 실행해야 한다. )  

## gomock과 testify/mock 의 비교 
GoMock vs. Testify: Mocking frameworks for Go  
[Link](https://blog.codecentric.de/2019/07/gomock-vs-testify/)  
위 링크에 내용이 잘 정리되어있으니 참고해보세요.( 영문사이트... )  

두 진영 비교  
[Star](https://umi0410.github.io/blog/golang/how-to-backend-in-go-testcode/star-comparison.png)  
최재호 책임의 의견처럼 우리는 어떤 진영을 선택할지는 중요하지 않습니다.  
테스트를 만드는 것은 우리의 몫이기 때문입니다.  

이제 두 진영의 사용 방법에 대해서 확인해 보겠습니다.  

## golang/mock  
---
1. 설치  
    ```bash
    $ go get github.com/golang/mock/gomock@v1.6.0
    $ go get github.com/golang/mock/mockgen
    ## 참고 : mockgen 설치 시 root 권한이 필요할 수 있다.  
    ```
    mockgen 설치 확인  
    ```bash
    $ mockgen -version
    ```

2. 아주 간단한 예제    
    doer, user로 구성된 아주 간략한 프로그램  
    doer는 interface이며, user에서 doer를 사용  
    ( user가 시험 대상 )  
    * prepare  
        ```bash
        $ mkdir -p gomock && cd gomock && go mod init testing-with-gomock && mkdir -p doer && \
          mkdir -p user && go get github.com/golang/mock/gomock@v1.6.0
        ```
    * doer : interface  
        ```bash
        $ echo 'package doer
        type Doer interface { 
            DoSomething(int, string) error 
        }' > doer/doer.go
        ```
    * user  
        ```bash
        $ echo 'package user
        import "testing-with-gomock/doer"
        type User struct {
            Doer doer.Doer
        }
        func (u *User) Use() error {
            return u.Doer.DoSomething(123, "Hello GoMock")
        }' > user/user.go
        ```
    * mock 생성  
        ```bash
        $ mkdir -p mocks
        $ mockgen -destination=mocks/mock_doer.go -package=mocks -source=doer/doer.go 
        ```
    * test  
        ```bash
        $ echo 'package user
        import (
                "testing"
                "testing-with-gomock/mocks"
                "github.com/golang/mock/gomock"
        )

        func TestUse(t *testing.T) {
                mockCtrl := gomock.NewController(t)
                defer mockCtrl.Finish()

                mockDoer := mocks.NewMockDoer(mockCtrl)
                testUser := &User{Doer: mockDoer}

                // mock의 Dosomething 호출 시 인자값이 123, "Hello GoMock"을 수신하길 기대 함. 
                mockDoer.EXPECT().DoSomething(123, "Hello GoMock").Return(nil).Times(1)

                // User에서 use는 doer의 dosomething 호출에서 123, Hello GoMock를 인자로 전달하고 있어 테스트는 성공한다.  
                testUser.Use()
        }' > user/user_test.go
        ```
    * go test  
        ```bash
        $ go test -v ./user/user_test.go
        === RUN   TestUse
        --- PASS: TestUse (0.00s)
        PASS
        ok  	command-line-arguments	0.397s
        ```
## testify/mock  
---
1. 설치  
    ```bash
    $ go get github.com/stretchr/testify/mock
    $ go get github.com/vektra/mockery/.../
    ## 권한 오류가 발생할 수 있다. 그런 경우 root 유저로 실행한다.  
    ```
2. 아주 간단한 예제  
    golang/mock과 코드는 동일하다. 그러나 test 코드 작성에서 차이가 있다.  
    * prepare  
        ```bash
        $ mkdir -p testify-mock && cd testify-mock && go mod init testify-mock 
        $ go get github.com/stretchr/testify/assert
        $ go get github.com/stretchr/testify/require
        $ go get github.com/stretchr/testify/mock
        $ mkdir -p doer && mkdir -p user 
        ```
    * doer : interface  
        ```bash
        $ echo 'package doer
        type Doer interface { 
            DoSomething(int, string) error 
        }' > doer/doer.go
        ```
    * user  
        ```bash
        $ echo 'package user
        import "testify-mock/doer"
        type User struct {
            Doer doer.Doer
        }
        func (u *User) Use() error {
            return u.Doer.DoSomething(123, "Hello GoMock")
        }' > user/user.go
        ```
    * mock 생성  
        ```bash
        $ mkdir -p mocks
        $ mockery -dir doer -name Doer
        ```
    * test 
        ```bash
        $ echo 'package user
        import (
        
        )
        func TestUserWithTestifyMock(t *testing.T) {
            mockDoer := &mocks.Doer{}

            testUser := &User{Doer:mockDoer}

            mockDoer.On("DoSomething", 123, "Hello GoMock").Return(nil).Once()

            testUser.Use()

            mockDoer.AssertExpectations(t)
        }' > user/user_test.go
        ```
    * run test
        ```bash
        $ go test -v ./user
        === RUN   TestUserWithTestifyMock
            user_test.go:17: PASS:      DoSomething(int,string)
        --- PASS: TestUserWithTestifyMock (0.00s)
        PASS
        ok      testify-mock/user       (cached)
        ```

## 비교  
---
...
...

## 추가 
---
* mock 생성 팁  
아래와 같이 주석을 추가  
```go
package doer

//go:generate mockgen -destination=../mocks/mock_doer.go -package=mocks testing-with-gomock/doer Doer

type Doer interface {
	DoSomething(int, string) error
}
```
프로젝트 루트 디렉토리에서 다음 명령으로 mock 생성이 가능하다.  
```bash
$ go generate ./...
```
```bash
mkdir -p mocks
```
```bash
mockgen -destination=mocks/mock_doer.go -package=mocks github.com/sgreben/testing-with-gomock/doer Doer
```
Here, we have to create the directory mocks ourselves because GoMock won’t do it for us and will quit with 
an error instead. Here’s what the arguments given to mockgen mean:  
* destination=mocks/mock_doer.go  
    put the generated mocks in the file mocks/mock_doer.go.
* package=mocks:  
    put the generated mocks in the package mocks
* github.com/sgreben/testing-with-gomock/doer:  
    generate mocks for this package
* Doer:  
    generate mocks for this interface. This argument is required — we need to specify the interfaces to 
    generate mocks for explicitly. We can, however specify multiple interfaces here as a comma-separated 
    list (e.g. Doer1,Doer2).

## Echo Framework + Testing   
...
...

## Sonarqube  
- sonarqube config 파일
```
#Configure here general information about the environment, such as SonarQube server connection details for example
#No information about specific project should appear here

#----- Default SonarQube server
sonar.host.url=http://192.168.102.127:9000/
sonar.login=2a09a7d77a7c54c94b33b5a7270b30907d023127

# #----- Default source code encoding
sonar.sourceEncoding=UTF-8

sonar.projectKey=golang-echo-sample
sonar.projectName=golang-echo-sample
# sonar.projectVersion=1.0
sonar.language=go
sonar.sources=.
sonar.exclusions=**/mock/**,**/secret/**,**/docs/**,**/data/**,.idea/**,**/vendor/**
sonar.sourceEncoding=UTF-8
sonar.tests=.
sonar.test.inclusions=**/*_test.go
sonar.test.exclusions=**/vendor/**
sonar.go.coverage.reportPaths=**/coverage.out
```
- 명령어
```bash
$ go test -v ./... -coverprofile=coverage.out
$ go test -v ./... -json > report.json
$ sonar-scanner
```

