## Data Fabric - Swagger API Documentation 

이 Swagger 문서는 워드로 작성된 설계 문서를 기반으로 작성되었습니다.
[설계서](https://mobigen0321-my.sharepoint.com/:w:/g/personal/jblim_mobigen0321_onmicrosoft_com/EQNuFj-110tGrsLMzK-CEIoB1wgtIoMgi0cF3G95h2BtmA?e=Cbk7VY)를 사용하여 작성되었습니다.

하나의 문서로 모든 내용을 정리할 경우 보기 어려운 문제가 있어 디렉토리르 세분화하여 작성하였습니다. (추가적인 정리는 필요)  

### 문서 작성 방법
1. Path 정의
2. Parameter 정의
    parameters 디렉토리 
3. Schema 정의
    재사용성을 고려(Request, Response 에서 재사용)
4. Request, Response 정의
5. Example 

### 최종 문서 통합
여러개의 문서를 하나의 문서로 통합하는 방법은 다음과 같습니다.
1. swagger-cli 설치
    ```
    $ npm install -g swagger-cli
    ```
2. swagger-cli를 사용하여 통합
    ```
    $ swagger-cli bundle api.yaml -o _build/openapi.yaml -t yaml
    ``` 