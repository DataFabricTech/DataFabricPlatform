package services

import (
	"context"
	"fmt"
	pbPortal "github.com/datafabric/gateway/protobuf"
	"github.com/golang/protobuf/ptypes/empty"
	"github.com/sirupsen/logrus"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/status"
	"time"
)

const ServiceName = "PORTAL"

// PortalService portal service struct
type PortalService struct {
	log    *logrus.Logger
	conn   *grpc.ClientConn
	client pbPortal.PortalServiceClient
}

// PortalServiceInitialize Portal Service 초기화
func PortalServiceInitialize(log *logrus.Logger, host string, port int) (*PortalService, error) {
	service := new(PortalService)
	service.log = log

	address := fmt.Sprintf("%s:%d", host, port)
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	conn, err := grpc.DialContext(ctx, address,
		grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		return nil, fmt.Errorf("can't connect Data Fabric Portal Service[ %s ][ %v ]", address, err)
	}
	service.conn = conn
	service.client = pbPortal.NewPortalServiceClient(conn)

	service.log.Errorf("[ Portal Service ] Start .......................................................... [ OK ]")
	return service, nil
}

// Destroy grpc client 종료
func (service *PortalService) Destroy() {
	_ = service.conn.Close()
}

func (service *PortalService) Search(request *pbPortal.ReqSearch) (*pbPortal.ResSearch, error) {
	service.log.Infof("[%-10s] >> Search : Server", ServiceName)
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.Search(ctx, request)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%-10s] !! Error while Search. Code[ %d ], MSG[ %s ]", ServiceName, errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Search : Success", ServiceName)
	return res, nil
}

func (service *PortalService) RecentSearches() (*pbPortal.ResRecentSearches, error) {
	service.log.Infof("[%-10s] >> Recent Searches : Server", ServiceName)
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.RecentSearches(ctx, &empty.Empty{})
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Recent Searches. Code[ %d ], MSG[ %s ]", ServiceName, errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Recent Searches : Success", ServiceName)
	return res, nil
}
