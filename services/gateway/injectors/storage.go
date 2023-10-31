package injectors

import (
	"github.com/datafabric/gateway/controllers"
	"github.com/datafabric/gateway/services"
)

// StorageInjector portal injector
type StorageInjector struct{}

// Init for interconnection [ controller(App) - Service(Repository) - repository - datastore ] : Dependency Injection
func (StorageInjector) Init(in *Injector) *controllers.StorageController {
	service, err := services.StorageServiceInitialize(in.Log.Logger, "localhost", 9360)
	if err != nil {
		return nil
	}
	return controllers.StorageControllerInitialize(service)
}
