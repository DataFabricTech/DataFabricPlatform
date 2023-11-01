package injectors

import (
	"github.com/datafabric/gateway/controllers"
	"github.com/datafabric/gateway/services"
)

// DataCatalogInjector portal injector
type DataCatalogInjector struct{}

// Init for interconnection [ controller(App) - Service(Repository) - repository - datastore ] : Dependency Injection
func (DataCatalogInjector) Init(in *Injector) *controllers.DataCatalogController {
	svc, err := services.DataCatalogServiceInitialize(in.Log.Logger, "localhost", 9360)
	if err != nil {
		return nil
	}
	return controllers.DataCatalogControllerInitialize(svc)
}
