package controllers

import (
	"github.com/datafabric/gateway/common"
	"github.com/datafabric/gateway/models"
	"github.com/datafabric/gateway/protobuf"
	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
	"net/http"
)

// StorageService storage service
type StorageService interface {
	/*
		Storage Browser : 원천 데이터 저장소 탐색기
	*/
	// Overview 		GET    : /storage/v1/overview
	Overview() (*protobuf.ResStorageOverview, error)
	// Search 			POST   : /storage/v1/search
	Search(req *protobuf.ReqStorageSearch) (*protobuf.ResStorages, error)
	// Status			POST   : /storage/v1/status
	Status(req *protobuf.ReqId) (*protobuf.ResStorage, error)
	// Default 			POST   : /storage/v1/default
	Default(req *protobuf.ReqId) (*protobuf.ResStorage, error)
	// Advanced			POST   : /storage/v1/advanced
	Advanced(req *protobuf.ReqId) (*protobuf.ResStorage, error)
	// Browse			POST   : /storage/v1/browse
	Browse(req *protobuf.ReqStorageBrowse) (*protobuf.ResStorageBrowse, error)
	// BrowseDefault	POST   : /storage/v1/browse/default
	BrowseDefault(req *protobuf.ReqStorageBrowse) (*protobuf.ResStorageBrowseDefault, error)

	/*
	   Storage Management : 원천 데이터 저장소 정보 추가/수정/삭제
	   연결정보 추가를 위한 과정 중 StorageType, Adaptor 관련은 AdaptorService 에서 처리
	*/
	// ConnectTest 			POST   : /storage/v1/connect-test
	ConnectTest(req *protobuf.ConnInfo) (*protobuf.CommonResponse, error)
	// AddStorage			POST   : /storage/v1/add
	AddStorage(req *protobuf.Storage) (*protobuf.CommonResponse, error)
	// UpdateStorage		POST   : /storage/v1/modify
	UpdateStorage(req *protobuf.Storage) (*protobuf.CommonResponse, error)
	// ConnectedData		GET	   : /storage/v1/connected-data/{storage-id}
	ConnectedData(req *protobuf.ReqId) (*protobuf.ResConnectedData, error)
	// DeleteStorage		POST   : /storage/v1/delete
	DeleteStorage(req *protobuf.ReqId) (*protobuf.CommonResponse, error)
}

// StorageController storage controller Layer instance
type StorageController struct {
	log     *logrus.Logger
	service StorageService
}

// StorageControllerInitialize create storage controller layer instance.
func StorageControllerInitialize(service StorageService) *StorageController {
	return &StorageController{
		log:     common.Logger{}.GetInstance().Logger,
		service: service,
	}
}

// Overview Get Storage Dashboard
func (ctrl *StorageController) Overview(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Overview", "STORAGE")
	res, err := ctrl.service.Overview()
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << Overview : Error   [ %4s ]/[ %s ]", "storage", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	// TODO : 대시 보드용으로 데이터 가공
	ctrl.log.Infof("[%-10s] << Overview", "storage")
	return c.JSON(http.StatusOK, res)
}

// Search 			POST   : /storage/v1/search
func (ctrl *StorageController) Search(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Search", "STORAGE")
	req := new(protobuf.ReqStorageSearch)
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "400", ErrMsg: err.Error()})
	}
	res, err := ctrl.service.Search(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << Search : Error   [ %4s ]/[ %s ]", "storage", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	ctrl.log.Infof("[%-10s] << Search", "storage")
	return c.JSON(http.StatusOK, res)
}

// Status			POST   : /storage/v1/status
func (ctrl *StorageController) Status(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Status", "STORAGE")
	req := new(protobuf.ReqId)
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "400", ErrMsg: err.Error()})
	}
	res, err := ctrl.service.Status(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << Status : Error   [ %4s ]/[ %s ]", "storage", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	// TODO : 데이터 가공
	ctrl.log.Infof("[%-10s] << Status ", "storage")
	return c.JSON(http.StatusOK, res)
}

// Default 			POST   : /storage/v1/default
func (ctrl *StorageController) Default(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Default", "STORAGE")
	req := new(protobuf.ReqId)
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "400", ErrMsg: err.Error()})
	}
	res, err := ctrl.service.Default(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << Default : Error   [ %4s ]/[ %s ]", "storage", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	// TODO : 데이터 가공
	ctrl.log.Infof("[%-10s] << Default ", "storage")
	return c.JSON(http.StatusOK, res)
}

// Advanced			POST   : /storage/v1/advanced
func (ctrl *StorageController) Advanced(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Advanced", "STORAGE")
	req := new(protobuf.ReqId)
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "400", ErrMsg: err.Error()})
	}
	res, err := ctrl.service.Advanced(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << Advanced : Error   [ %4s ]/[ %s ]", "storage", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	// TODO : 데이터 가공
	ctrl.log.Infof("[%-10s] << Advanced ", "storage")
	return c.JSON(http.StatusOK, res)
}

// Browse			POST   : /storage/v1/browse
func (ctrl *StorageController) Browse(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Browse", "STORAGE")
	req := new(protobuf.ReqStorageBrowse)
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "400", ErrMsg: err.Error()})
	}
	res, err := ctrl.service.Browse(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << Browse: Error   [ %4s ]/[ %s ]", "storage", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	ctrl.log.Infof("[%-10s] << Browse", "storage")
	return c.JSON(http.StatusOK, res)
}

// BrowseDefault	POST   : /storage/v1/browse/default
func (ctrl *StorageController) BrowseDefault(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Browse / Default", "STORAGE")
	req := new(protobuf.ReqStorageBrowse)
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "400", ErrMsg: err.Error()})
	}
	res, err := ctrl.service.BrowseDefault(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << Browse / Default : Error   [ %4s ]/[ %s ]", "storage", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	ctrl.log.Infof("[%-10s] << Browse / Default", "storage")
	return c.JSON(http.StatusOK, res)
}

// ConnectTest 			POST   : /storage/v1/connect-test
func (ctrl *StorageController) ConnectTest(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> ConnectTest", "STORAGE")
	req := new(protobuf.ConnInfo)
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{
			Code:   "400",
			ErrMsg: err.Error(),
		})
	}
	res, err := ctrl.service.ConnectTest(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{
			Code:   "500",
			ErrMsg: err.Error(),
		})
	}
	ctrl.log.Infof("[%-10s] << ConnectTest", "STORAGE")
	return c.JSON(http.StatusOK, res)
}

// AddStorage			POST   : /storage/v1/add
func (ctrl *StorageController) AddStorage(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Add Storage", "STORAGE")
	req := new(protobuf.Storage)
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{
			Code:   "400",
			ErrMsg: err.Error(),
		})
	}
	res, err := ctrl.service.AddStorage(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{
			Code:   "500",
			ErrMsg: err.Error(),
		})
	}
	ctrl.log.Infof("[%-10s] << Add Storage", "STORAGE")
	return c.JSON(http.StatusOK, res)
}

// UpdateStorage		POST   : /storage/v1/modify
func (ctrl *StorageController) UpdateStorage(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Update Storage", "STORAGE")
	req := new(protobuf.Storage)
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{
			Code:   "400",
			ErrMsg: err.Error(),
		})
	}
	res, err := ctrl.service.UpdateStorage(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{
			Code:   "500",
			ErrMsg: err.Error(),
		})
	}
	ctrl.log.Infof("[%-10s] << Update Storage", "STORAGE")
	return c.JSON(http.StatusOK, res)
}

// ConnectedData		GET : /storage/v1/connected-data/{storage-id}
func (ctrl *StorageController) ConnectedData(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Connected Data From Storage", "STORAGE")
	storageId := c.Param("storage-id")
	if len(storageId) <= 0 {
		return c.JSON(http.StatusOK, &models.CommonResponse{
			Code:   "400",
			ErrMsg: "storage-id is empty",
		})
	}
	res, err := ctrl.service.ConnectedData(&protobuf.ReqId{Id: storageId})
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{
			Code:   "500",
			ErrMsg: err.Error(),
		})
	}
	ctrl.log.Infof("[%-10s] << Connected Data From Storage", "STORAGE")
	return c.JSON(http.StatusOK, res)
}

// DeleteStorage		POST   : /storage/v1/delete
func (ctrl *StorageController) DeleteStorage(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Delete Storage", "STORAGE")
	req := new(protobuf.ReqId)
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{
			Code:   "400",
			ErrMsg: err.Error(),
		})
	}
	res, err := ctrl.service.DeleteStorage(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{
			Code:   "500",
			ErrMsg: err.Error(),
		})
	}
	ctrl.log.Infof("[%-10s] << Delete Storage", "STORAGE")
	return c.JSON(http.StatusOK, res)
}
