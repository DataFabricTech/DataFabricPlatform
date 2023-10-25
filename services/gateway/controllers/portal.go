package controllers

import (
	"context"
	"fmt"
	pbPortal "github.com/datafabric/gateway/proto/datamodel"
	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/status"
	"net/http"
	"time"

	"github.com/datafabric/gateway/common"
	// "github.com/datafabric/gateway/models"
)

// PortalUsecase portal service
type PortalUsecase interface {
	// Search() (*[]models.Sample, error)
	// RecentSearches() (*models.Sample, error)
	Hello(*pbPortal.HelloRequest) (*pbPortal.HelloResponse, error)
}

// type Endpoint interface {
// 	GetEndpoint() (string, int, error)
// }

// Portal Controller
type Portal struct {
	Log     *logrus.Logger
	Usecase PortalUsecase
}

// New create Sample instance.
func (Portal) New(usecase PortalUsecase) *Portal {
	return &Portal{
		Log:     common.Logger{}.GetInstance().Logger,
		Usecase: usecase,
	}
}

// Search for user search
func (controller *Portal) Search(c echo.Context) error {
	request := new(pbPortal.HelloRequest)
	err := c.Bind(request)
	if err != nil {
		return err
	}
	// res, _ := controller.Usecase.Hello(search)

	address := fmt.Sprintf("%s:%d", "localhost", 9360)
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	conn, err := grpc.DialContext(ctx, address,
		grpc.WithTransportCredentials(insecure.NewCredentials()),
		grpc.WithBlock())
	if err != nil {
		controller.Log.Errorf("can't connect backend[ %s ][ %v ]", address, err)
		return err
	}
	defer func(conn *grpc.ClientConn) {
		_ = conn.Close()
	}(conn)
	serviceClient := pbPortal.NewHelloServiceClient(conn)
	ctx, cancel = context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	controller.Log.Infof("[Portal Service] >> Send Search")
	res, err := serviceClient.Hello(ctx, request)
	if err != nil {
		errStatus, _ := status.FromError(err)
		controller.Log.Errorf("[Portal Service] !! Error while Search. code[ %d ], msg[ %s ]", errStatus.Code(), errStatus.Message())
		// switch errStatus.Code() {
		// case codes.DeadlineExceeded, codes.Unavailable:
		// _ = conn.Close()
		// atomic.StoreInt32(&dslParserClient.state, models.GrpcDisconnected)
		// common.Sentry{}.CaptureException(fmt.Errorf("DSL Parser Err[ %s ]", errStatus.Message()))
		// }
		// dslParserClient.statManager.Add(stat.DslParseFail)
		return err
	}
	controller.Log.Infof("[Portal Service] << Receive Search")
	return c.JSON(http.StatusOK, res)
}

// GetByID return sample whoes ID mathces
// func (controller *Sample) GetByID(c echo.Context) error {
// 	id, err := strconv.Atoi(c.Param("id"))
// 	if err != nil {
// 		return c.JSON(http.StatusBadRequest, err)
// 	}
// 	sample, err := controller.Usecase.GetByID(id)
// 	if err != nil {
// 		if err == common.ErrNoHaveResult {
// 			return c.JSON(http.StatusOK, sample)
// 		}
// 		return c.JSON(http.StatusInternalServerError, err)
// 	}
// 	return c.JSON(http.StatusOK, sample)
// }

// // Create create a new ...
// func (controller *Sample) Create(c echo.Context) error {
// 	input := new(models.Sample)
// 	c.Bind(input)
// 	sample, err := controller.Usecase.Create(input)
// 	if err != nil {
// 		return c.JSON(http.StatusInternalServerError, err)
// 	}
// 	return c.JSON(http.StatusCreated, sample)
// }
//
// // Update update from input
// func (controller *Sample) Update(c echo.Context) error {
// 	input := new(models.Sample)
// 	c.Bind(input)
// 	sample, err := controller.Usecase.Update(input)
// 	if err != nil {
// 		return c.JSON(http.StatusInternalServerError, err)
// 	}
// 	return c.JSON(http.StatusOK, sample)
// }
//
// // Delete delete sample from id
// func (controller *Sample) Delete(c echo.Context) error {
// 	id, err := strconv.Atoi(c.Param("id"))
// 	if err != nil {
// 		return c.JSON(http.StatusBadRequest, err)
// 	}
// 	sample, err := controller.Usecase.Delete(id)
// 	if err != nil {
// 		return c.JSON(http.StatusInternalServerError, err)
// 	}
// 	return c.JSON(http.StatusOK, sample)
// }
