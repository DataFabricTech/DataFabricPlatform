package injectors

import (
	"github.com/datafabric/gateway/common"
	"github.com/datafabric/gateway/infrastructures/datastore"
	"github.com/datafabric/gateway/infrastructures/router"
)

// Injector web-server layer initializer : Dependency Injection )
type Injector struct {
	Router    *router.Router
	Datastore *datastore.DataStore
	Log       *common.Logger
}

// New create Injector
func (Injector) New(r *router.Router, d *datastore.DataStore,
	l *common.Logger) *Injector {
	return &Injector{
		Router:    r,
		Datastore: d,
		Log:       l,
	}
}

// Init init web-server layer interconnection create (web server layer init
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
	dataCatalogGroup := h.Router.Group("/data/v1")
	dataCatalog := DataCatalogInjector{}.Init(h)
	// Data Catalog Detail And Management
	dataCatalogGroup.POST("/preview", dataCatalog.Preview)
	dataCatalogGroup.POST("/default", dataCatalog.Default)
	dataCatalogGroup.POST("/metadata", dataCatalog.UserMetadata)
	dataCatalogGroup.POST("/tag", dataCatalog.Tag)
	dataCatalogGroup.POST("/download-request", dataCatalog.DownloadRequest)
	dataCatalogGroup.POST("/comment/add", dataCatalog.AddComment)
	dataCatalogGroup.POST("/comment/update", dataCatalog.UpdateComment)
	dataCatalogGroup.POST("/comment/delete", dataCatalog.DelComment)
	// DCatalogGta Catalog Browser
	dataCatalogGroup.POST("/all-data/summary", dataCatalog.AllDataSummary)
	dataCatalogGroup.POST("/all-data", dataCatalog.AllData)

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
