package services

import (
	"context"
	"fmt"
	"github.com/datafabric/gateway/protobuf"
	"github.com/sirupsen/logrus"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/connectivity"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/emptypb"
	"time"
)

// AdaptorService adaptor service struct
type AdaptorService struct {
	log    *logrus.Logger
	conn   *grpc.ClientConn
	client protobuf.AdaptorServiceClient
}

// AdaptorServiceInitialize Adaptor Service 초기화
func AdaptorServiceInitialize(log *logrus.Logger, host string, port int) (*AdaptorService, error) {
	service := new(AdaptorService)
	service.log = log

	address := fmt.Sprintf("%s:%d", host, port)
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	conn, err := grpc.DialContext(ctx, address,
		grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		return nil, fmt.Errorf("can't connect Data Fabric Adaptor Service[ %s ][ %v ]", address, err)
	}
	service.conn = conn
	service.client = protobuf.NewAdaptorServiceClient(conn)

	service.log.Errorf("[ Adaptor Service ] Start ......................................................... [ OK ]")
	return service, nil
}

// Destroy grpc client 종료
func (service *AdaptorService) Destroy() {
	_ = service.conn.Close()
}

func (service *AdaptorService) Reconnect() {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	conn, err := grpc.DialContext(ctx, service.conn.Target(),
		grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		service.log.Errorf("can't connect Data Fabric Adaptor Service[ %s ][ %v ]", service.conn.Target(), err)
		return
	}
	service.conn = conn
	service.client = protobuf.NewAdaptorServiceClient(conn)
}

// SupportedStorageType : GET /storage/v1/storage-type:
func (service *AdaptorService) SupportedStorageType() (*protobuf.ResSupportedStorageType, error) {
	if service.conn.GetState() != connectivity.Ready {
		service.Reconnect()
		return nil, fmt.Errorf("can't connect Data Fabric Service[ %s ][ %v ]", service.conn.Target(), "Not Ready")
	}
	service.log.Infof("[%-10s] >> Supported Storage Type : Server", "ADAPTOR")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.GetStorageType(ctx, &emptypb.Empty{})
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%-10s] !! Error while Get Supported Storage Type. Code[ %d ], MSG[ %s ]",
			"ADAPTOR", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Supported Storage Type : Success", "ADAPTOR")
	return res, nil
}

// GetAdaptors : GET /storage/v1/adaptors?storage-type=xxx
func (service *AdaptorService) GetAdaptors(req *protobuf.ReqAdaptors) (*protobuf.ResAdaptors, error) {
	if service.conn.GetState() != connectivity.Ready {
		service.Reconnect()
		return nil, fmt.Errorf("can't connect Data Fabric Service[ %s ][ %v ]", service.conn.Target(), "Not Ready")
	}
	service.log.Infof("[%-10s] >> Get Adaptors : Server", "ADAPTOR")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.GetAdaptors(ctx, req)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Get Adaptors. Code[ %d ], MSG[ %s ]",
			"ADAPTOR", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Get Adaptors : Success", "ADAPTOR")
	return res, nil
}
