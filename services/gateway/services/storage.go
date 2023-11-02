package services

import (
	"context"
	"fmt"
	"github.com/datafabric/gateway/protobuf"
	"github.com/sirupsen/logrus"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/emptypb"
	"time"
)

// StorageService storage service struct
type StorageService struct {
	log    *logrus.Logger
	conn   *grpc.ClientConn
	client protobuf.StorageServiceClient
}

// StorageServiceInitialize Storage Service 초기화
func StorageServiceInitialize(log *logrus.Logger, host string, port int) (*StorageService, error) {
	service := new(StorageService)
	service.log = log

	address := fmt.Sprintf("%s:%d", host, port)
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	conn, err := grpc.DialContext(ctx, address,
		grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		return nil, fmt.Errorf("can't connect Data Fabric Storage Service[ %s ][ %v ]", address, err)
	}
	service.conn = conn
	service.client = protobuf.NewStorageServiceClient(conn)

	service.log.Errorf("[ Storage Service ] Start ......................................................... [ OK ]")
	return service, nil
}

// Destroy grpc client 종료
func (service *StorageService) Destroy() {
	_ = service.conn.Close()
}

// Overview GET    : /storage/v1/overview
func (service *StorageService) Overview() (*protobuf.ResStorageOverview, error) {
	// Overview(ctx context.Context, in *empty.Empty, opts ...grpc.CallOption) (*ResStorageOverview, error)
	service.log.Infof("[%-10s] >> Overview : Server", "STORAGE")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.Overview(ctx, &emptypb.Empty{})
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Overview. Code[ %d ], MSG[ %s ]",
			"STORAGE", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Overview : Success", "STORAGE")
	return res, nil
}

// Search POST   : /storage/v1/search
func (service *StorageService) Search(in *protobuf.ReqStorageSearch) (*protobuf.ResStorages, error) {
	// Search(ctx context.Context, in *ReqStorageSearch, opts ...grpc.CallOption) (*ResStorage, error)
	service.log.Infof("[%-10s] >> Search : Server", "STORAGE")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.Search(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Search. Code[ %d ], MSG[ %s ]",
			"STORAGE", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Search : Success", "STORAGE")
	return res, nil
}

// Status POST   : /storage/v1/status
func (service *StorageService) Status(in *protobuf.ReqId) (*protobuf.ResStorage, error) {
	// Status(ctx context.Context, in *ReqId, opts ...grpc.CallOption) (*ResStorage, error)
	service.log.Infof("[%-10s] >> Status : Server", "STORAGE")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.Status(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Status . Code[ %d ], MSG[ %s ]",
			"STORAGE", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Status : Success", "STORAGE")
	return res, nil
}

// Default POST   : /storage/v1/default
func (service *StorageService) Default(in *protobuf.ReqId) (*protobuf.ResStorage, error) {
	// Default(ctx context.Context, in *ReqId, opts ...grpc.CallOption) (*ResStorage, error)
	service.log.Infof("[%-10s] >> Default : Server", "STORAGE")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.Default(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Default. Code[ %d ], MSG[ %s ]",
			"STORAGE", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Default : Success", "STORAGE")
	return res, nil
}

// Advanced POST   : /storage/v1/advanced
func (service *StorageService) Advanced(in *protobuf.ReqId) (*protobuf.ResStorage, error) {
	// Advanced(ctx context.Context, in *ReqId, opts ...grpc.CallOption) (*ResStorage, error)
	service.log.Infof("[%-10s] >> Advanced : Server", "STORAGE")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.Advanced(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Advanced. Code[ %d ], MSG[ %s ]",
			"STORAGE", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Advanced : Success", "STORAGE")
	return res, nil
}

// Browse POST   : /storage/v1/browse
func (service *StorageService) Browse(in *protobuf.ReqStorageBrowse) (*protobuf.ResStorageBrowse, error) {
	// Browse(ctx context.Context, in *ReqStorageBrowse, opts ...grpc.CallOption) (*ResStorageBrowse, error)
	service.log.Infof("[%-10s] >> Browse : Server", "STORAGE")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.Browse(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Browse. Code[ %d ], MSG[ %s ]",
			"STORAGE", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Browse : Success", "STORAGE")
	return res, nil
}

// BrowseDefault POST   : /storage/v1/browse/default
func (service *StorageService) BrowseDefault(in *protobuf.ReqStorageBrowse) (*protobuf.ResStorageBrowseDefault, error) {
	// BrowseDefault(ctx context.Context, in *ReqStorageBrowse, opts ...grpc.CallOption) (*ResStorageBrowseDefault, error)
	service.log.Infof("[%-10s] >> Browse / Default : Server", "STORAGE")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.BrowseDefault(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Browse / Default . Code[ %d ], MSG[ %s ]",
			"STORAGE", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Browse / Default : Success", "STORAGE")
	return res, nil
}

// ConnectTest POST   : /storage/v1/connect-test
func (service *StorageService) ConnectTest(in *protobuf.ConnInfo) (*protobuf.CommonResponse, error) {
	// ConnectTest(ctx context.Context, in *ConnInfo, opts ...grpc.CallOption) (*CommonResponse, error)
	service.log.Infof("[%-10s] >> Connect Test : Server", "STORAGE")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.ConnectTest(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Connect Test. Code[ %d ], MSG[ %s ]",
			"STORAGE", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Connect Test : Success", "STORAGE")
	return res, nil
}

// AddStorage POST   : /storage/v1/add
func (service *StorageService) AddStorage(in *protobuf.Storage) (*protobuf.CommonResponse, error) {
	// AddStorage(ctx context.Context, in *Storage, opts ...grpc.CallOption) (*CommonResponse, error)
	service.log.Infof("[%-10s] >> AddStorage : Server", "STORAGE")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.AddStorage(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while AddStorage. Code[ %d ], MSG[ %s ]",
			"STORAGE", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << AddStorage : Success", "STORAGE")
	return res, nil
}

// UpdateStorage POST   : /storage/v1/modify
func (service *StorageService) UpdateStorage(in *protobuf.Storage) (*protobuf.CommonResponse, error) {
	// UpdateStorage(ctx context.Context, in *Storage, opts ...grpc.CallOption) (*CommonResponse, error)
	service.log.Infof("[%-10s] >> UpdateStorage : Server", "STORAGE")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.UpdateStorage(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while UpdateStorage. Code[ %d ], MSG[ %s ]",
			"STORAGE", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << UpdateStorage : Success", "STORAGE")
	return res, nil
}

// ConnectedData POST   : /storage/v1/connected-data/{storage-id}
func (service *StorageService) ConnectedData(in *protobuf.ReqId) (*protobuf.ResConnectedData, error) {
	// ConnectedData(ctx context.Context, in *ReqId, opts ...grpc.CallOption) (*ResConnectedData, error)
	service.log.Infof("[%-10s] >> Connected Data Count : Server", "STORAGE")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.ConnectedData(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Connected Data Count. Code[ %d ], MSG[ %s ]",
			"STORAGE", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Connected Data Count : Success", "STORAGE")
	return res, nil
}

// DeleteStorage POST   : /storage/v1/delete
func (service *StorageService) DeleteStorage(in *protobuf.ReqId) (*protobuf.CommonResponse, error) {
	// DeleteStorage(ctx context.Context, in *ReqId, opts ...grpc.CallOption) (*CommonResponse, error)
	service.log.Infof("[%-10s] >> DeleteStorage : Server", "STORAGE")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.DeleteStorage(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while DeleteStorage. Code[ %d ], MSG[ %s ]",
			"STORAGE", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << DeleteStorage : Success", "STORAGE")
	return res, nil
}
