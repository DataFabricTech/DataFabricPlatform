package injectors

import (
	"github.com/datafabric/gateway/controllers"
)

// Version version injector
type Version struct{}

// Init version controller create
func (Version) Init(in *Injector) *controllers.Version {
	return controllers.Version{}.New()
}
