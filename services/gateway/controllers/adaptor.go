package controllers

import (
	"github.com/datafabric/gateway/common"
	"github.com/datafabric/gateway/models"
	"github.com/datafabric/gateway/protobuf"
	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
	"net/http"
)

// AdaptorService adaptor service
type AdaptorService interface {
	// SupportedStorageType : GET /storage/v1/storage-type:
	SupportedStorageType() (*protobuf.ResSupportedStorageType, error)
	// GetAdaptors : GET /storage/v1/adaptors?storage-type=xxx
	GetAdaptors(*protobuf.ReqAdaptors) (*protobuf.ResAdaptors, error)
}

// AdaptorController adaptor controller Layer instance
type AdaptorController struct {
	log     *logrus.Logger
	service AdaptorService
}

// AdaptorControllerInitialize create adaptor controller layer instance.
func AdaptorControllerInitialize(service AdaptorService) *AdaptorController {
	return &AdaptorController{
		log:     common.Logger{}.GetInstance().Logger,
		service: service,
	}
}

// SupportedStorageType Get Supported Storage Type
func (ctrl *AdaptorController) SupportedStorageType(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Supported Storage Type", "ADAPTOR")
	res, err := ctrl.service.SupportedStorageType()
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << Supported Storage Type : Error   [ %4s ]/[ %s ]", "ADAPTOR", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	//adaptors := &models.Adaptors{}
	//adaptors.Convert(res.Data)
	storageTypes := &models.SupportedStorageTypes{}
	storageTypes.Convert(res.Data.SupportedStorageType)
	resStorageTypes := &models.CommonResponse{
		Code: res.Code,
		Data: map[string]interface{}{
			"supportedStorageType": storageTypes,
		},
	}

	ctrl.log.Infof("[%-10s] << Supported Storage Type", "ADAPTOR")
	return c.JSON(http.StatusOK, resStorageTypes)
}

// GetAdaptors Get Adaptor From Query Param(storage-type)
func (ctrl *AdaptorController) GetAdaptors(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> GetAdaptor", "ADAPTOR")
	storageType := c.QueryParam("storage-type")
	if len(storageType) <= 0 {
		return c.JSON(http.StatusOK, &models.CommonResponse{
			Code:   "400",
			ErrMsg: "not found query parameter(storage-type)",
		})
	}

	req := &protobuf.ReqAdaptors{StorageType: storageType}
	res, err := ctrl.service.GetAdaptors(req)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{
			Code:   "500",
			ErrMsg: err.Error(),
		})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << GetAdaptor : Error   [ %4s ]/[ %s ]", "storage", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	//adaptors := &models.Adaptors{}
	//adaptors.Convert(res.Data)
	//resAdaptor := &models.CommonResponse{
	//	Code: res.Code,
	//	Data: map[string]interface{}{
	//		"adaptors": adaptors,
	//	},
	//}
	ctrl.log.Infof("[%-10s] << GetAdaptor", "ADAPTOR")
	return c.JSON(http.StatusOK, res)
}
