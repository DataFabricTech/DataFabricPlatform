package controllers

import (
	"net/http"

	"github.com/labstack/echo/v4"

	"github.com/datafabric/gateway/common/appdata"
)

// Version for version
type Version struct{}

// New create version instance.
func (Version) New() *Version {
	return &Version{}
}

// GetVersion return app version
// @Summary Get Server Version
// @Description get server version info
// @Tags version
// @Accept  json
// @Produce  json
// @success 200 {object} appdata.VersionInfo "(name, version, git_hash)"
// @Router /version [get]
func (controller *Version) GetVersion(c echo.Context) error {
	return c.JSON(http.StatusOK,
		appdata.VersionInfo{
			Name:      appdata.Name,
			Version:   appdata.Version,
			BuildHash: appdata.BuildHash})
}
