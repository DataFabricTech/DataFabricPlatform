package service

import (
	"context"
	"github.com/datafabric/gateway/protobuf"
	"google.golang.org/protobuf/types/known/emptypb"
)

type AdaptorService struct {
	protobuf.UnimplementedAdaptorServiceServer
}

func (service *AdaptorService) GetStorageType(ctx context.Context, req *emptypb.Empty) (*protobuf.ResSupportedStorageType, error) {
	res := &protobuf.ResSupportedStorageType{
		Code: "200",
		Data: &protobuf.ResSupportedStorageType_Data{
			SupportedStorageType: []*protobuf.SupportedStorageType{
				{
					Name: "IRIS",
					Icon: []byte("bytes....123097598745923045"),
					ConnSchema: []*protobuf.InputField{
						{
							Key:         "HOST",
							Required:    true,
							ValueType:   "STRING",
							Default:     "IP address or Domain",
							Description: "Data Storage IP Address or Domain",
						},
						{
							Key:         "PORT",
							Required:    true,
							ValueType:   "NUMBER",
							Default:     "Port Number",
							Description: "Data Storage Port Number",
						},
						{
							Key:         "DATABASE",
							Required:    true,
							ValueType:   "string",
							Default:     "DatabaseName",
							Description: "Database Name of Data Storage",
						},
					},
					AuthSchema: []*protobuf.AuthSchema{
						{
							AuthType:        protobuf.AuthType_NONE,
							AuthInputFields: []*protobuf.InputField{},
						},
						{
							AuthType: protobuf.AuthType_USER_PASSWORD,
							AuthInputFields: []*protobuf.InputField{
								{
									Key:         "USER",
									Required:    true,
									ValueType:   "string",
									Default:     "storage-user-id",
									Description: "Data Storage Authentication USER Identity",
								},
								{
									Key:         "PASSWORD",
									Required:    true,
									ValueType:   "string",
									Default:     "paSsW0rd",
									Description: "Data Storage Authentication USER Password",
								},
							},
						},
					},
				},
				{
					Name: "MariaDB",
					Icon: []byte("bytes....123097598745923045"),
					ConnSchema: []*protobuf.InputField{
						{
							Key:         "HOST",
							Required:    true,
							ValueType:   "STRING",
							Default:     "IP address or Domain",
							Description: "Data Storage IP Address or Domain",
						},
						{
							Key:         "PORT",
							Required:    true,
							ValueType:   "NUMBER",
							Default:     "Port Number",
							Description: "Data Storage Port Number",
						},
						{
							Key:         "DATABASE",
							Required:    true,
							ValueType:   "string",
							Default:     "DatabaseName",
							Description: "Database Name of Data Storage",
						},
					},
					AuthSchema: []*protobuf.AuthSchema{
						{
							AuthType:        protobuf.AuthType_NONE,
							AuthInputFields: []*protobuf.InputField{},
						},
						{
							AuthType: protobuf.AuthType_USER_PASSWORD,
							AuthInputFields: []*protobuf.InputField{
								{
									Key:         "USER",
									Required:    true,
									ValueType:   "string",
									Default:     "storage-user-id",
									Description: "Data Storage Authentication USER Identity",
								},
								{
									Key:         "PASSWORD",
									Required:    true,
									ValueType:   "string",
									Default:     "paSsW0rd",
									Description: "Data Storage Authentication USER Password",
								},
							},
						},
					},
				},
				{
					Name: "MySQL",
					Icon: []byte("bytes....0987654310980862345"),
					ConnSchema: []*protobuf.InputField{
						{
							Key:         "HOST",
							Required:    true,
							ValueType:   "STRING",
							Default:     "IP address or Domain",
							Description: "Data Storage IP Address or Domain",
						},
						{
							Key:         "PORT",
							Required:    true,
							ValueType:   "NUMBER",
							Default:     "Port Number",
							Description: "Data Storage Port Number",
						},
						{
							Key:         "DATABASE",
							Required:    true,
							ValueType:   "string",
							Default:     "DatabaseName",
							Description: "Database Name of Data Storage",
						},
					},
					AuthSchema: []*protobuf.AuthSchema{
						{
							AuthType:        protobuf.AuthType_NONE,
							AuthInputFields: []*protobuf.InputField{},
						},
						{
							AuthType: protobuf.AuthType_USER_PASSWORD,
							AuthInputFields: []*protobuf.InputField{
								{
									Key:         "USER",
									Required:    true,
									ValueType:   "string",
									Default:     "storage-user-id",
									Description: "Data Storage Authentication USER Identity",
								},
								{
									Key:         "PASSWORD",
									Required:    true,
									ValueType:   "string",
									Default:     "paSsW0rd",
									Description: "Data Storage Authentication USER Password",
								},
							},
						},
					},
				},
			},
		},
	}
	return res, nil
}

func (service *AdaptorService) GetAdaptors(ctx context.Context, req *protobuf.ReqAdaptors) (*protobuf.ResAdaptors, error) {
	res := &protobuf.ResAdaptors{
		Code: "200",
		Data: &protobuf.ResAdaptors_Data{
			Adaptors: []*protobuf.Adaptor{
				{
					Id:          "adaptor id 01",
					Name:        "adaptor name 01",
					StorageType: req.GetStorageType(),
					Version:     "0.1",
					Path:        "/data-fabric/adaptor/adaptor-id-01.jar",
					Class:       "org.driver.jdbc",
					SupportedURL: []string{
						"jdbc://host:port/database",
						"jdbc://host:port/database?opt=opt",
						"jdbc://host:port/database/opt?opt=opt",
					},
					BasicOptions: []*protobuf.InputField{
						{
							Key:         "HOST",
							Required:    true,
							ValueType:   "STRING",
							Default:     "IP address or Domain",
							Description: "Data Storage IP Address or Domain",
						},
						{
							Key:         "PORT",
							Required:    true,
							ValueType:   "NUMBER",
							Default:     "Port Number",
							Description: "Data Storage Port Number",
						},
						{
							Key:         "DATABASE",
							Required:    true,
							ValueType:   "string",
							Default:     "DatabaseName",
							Description: "Database Name of Data Storage",
						},
						{
							Key:         "USER",
							Required:    true,
							ValueType:   "string",
							Default:     "storage-user-id",
							Description: "Data Storage Authentication USER Identity",
						},
						{
							Key:         "PASSWORD",
							Required:    true,
							ValueType:   "string",
							Default:     "paSsW0rd",
							Description: "Data Storage Authentication USER Password",
						},
					},
					AdditionalOptions: []*protobuf.InputField{
						{
							Key:         "addOptions01",
							Required:    false,
							ValueType:   "string",
							Default:     "add-option-sample",
							Description: "Option Sample Desc",
						},
						{
							Key:         "addOptions02",
							Required:    true,
							ValueType:   "string",
							Default:     "add-option-sample-02",
							Description: "Option Sample Desc 02",
						},
					},
				},
				{
					Id:          "adaptor id 02",
					Name:        "adaptor name 02",
					StorageType: req.GetStorageType(),
					Version:     "0.2",
					Path:        "/data-fabric/adaptor/adaptor-id-02.jar",
					Class:       "org.driver.jdbc",
					SupportedURL: []string{
						"jdbc://host:port/database",
						"jdbc://host:port/database?opt=opt",
						"jdbc://host:port/database/opt?opt=opt",
					},
					BasicOptions: []*protobuf.InputField{
						{
							Key:         "HOST",
							Required:    true,
							ValueType:   "STRING",
							Default:     "IP address or Domain",
							Description: "Data Storage IP Address or Domain",
						},
						{
							Key:         "PORT",
							Required:    true,
							ValueType:   "NUMBER",
							Default:     "Port Number",
							Description: "Data Storage Port Number",
						},
						{
							Key:         "DATABASE",
							Required:    true,
							ValueType:   "string",
							Default:     "DatabaseName",
							Description: "Database Name of Data Storage",
						},
						{
							Key:         "USER",
							Required:    true,
							ValueType:   "string",
							Default:     "storage-user-id",
							Description: "Data Storage Authentication USER Identity",
						},
						{
							Key:         "PASSWORD",
							Required:    true,
							ValueType:   "string",
							Default:     "paSsW0rd",
							Description: "Data Storage Authentication USER Password",
						},
					},
					AdditionalOptions: []*protobuf.InputField{
						{
							Key:         "addOptions01",
							Required:    false,
							ValueType:   "string",
							Default:     "add-option-sample",
							Description: "Option Sample Desc",
						},
						{
							Key:         "addOptions02",
							Required:    true,
							ValueType:   "string",
							Default:     "add-option-sample-02",
							Description: "Option Sample Desc 02",
						},
						{
							Key:         "addOptions03",
							Required:    true,
							ValueType:   "string",
							Default:     "add-option-sample-03",
							Description: "Option Sample Desc 03",
						},
					},
				},
				{
					Id:          "adaptor id 03",
					Name:        "adaptor name 03",
					StorageType: req.GetStorageType(),
					Version:     "0.3",
					Path:        "/data-fabric/adaptor/adaptor-id-03.jar",
					Class:       "org.driver.jdbc",
					SupportedURL: []string{
						"jdbc://host:port/database",
						"jdbc://host:port/database?opt=opt",
					},
					BasicOptions: []*protobuf.InputField{
						{
							Key:         "HOST",
							Required:    true,
							ValueType:   "STRING",
							Default:     "IP address or Domain",
							Description: "Data Storage IP Address or Domain",
						},
						{
							Key:         "PORT",
							Required:    true,
							ValueType:   "NUMBER",
							Default:     "Port Number",
							Description: "Data Storage Port Number",
						},
						{
							Key:         "DATABASE",
							Required:    true,
							ValueType:   "string",
							Default:     "DatabaseName",
							Description: "Database Name of Data Storage",
						},
						{
							Key:         "SCOPE",
							Required:    true,
							ValueType:   "string",
							Default:     "scope",
							Description: "scope of Data Storage",
						},
						{
							Key:         "USER",
							Required:    true,
							ValueType:   "string",
							Default:     "storage-user-id",
							Description: "Data Storage Authentication USER Identity",
						},
						{
							Key:         "PASSWORD",
							Required:    true,
							ValueType:   "string",
							Default:     "paSsW0rd",
							Description: "Data Storage Authentication USER Password",
						},
					},
					AdditionalOptions: []*protobuf.InputField{
						{
							Key:         "addOptions02",
							Required:    true,
							ValueType:   "string",
							Default:     "add-option-sample-02",
							Description: "Option Sample Desc 02",
						},
						{
							Key:         "addOptions03",
							Required:    true,
							ValueType:   "string",
							Default:     "add-option-sample-03",
							Description: "Option Sample Desc 03",
						},
					},
				},
			},
		},
	}
	return res, nil
}
