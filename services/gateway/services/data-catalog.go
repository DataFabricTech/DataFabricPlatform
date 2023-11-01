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
	"time"
)

// DataCatalogService data catalog service struct
type DataCatalogService struct {
	log    *logrus.Logger
	conn   *grpc.ClientConn
	client protobuf.DataCatalogServiceClient
}

// DataCatalogServiceInitialize DataCatalog Service 초기화
func DataCatalogServiceInitialize(log *logrus.Logger, host string, port int) (*DataCatalogService, error) {
	service := new(DataCatalogService)
	service.log = log

	address := fmt.Sprintf("%s:%d", host, port)
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	conn, err := grpc.DialContext(ctx, address,
		grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		return nil, fmt.Errorf("can't connect Data Fabric DataCatalog Service[ %s ][ %v ]", address, err)
	}
	service.conn = conn
	service.client = protobuf.NewDataCatalogServiceClient(conn)

	service.log.Errorf("[ DataCatalog Service ] Start ..................................................... [ OK ]")
	return service, nil
}

// Destroy grpc client 종료
func (service *DataCatalogService) Destroy() {
	_ = service.conn.Close()
}

// Preview 			POST /data/v1/preview 				- 미리보기
func (service *DataCatalogService) Preview(in *protobuf.ReqId) (*protobuf.DataCatalogPreview, error) {
	// Preview(in *protobuf.ReqId) (*protobuf.DataCatalogPreview, error)
	service.log.Infof("[%-10s] >> Preview : Server", "DATA_CATALOG")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.Preview(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Preview. Code[ %d ], MSG[ %s ]",
			"DATA_CATALOG", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Preview : Server", "DATA_CATALOG")
	return res, nil
}

// Default 			POST /data/v1/default 				- 데이터 상세 보기 - 기본 정보
func (service *DataCatalogService) Default(in *protobuf.ReqId) (*protobuf.DataCatalogDefault, error) {
	// Default(in *protobuf.ReqId) (*protobuf.DataCatalogDefault, error)
	service.log.Infof("[%-10s] >> Detail/Default : Server", "DATA_CATALOG")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.Default(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Default. Code[ %d ], MSG[ %s ]",
			"DATA_CATALOG", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Detail/Default : Server", "DATA_CATALOG")
	return res, nil
}

// UserMetadata 	POST /data/v1/metadata 				- 사용자 설정 메타 데이터 업데이트
func (service *DataCatalogService) UserMetadata(in *protobuf.ReqMetaUpdate) (*protobuf.CommonResponse, error) {
	// UserMetadata(in *protobuf.ReqMetaUpdate) (*protobuf.CommonResponse, error)
	service.log.Infof("[%-10s] >> User Metadata : Server", "DATA_CATALOG")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.UserMetadata(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while User Metadata. Code[ %d ], MSG[ %s ]",
			"DATA_CATALOG", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << User Metadata : Server", "DATA_CATALOG")
	return res, nil
}

// Tag 				POST /data/v1/tag 					- 데이터 태그 업데이트
func (service *DataCatalogService) Tag(in *protobuf.ReqTagUpdate) (*protobuf.CommonResponse, error) {
	// Tag(in *protobuf.ReqTagUpdate) (*protobuf.CommonResponse, error)
	service.log.Infof("[%-10s] >> Tag : Server", "DATA_CATALOG")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.Tag(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Tag. Code[ %d ], MSG[ %s ]",
			"DATA_CATALOG", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Tag : Server", "DATA_CATALOG")
	return res, nil
}

// DownloadRequest 	POST /data/v1/download-request 		- 다운로드 요청
func (service *DataCatalogService) DownloadRequest(in *protobuf.ReqId) (*protobuf.CommonResponse, error) {
	// DownloadRequest(in *protobuf.ReqId) (*protobuf.CommonResponse, error)
	service.log.Infof("[%-10s] >> Download Request : Server", "DATA_CATALOG")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.DownloadRequest(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Download Request. Code[ %d ], MSG[ %s ]",
			"DATA_CATALOG", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Download Request : Server", "DATA_CATALOG")
	return res, nil
}

// AddComment 		POST /data/v1/comment/add 			- 데이터 평가와 댓글 추가
func (service *DataCatalogService) AddComment(in *protobuf.ReqRatingAndComment) (*protobuf.CommonResponse, error) {
	// AddComment(in *protobuf.ReqRatingAndComment) (*protobuf.CommonResponse, error)
	service.log.Infof("[%-10s] >> Add RatingNComment : Server", "DATA_CATALOG")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.AddComment(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Add RatingNComment. Code[ %d ], MSG[ %s ]",
			"DATA_CATALOG", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Add RatingNComment: Server", "DATA_CATALOG")
	return res, nil
}

// UpdateComment 	POST /data/v1/comment/update 		- 데이터 평가와 댓글 업데이트
func (service *DataCatalogService) UpdateComment(in *protobuf.ReqRatingAndComment) (*protobuf.CommonResponse, error) {
	// UpdateComment(in *protobuf.ReqRatingAndComment) (*protobuf.CommonResponse, error)
	service.log.Infof("[%-10s] >> Update RatingNComment : Server", "DATA_CATALOG")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.UpdateComment(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Update RatingNComment. Code[ %d ], MSG[ %s ]",
			"DATA_CATALOG", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Update RatingNComment : Server", "DATA_CATALOG")
	return res, nil
}

// DelComment 		POST /data/v1/comment/delete 		- 데이터 평가와 댓글 삭제
func (service *DataCatalogService) DelComment(in *protobuf.ReqId) (*protobuf.CommonResponse, error) {
	// DelComment(in *protobuf.ReqId) (*protobuf.CommonResponse, error)
	service.log.Infof("[%-10s] >> Delete RatingNComment : Server", "DATA_CATALOG")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.DelComment(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while Delete RatingNComment. Code[ %d ], MSG[ %s ]",
			"DATA_CATALOG", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << Delete RatingNComment : Server", "DATA_CATALOG")
	return res, nil
}

// AllDataSummary 	POST /data/v1/all-data/summary 		- 데이터 브라우저 좌측 패널용 데이터
func (service *DataCatalogService) AllDataSummary(in *protobuf.DataCatalogSearch) (*protobuf.ResDataCatalogs, error) {
	// AllDataSummary(in *protobuf.DataCatalogSearch) (*protobuf.ResDataCatalogs, error)
	service.log.Infof("[%-10s] >> All Data Summary(Left): Server", "DATA_CATALOG")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.AllDataSummary(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while All Data Summary. Code[ %d ], MSG[ %s ]",
			"DATA_CATALOG", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << All Data Summary : Server", "DATA_CATALOG")
	return res, nil
}

// AllData 			POST /data/v1/all-data          	- 데이터 브라우저 조회(검색) 데이터
func (service *DataCatalogService) AllData(in *protobuf.DataCatalogSearch) (*protobuf.ResDataCatalogs, error) {
	// AllData(in *protobuf.DataCatalogSearch) (*protobuf.ResDataCatalogs, error)
	service.log.Infof("[%-10s] >> All Data : Server", "DATA_CATALOG")
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	res, err := service.client.AllData(ctx, in)
	if err != nil {
		errStatus, _ := status.FromError(err)
		service.log.Errorf("[%10s] !! Error while All Data. Code[ %d ], MSG[ %s ]",
			"DATA_CATALOG", errStatus.Code(), errStatus.Message())
		switch errStatus.Code() {
		case codes.DeadlineExceeded, codes.Unavailable:
			_ = service.conn.Close()
		}
		return nil, err
	}
	service.log.Infof("[%-10s] << All Data : Server", "DATA_CATALOG")
	return res, nil
}
