package injectors

import (
	"github.com/datafabric/gateway/controllers"
	"github.com/datafabric/gateway/services"
)

// AdaptorInjector portal injector
type AdaptorInjector struct{}

// Init for interconnection [ controller(App) - Service(Repository) - repository - datastore ] : Dependency Injection
func (AdaptorInjector) Init(in *Injector) *controllers.AdaptorController {
	service, err := services.AdaptorServiceInitialize(in.Log.Logger,
		in.AppConfig.Services["core"].Host,
		in.AppConfig.Services["core"].Port)
	if err != nil {
		return nil
	}
	return controllers.AdaptorControllerInitialize(service)
}
