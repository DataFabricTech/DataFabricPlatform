# 코드 작성 - Sample
샘플 코드 작성에 앞서 모두 동일한 이름을 가질 수는 없으므로, 기존 코드의 변경이 필요하다.  

## Appendix.1 Change Module Name 
다음 명령은 소스 코드에서 {search}를 찾아 {replace}로 변경한다.  
```
# foo를 찾아 bar로 변경한다.  
$ find . -type f -name "*.go" -print0 | xargs -0 sed -i 's/foo/bar/g'

# The easier and much more readable option is to use another delimiter 
# character. Most people use the vertical bar (|) or colon (:) but you 
# can use any other character:

# github.com/mobigen/test => github.com/mobigen/blahblah 로 변경  
$ find . -type f -name "*.go" -print0 | xargs -0 sed -i 's|github.com/mobigen/test|github.com/mobigen/blahblah|g'
```

## 개요  
이 문서에서는 사용자 입력을 받아 데이터 베이스 제어하는 부분까지의 내용을 다룬다.  

## 코드 작성  
1. Create Data  
데이터베이스, Client와 주고 받을 데이터 정의
1.1. models/sample.go
```go
package models

// Sample ....
type Sample struct {
	ID        int       `json:"id", gorm:"column:id;primaryKey;autoIncrement"`
	Name      string    `json:"name" gorm:"column:name;not null"`
	Desc      string    `json:"desc" gorm:"column:desc;size:256"`
    CreateAt  int64     `json:"createAt gorm:"column:createAt;autoCreateTime:milli"`
}
```
2. 테이블 생성  
infrastructures/datastore/gorm.go - Migrate 함수  
```go
func (ds *DataStore) Migrate() error {
    ...
	ds.Orm.AutoMigrate(&models.Sample{})
    ...
}
```
3. Usecase 작성  
- Controllers - Services 사이의 interface
    controllers/sample.go 
    ```go
    type SampleUsecase interface {
        GetAll()(*[]models.Sample, error)
        GetByID(int)(*models.Sample, error)
        Create(*models.Sample)(*models.Sample, error)
        Update(*models.Sample)(*models.Sample, error)
        Delete(int)(*models.Sample, error)
    }
```
- Services - Repositories 사이의 interface
    services/sample.go
    ```go
    type SampleRepository interface {
        GetAll()(*[]models.Sample, error)
        GetByID(int)(*models.Sample, error)
        Create(*models.Sample)(*models.Sample, error)
        Update(*models.Sample)(*models.Sample, error)
        Delete(int)(*models.Sample, error)
    }
    ```
4. 작성 순서  
    기능 정의, 모델 작성, Usecase까지 정의 되어야 하는 부분이 완료되었다면, 
    Controller, Service, Repositories 중 어떤 것을 먼저 작성하더라도 상관 없다.
    각 레이어간 필요로 하는 인터페이스(모델)이 모두 정의되어 있으므로, 필요로 하는 
    부분을 처리하도록 구현을 진행하면 되기 때문이다.  
5. Repositories 작성  
    repositories/sample.go
    ```go
    package repositories

    import (
        "fmt"

        "github.com/mobigen/golang-web-template/infrastructures/datastore"
        "github.com/mobigen/golang-web-template/infrastructures/tools/util"
        "github.com/mobigen/golang-web-template/models"
    )

    // Sample is struct of todo.
    type Sample struct {
        *datastore.DataStore
    }

    // New is constructor that creates SampleRepository
    func (Sample) New(handler *datastore.DataStore) *Sample {
        return &Sample{handler}
    }

    // GetAll get all sample from database(store)
    func (repo *Sample) GetAll() (*[]models.Sample, error) {
        dst := new([]models.Sample)
        result := repo.Orm.Find(dst)
        if result.Error != nil {
            return nil, result.Error
        }
        if result.RowsAffected <= 0 {
            return nil, fmt.Errorf("no have result")
        }
        return dst, nil
    }

    // GetByID get sample whoes id match
    func (repo *Sample) GetByID(id int) (*models.Sample, error) {
        var dst *models.Sample
        result := repo.Orm.Find(dst).Where(&models.Sample{ID: id})
        if result.Error != nil {
            return nil, result.Error
        }
        if result.RowsAffected <= 0 {
            return nil, fmt.Errorf("no have result")
        }
        return dst, nil
    }

    // Create create sample
    func (repo *Sample) Create(input *models.Sample) (*models.Sample, error) {
        input.CreateAt = util.GetMillis()
        result := repo.Orm.Create(input)
        if result.Error != nil {
            return nil, result.Error
        }
        return input, nil
    }

    // Update update sample
    func (repo *Sample) Update(input *models.Sample) (*models.Sample, error) {
        // Save/Update All Fields
        // repo.Orm.Save(input)

        // ID       int    `json:"id" gorm:"column:id;primaryKey;autoIncrement"`
        // Name     string `json:"name" gorm:"column:name;not null"`
        // Desc     string `json:"desc" gorm:"column:desc;size:256"`
        // CreateAt int64  `json:"createAt" gorm:"column:createAt;autoCreateTime:milli"`
        result := repo.Orm.Model(input).
            Where(&models.Sample{ID: input.ID}).
            Updates(
                map[string]interface{}{
                    "name": input.Name,
                    "desc": input.Desc,
                })
        if result.Error != nil {
            return nil, result.Error
        }
        return input, nil

    }

    // Delete delete sample from id(primaryKey)
    func (repo *Sample) Delete(id int) (*models.Sample, error) {
        dst := new(models.Sample)
        result := repo.Orm.Find(dst).Where(&models.Sample{ID: id})
        if result.Error != nil {
            return nil, result.Error
        }
        if result.RowsAffected <= 0 {
            return nil, fmt.Errorf("no have result")
        }
        // Delete with additional conditions
        result = repo.Orm.Delete(&models.Sample{}, id)
        if result.Error != nil {
            return nil, result.Error
        }
        return dst, nil
    }
    ```
6. Services 작성  
    services/sample.go
    ```go
    package services

    import (
        "github.com/mobigen/golang-web-template/models"
    )

    // Sample service - repository - interactor for Sample entity.
    type Sample struct {
        Repo SampleRepository
    }

    // New is constructor that creates Sample service
    func (Sample) New(repo SampleRepository) *Sample {
        return &Sample{repo}
    }

    // GetAll returns All of samples.
    func (service *Sample) GetAll() ([]*models.Sample, error) {
        return service.Repo.GetAll()
    }

    // GetByID returns sample whoes that ID mathces.
    func (service *Sample) GetByID(id int) (*models.Sample, error) {
        return service.Repo.GetByID(id)
    }

    // Create create a new sample.
    func (service *Sample) Create(sample *models.Sample) (*models.Sample, error) {
        return service.Repo.Create(sample)
    }

    // Update update a sample.
    func (service *Sample) Update(sample *models.Sample) (*models.Sample, error) {
        return service.Repo.Update(sample)
    }

    // Delete delete sample from id.
    func (service *Sample) Delete(id int) (*models.Sample, error) {
        return service.Repo.Delete(id)
    }
    ```
7. Controller 작성  
    controllers/sample.go  
    ```go
    package controllers

    import (
        "net/http"
        "strconv"

        "github.com/mobigen/golang-web-template/models"
        "github.com/labstack/echo/v4"
    )

    // Sample Controller
    type Sample struct {
        Usecase SampleUsecase
    }

    // SampleUsecase usecase define
    type SampleUsecase interface {
        GetAll()(*[]models.Sample, error)
        GetByID(int)(*models.Sample, error)
        Create(*models.Sample)(*models.Sample, error)
        Update(*models.Sample)(*models.Sample, error)
        Delete(int)(*models.Sample, error)
    }

    // New create Sample instance.
    func (Sample) New(usecase SampleUsecase) *Sample {
        return &Sample{usecase}
    }

    // GetAll returns all of sample as JSON object.
    func (controller *Sample) GetAll(c echo.Context) error {
        samples, err := controller.Usecase.GetAll()
        if err != nil {
            return c.JSON(http.StatusBadRequest, samples)
        }
        return c.JSON(http.StatusOK, samples)
    }

    // GetByID return sample whoes ID mathces
    func (controller *Sample) GetByID(c echo.Context) error {
        id, err := strconv.Atoi(c.Param("id"))
        if err != nil {
            return c.JSON(http.StatusBadRequest, err)
        }
        sample, err := controller.Usecase.GetByID(id)
        if err != nil {
            return c.JSON(http.StatusInternalServerError, err)
        }
        return c.JSON(http.StatusOK, sample)
    }

    // Create create a new ...
    func (controller *Sample) Create(c echo.Context) error {
        input := new(models.Sample)
        c.Bind(input)
        sample, err := controller.Usecase.Create(input)
        if err != nil {
            return c.JSON(http.StatusInternalServerError, err)
        }
        return c.JSON(http.StatusCreated, sample)
    }

    // Update update from input
    func (controller *Sample) Update(c echo.Context) error {
        input := new(models.Sample)
        c.Bind(input)
        sample, err := controller.Usecase.Update(input)
        if err != nil {
            return c.JSON(http.StatusInternalServerError, err)
        }
        return c.JSON(http.StatusOK, sample)
    }

    // Delete delete sample from id
    func (controller *Sample) Delete(c echo.Context) error {
        id, err := strconv.Atoi(c.Param("id"))
        if err != nil {
            return c.JSON(http.StatusBadRequest, err)
        }
        sample, err := controller.Usecase.Delete(id)
        if err != nil {
            return c.JSON(http.StatusInternalServerError, err)
        }
        return c.JSON(http.StatusOK, sample)
    }
    ```

8. Injector 작성  
    - Controller 생성, Dependency Injection
    injectors/sample.go
    ```go
    package injectors

    import (
        "github.com/mobigen/golang-web-template/controllers"
        "github.com/mobigen/golang-web-template/repositories"
        "github.com/mobigen/golang-web-template/services"
    )

    // Sample sample injector
    type Sample struct{}

    // Init for interconnection [ controller(App) - Service(Repository) - repository - datastore ] : Dependency Injection
    func (Sample) Init(in *Injector) *controllers.Sample {
        repo := repositories.Sample{}.New(in.Datastore)
        svc := services.Sample{}.New(repo)
        return controllers.Sample{}.New(svc)
    }
    ```
    - PATH 등록  
        injectors/injector-core.go - Init 함수에서 path와 controller.func 를 연결해 준다.  
        ```go
        // Init ... 
        func (h *Injector) Init() error {
            // path grouping
            apiv1 := h.Router.Group("/api/v1")

            // Sample
            h.Log.Errorf("[ PATH ] /api/v1/sample ........................................................... [ OK ]")
            sample := Sample{}.Init(h)
            apiv1.GET("/samples", sample.GetAll)
            apiv1.GET("/sample/:id", sample.GetByID)
            apiv1.POST("/sample", sample.Create)
            apiv1.POST("/sample/update", sample.Update)
            apiv1.DELETE("/sample/:id", sample.Delete)
            return nil
        }
        ```
9. Build, Test, Doc
    이제 build, test, doc 작성으로 넘어간다.  
