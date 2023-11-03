package services

import (
	"context"
	"fmt"
	"github.com/datafabric/gateway/protobuf"
	"github.com/golang/protobuf/ptypes/empty"
	"github.com/sirupsen/logrus"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/connectivity"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/status"
	"time"
)

// PortalService portal service struct
type PortalService struct {
	log    *logrus.Logger
	conn   *grpc.ClientConn
	client protobuf.PortalServiceClient
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
	service.client = protobuf.NewPortalServiceClient(conn)

	service.log.Errorf("[ Portal Service ] Start .......................................................... [ OK ]")
	return service, nil
}

// Destroy grpc client 종료
func (service *PortalService) Destroy() {
	_ = service.conn.Close()
}

func (service *PortalService) Reconnect() {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	conn, err := grpc.DialContext(ctx, service.conn.Target(),
		grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		service.log.Errorf("can't connect Data Fabric Portal Service[ %s ][ %v ]", service.conn.Target(), err)
		return
	}
	service.conn = conn
	service.client = protobuf.NewPortalServiceClient(conn)
}

func (service *PortalService) Search(request *protobuf.ReqSearch) (*protobuf.ResSearch, error) {
	if service.conn.GetState() != connectivity.Ready {
		service.Reconnect()
		return nil, fmt.Errorf("can't connect Data Fabric Portal Service[ %s ][ %v ]", service.conn.Target(), "Not Ready")
	}
	service.log.Infof("[%-10s] >> Search : Server", "PORTAL")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.Search(ctx, request)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%-10s] !! Error while Search. Code[ %d ], MSG[ %s ]", "PORTAL", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Search : Success", "PORTAL")
	return res, nil
}

func (service *PortalService) RecentSearches() (*protobuf.ResRecentSearches, error) {
	if service.conn.GetState() != connectivity.Ready {
		service.Reconnect()
		return nil, fmt.Errorf("can't connect Data Fabric Portal Service[ %s ][ %v ]", service.conn.Target(), "Not Ready")
	}
	service.log.Infof("[%-10s] >> Recent Searches : Server", "PORTAL")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.RecentSearches(ctx, &empty.Empty{})
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Recent Searches. Code[ %d ], MSG[ %s ]", "PORTAL", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Recent Searches : Success", "PORTAL")
	return res, nil
}
