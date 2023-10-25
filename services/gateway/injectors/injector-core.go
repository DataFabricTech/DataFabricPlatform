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

	// Grouping
	portalGroup := h.Router.Group("/portal/v1")

	// Sample
	h.Log.Errorf("[ PATH ] /portal/v1 ............................................................... [ OK ]")
	portal := PortalInjector{}.Init(h)
	portalGroup.POST("/search", portal.Search)
	return nil
}
