package controllers

import (
	"github.com/datafabric/gateway/common"
	"github.com/datafabric/gateway/models"
	pbPortal "github.com/datafabric/gateway/protobuf"
	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
	"net/http"
)

// PortalService portal service
type PortalService interface {
	Search(search *pbPortal.ReqSearch) (*pbPortal.ResSearch, error)
	RecentSearches() (*pbPortal.ResRecentSearches, error)
}

// PortalController Portal Controller layer
type PortalController struct {
	log     *logrus.Logger
	Service PortalService
}

// PortalControllerInitialize create portal controller layer instance.
func PortalControllerInitialize(service PortalService) *PortalController {
	return &PortalController{
		log:     common.Logger{}.GetInstance().Logger,
		Service: service,
	}
}

// Search return search result
func (ctrl *PortalController) Search(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> Search", "PORTAL")
	request := new(pbPortal.ReqSearch)
	err := c.Bind(request)
	if err != nil {
		return err
	}
	res, err := ctrl.Service.Search(request)
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	if res.Code != "200" {
		ctrl.log.Errorf("[%-10s] << Search : Error   [ %4s ]/[ %s ]", "PORTAL", res.Code, res.ErrMsg)
		return c.JSON(http.StatusOK, res)
	}
	resSearch := &models.ResSearch{}
	_, err = resSearch.Convert(res.Data.SearchResponse)

	ctrl.log.Infof("[%-10s] << Search", "PORTAL")
	return c.JSON(http.StatusOK, resSearch)
}

// RecentSearches for user search history
func (ctrl *PortalController) RecentSearches(c echo.Context) error {
	ctrl.log.Infof("[%-10s] >> RecentSearches", "PORTAL")
	res, err := ctrl.Service.RecentSearches()
	if err != nil {
		return c.JSON(http.StatusOK, &models.CommonResponse{Code: "500", ErrMsg: err.Error()})
	}
	ctrl.log.Infof("[%-10s] << RecentSearches", "PORTAL")
	return c.JSON(http.StatusOK, res)
}
