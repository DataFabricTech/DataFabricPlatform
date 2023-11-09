package injectors

import (
	"github.com/datafabric/gateway/controllers"
	"github.com/datafabric/gateway/services"
)

// DataModelInjector portal injector
type DataModelInjector struct{}

// Init for interconnection [ controller(App) - Service(Repository) - repository - datastore ] : Dependency Injection
func (DataModelInjector) Init(in *Injector) *controllers.DataModelController {
	svc, err := services.DataModelServiceInitialize(in.Log.Logger,
		in.AppConfig.Services["core"].Host,
		in.AppConfig.Services["core"].Port)
	if err != nil {
		return nil
	}
	return controllers.DataModelControllerInitialize(svc)
}
