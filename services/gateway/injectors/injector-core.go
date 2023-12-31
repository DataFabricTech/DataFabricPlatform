package injectors

import (
	"github.com/datafabric/gateway/common"
	"github.com/datafabric/gateway/infrastructures/datastore"
	"github.com/datafabric/gateway/infrastructures/router"
	"github.com/datafabric/gateway/models"
)

// Injector web-server layer initializer : Dependency Injection )
type Injector struct {
	Router    *router.Router
	Datastore *datastore.DataStore
	Log       *common.Logger
	AppConfig *models.Configuration
}

// NewInjector create Injector
func NewInjector(r *router.Router, d *datastore.DataStore, l *common.Logger, config *models.Configuration) *Injector {
	return &Injector{
		Router:    r,
		Datastore: d,
		Log:       l,
		AppConfig: config,
	}
}

// Init web-server layer interconnection create (web server layer init
func (h *Injector) Init() error {
	// For Version
	ver := Version{}.Init(h)
	h.Router.GET("/version", ver.GetVersion)

	// Portal : Grouping
	h.Log.Errorf("[ PATH ] /portal/v1 ............................................................... [ OK ]")
	portalGroup := h.Router.Group("/portal/v1")
	portal := PortalInjector{}.Init(h)
	portalGroup.POST("/search", portal.Search)
	portalGroup.GET("/recent-searches", portal.RecentSearches)

	// Data Catalog : Grouping
	h.Log.Errorf("[ PATH ] /data/v1 ................................................................. [ OK ]")
	dataModelGroup := h.Router.Group("/data/v1")
	dataModel := DataModelInjector{}.Init(h)
	// Data Catalog Detail And Management
	dataModelGroup.POST("/preview", dataModel.Preview)
	dataModelGroup.POST("/default", dataModel.Default)
	dataModelGroup.POST("/metadata", dataModel.UserMetadata)
	dataModelGroup.POST("/tag", dataModel.Tag)
	dataModelGroup.POST("/download-request", dataModel.DownloadRequest)
	dataModelGroup.POST("/comment/add", dataModel.AddComment)
	dataModelGroup.POST("/comment/update", dataModel.UpdateComment)
	dataModelGroup.POST("/comment/delete", dataModel.DelComment)
	// DCatalogGta Catalog Browser
	dataModelGroup.POST("/all-data/summary", dataModel.AllDataSummary)
	dataModelGroup.POST("/all-data", dataModel.AllData)

	// Storage : Grouping
	h.Log.Errorf("[ PATH ] /storage/v1 .............................................................. [ OK ]")
	storageGroup := h.Router.Group("/storage/v1")
	// Adaptor
	adaptor := AdaptorInjector{}.Init(h)
	storageGroup.GET("/storage-type", adaptor.SupportedStorageType)
	storageGroup.GET("/adaptors", adaptor.GetAdaptors)

	// Storage
	storage := StorageInjector{}.Init(h)
	// Storage Management
	storageGroup.POST("/connect-test", storage.ConnectTest)
	storageGroup.POST("/add", storage.AddStorage)
	storageGroup.POST("/modify", storage.UpdateStorage)
	storageGroup.GET("/connected-data/:storage-id", storage.ConnectedData)
	storageGroup.POST("/delete", storage.DeleteStorage)

	// Storage Browser
	storageGroup.GET("/overview", storage.Overview)
	storageGroup.POST("/search", storage.Search)
	storageGroup.POST("/status", storage.Status)
	storageGroup.POST("/default", storage.Default)
	storageGroup.POST("/advanced", storage.Advanced)
	storageGroup.POST("/browse", storage.Browse)
	storageGroup.POST("/browse/default", storage.BrowseDefault)
	return nil
}
