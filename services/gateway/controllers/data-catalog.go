package controllers

import (
	"github.com/datafabric/gateway/common"
	"github.com/datafabric/gateway/models"
	"github.com/datafabric/gateway/protobuf"
	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
	"net/http"
)

// DataCatalogService data catalog service
type DataCatalogService interface {
	// Preview 			POST /data/v1/preview 				- 미리보기
	Preview(in *protobuf.ReqId) (*protobuf.DataCatalogPreview, error)
	// Default 			POST /data/v1/default 				- 데이터 상세 보기 - 기본 정보
	Default(in *protobuf.ReqId) (*protobuf.DataCatalogDefault, error)
	// UserMetadata 	POST /data/v1/metadata 				- 사용자 설정 메타 데이터 업데이트
	UserMetadata(in *protobuf.ReqMetaUpdate) (*protobuf.CommonResponse, error)
	// Tag 				POST /data/v1/tag 					- 데이터 태그 업데이트
	Tag(in *protobuf.ReqTagUpdate) (*protobuf.CommonResponse, error)
	// DownloadRequest 	POST /data/v1/download-request 		- 다운로드 요청
	DownloadRequest(in *protobuf.ReqId) (*protobuf.CommonResponse, error)
	// AddComment 		POST /data/v1/comment/add 			- 데이터 평가와 댓글 추가
	AddComment(in *protobuf.ReqRatingAndComment) (*protobuf.CommonResponse, error)
	// UpdateComment 	POST /data/v1/comment/update 		- 데이터 평가와 댓글 업데이트
	UpdateComment(in *protobuf.ReqRatingAndComment) (*protobuf.CommonResponse, error)
	// DelComment 		POST /data/v1/comment/delete 		- 데이터 평가와 댓글 삭제
	DelComment(in *protobuf.ReqId) (*protobuf.CommonResponse, error)
	// AllDataSummary 	POST /data/v1/all-data/summary 		- 데이터 브라우저 좌측 패널용 데이터
	AllDataSummary(in *protobuf.DataCatalogSearch) (*protobuf.ResDataCatalogs, error)
	// AllData 			POST /data/v1/all-data          	- 데이터 브라우저 조회(검색) 데이터
	AllData(in *protobuf.DataCatalogSearch) (*protobuf.ResDataCatalogs, error)
}

// DataCatalogController data catalog controller Layer instance
type DataCatalogController struct {
	log     *logrus.Logger
	service DataCatalogService
}

// DataCatalogControllerInitialize create data catalog controller layer instance.
func DataCatalogControllerInitialize(service DataCatalogService) *DataCatalogController {
	return &DataCatalogController{
		log:     common.Logger{}.GetInstance().Logger,
		service: service,
	}
}

// Preview POST /data/v1/preview - 미리보기
func (ctrl *DataCatalogController) Preview(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Preview ", "DATA_CATALOG")
	req := &protobuf.ReqId{}
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "400", ErrMsg: err.Error()})
	}
	res, err := ctrl.service.Preview(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << Preview : Error [ %4s ]/[ %s ]", "DATA_CATALOG", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	preview := &models.DataCatalog{}
	preview.Convert(res.Data.DataPreview)
	ctrl.log.Infof("[%-10s] << Preview", "DATA_CATALOG")
	return c.JSON(http.StatusOK,
		&models.CommonResponse{
			Code: res.Code,
			Data: map[string]interface{}{
				"dataPreview": preview,
			},
		})
}

// Default POST /data/v1/default - 데이터 상세 보기 - 기본 정보
func (ctrl *DataCatalogController) Default(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Default", "DATA_CATALOG")
	req := &protobuf.ReqId{}
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "400", ErrMsg: err.Error()})
	}
	res, err := ctrl.service.Default(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << Defalut : Error [ %4s ]/[ %s ]", "DATA_CATALOG", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	dataCatalog := &models.DataCatalog{}
	dataCatalog.Convert(res.Data.DataCatalog)
	ctrl.log.Infof("[%-10s] << Default", "DATA_CATALOG")
	return c.JSON(http.StatusOK,
		&models.CommonResponse{
			Code: res.Code,
			Data: map[string]interface{}{
				"dataCatalog": dataCatalog,
			},
		})
}

// UserMetadata POST /data/v1/metadata - 사용자 설정 메타 데이터 업데이트
func (ctrl *DataCatalogController) UserMetadata(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> UserMetadata", "DATA_CATALOG")
	req := &protobuf.ReqMetaUpdate{}
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "400", ErrMsg: err.Error()})
	}
	res, err := ctrl.service.UserMetadata(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << UserMetadata : Error [ %4s ]/[ %s ]", "DATA_CATALOG", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	ctrl.log.Infof("[%-10s] << UserMetadata", "DATA_CATALOG")
	return c.JSON(http.StatusOK, res)
}

// Tag POST /data/v1/tag - 데이터 태그 업데이트
func (ctrl *DataCatalogController) Tag(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Tag", "DATA_CATALOG")
	req := &protobuf.ReqTagUpdate{}
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "400", ErrMsg: err.Error()})
	}
	res, err := ctrl.service.Tag(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << Tag : Error [ %4s ]/[ %s ]", "DATA_CATALOG", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	ctrl.log.Infof("[%-10s] << Tag", "DATA_CATALOG")
	return c.JSON(http.StatusOK, res)
}

// DownloadRequest POST /data/v1/download-request - 다운로드 요청
func (ctrl *DataCatalogController) DownloadRequest(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Download Request", "DATA_CATALOG")
	req := &protobuf.ReqId{}
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "400", ErrMsg: err.Error()})
	}
	res, err := ctrl.service.DownloadRequest(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << Download Request: Error [ %4s ]/[ %s ]", "DATA_CATALOG", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	ctrl.log.Infof("[%-10s] << Download Request", "DATA_CATALOG")
	return c.JSON(http.StatusOK, res)
}

// AddComment POST /data/v1/comment/add - 데이터 평가와 댓글 추가
func (ctrl *DataCatalogController) AddComment(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Add Comment", "DATA_CATALOG")
	req := &protobuf.ReqRatingAndComment{}
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "400", ErrMsg: err.Error()})
	}
	res, err := ctrl.service.AddComment(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << Add Comment : Error [ %4s ]/[ %s ]", "DATA_CATALOG", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	ctrl.log.Infof("[%-10s] << Add Comment", "DATA_CATALOG")
	return c.JSON(http.StatusOK, res)
}

// UpdateComment POST /data/v1/comment/update - 데이터 평가와 댓글 업데이트
func (ctrl *DataCatalogController) UpdateComment(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Update Comment", "DATA_CATALOG")
	req := &protobuf.ReqRatingAndComment{}
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "400", ErrMsg: err.Error()})
	}
	res, err := ctrl.service.UpdateComment(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << Add Comment : Error [ %4s ]/[ %s ]", "DATA_CATALOG", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	ctrl.log.Infof("[%-10s] << Add Comment", "DATA_CATALOG")
	return c.JSON(http.StatusOK, res)
}

// DelComment POST /data/v1/comment/delete - 데이터 평가와 댓글 삭제
func (ctrl *DataCatalogController) DelComment(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Delete Comment", "DATA_CATALOG")
	req := &protobuf.ReqId{}
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "400", ErrMsg: err.Error()})
	}
	res, err := ctrl.service.DelComment(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << Delete Comment : Error [ %4s ]/[ %s ]", "DATA_CATALOG", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	ctrl.log.Infof("[%-10s] << Delete Comment", "DATA_CATALOG")
	return c.JSON(http.StatusOK, res)
}

// AllDataSummary 	POST /data/v1/all-data/summary 		- 데이터 브라우저 좌측 패널용 데이터
func (ctrl *DataCatalogController) AllDataSummary(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> All Data Summary", "DATA_CATALOG")
	req := &protobuf.DataCatalogSearch{}
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "400", ErrMsg: err.Error()})
	}
	res, err := ctrl.service.AllDataSummary(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << All Data Summary : Error [ %4s ]/[ %s ]", "DATA_CATALOG", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	ctrl.log.Infof("[%-10s] << All Data Summary", "DATA_CATALOG")
	return c.JSON(http.StatusOK, res)
}

// AllData 			POST /data/v1/all-data          	- 데이터 브라우저 조회(검색) 데이터
func (ctrl *DataCatalogController) AllData(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> All Data", "DATA_CATALOG")
	req := &protobuf.DataCatalogSearch{}
	err := c.Bind(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "400", ErrMsg: err.Error()})
	}
	res, err := ctrl.service.AllData(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << All Data : Error [ %4s ]/[ %s ]", "DATA_CATALOG", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	ctrl.log.Infof("[%-10s] << All Data", "DATA_CATALOG")
	return c.JSON(http.StatusOK, res)
}
