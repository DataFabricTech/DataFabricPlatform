package services

import (
	"context"
	"fmt"
	pbPortal "github.com/datafabric/gateway/proto/datamodel"
	"github.com/sirupsen/logrus"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/status"
	"time"
)

// PortalService portal service struct
type PortalService struct {
	log           *logrus.Logger
	conn          *grpc.ClientConn
	serviceClient pbPortal.HelloServiceClient
}

// Initialize dsl parser client 초기화
func (PortalService) Initialize(log *logrus.Logger, host string, port int) (*PortalService, error) {
	service := new(PortalService)
	service.log = log

	address := fmt.Sprintf("%s:%d", host, port)
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	conn, err := grpc.DialContext(ctx, address,
		grpc.WithTransportCredentials(insecure.NewCredentials()),
		grpc.WithBlock())
	if err != nil {
		return nil, fmt.Errorf("can't connect DSL Parser[ %s ][ %v ]", address, err)
	}
	service.conn = conn
	service.serviceClient = pbPortal.NewHelloServiceClient(conn)

	service.log.Errorf("[ Portal Service Client ] Start ................................................... [ OK ]")
	return service, nil
}

// Destroy grpc client 종료
func (service *PortalService) Destroy() {
	_ = service.conn.Close()
}

func (service *PortalService) Hello(request *pbPortal.HelloRequest) (*pbPortal.HelloResponse, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	service.log.Infof("[Portal Service] >> Send Search")
	res, err := service.serviceClient.Hello(ctx, request)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[Portal Service] !! Error while Search. code[ %d ], msg[ %s ]", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
			// atomic.StoreInt32(&dslParserClient.state, models.GrpcDisconnected)
			// common.Sentry{}.CaptureException(fmt.Errorf("DSL Parser Err[ %s ]", errStatus.Message()))
		}
		// dslParserClient.statManager.Add(stat.DslParseFail)
		return nil, err
	}
	service.log.Infof("[Portal Service] << Receive Search")
	return res, nil
}
