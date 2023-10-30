package injectors

import (
	"github.com/mobigen/golang-web-template/controllers"
	"github.com/mobigen/golang-web-template/repositories"
	"github.com/mobigen/golang-web-template/services"
)

// Sample sample injector
type Sample struct{}

// Init for interconnection [ controller(App) - Service(Repository) - repository - datastore ] : Dependency Injection
func (Sample) Init(in *Injector) *controllers.Sample {
	repo := repositories.Sample{}.New(in.Datastore)
	svc := services.Sample{}.New(repo)
	return controllers.Sample{}.New(svc)
}
