package main

import (
	"github.com/datafabric/gateway/protobuf"
	"github.com/datafabric/gateway/tests/service"
	"github.com/labstack/gommon/log"
	"google.golang.org/grpc"
	"net"
)

func main() {
	lis, err := net.Listen("tcp", ":9360")
	if err != nil {
		log.Printf("failed to listen: %v", err)
	}
	defer func(lis net.Listener) {
		err := lis.Close()
		if err != nil {
			log.Printf("failed to close: %v", err)
		}
	}(lis)
	server := grpc.NewServer()
	protalService := &service.PortalService{}
	protobuf.RegisterPortalServiceServer(server, protalService)
	adaptorService := &service.AdaptorService{}
	protobuf.RegisterAdaptorServiceServer(server, adaptorService)
	storageService := &service.StorageService{}
	protobuf.RegisterStorageServiceServer(server, storageService)
	dataCatalogService := &service.DataModelService{}
	protobuf.RegisterDataModelServiceServer(server, dataCatalogService)
	log.Printf("test server listening at %v", lis.Addr())
	if err := server.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
