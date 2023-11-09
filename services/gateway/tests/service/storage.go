package service

import (
	"context"
	"github.com/datafabric/gateway/protobuf"
	"github.com/golang/protobuf/ptypes/empty"
)

type StorageService struct {
	protobuf.UnimplementedStorageServiceServer
}

func (service *StorageService) Overview(ctx context.Context, in *empty.Empty) (*protobuf.ResStorageOverview, error) {
	res := &protobuf.ResStorageOverview{
		Code: "200",
		Data: &protobuf.ResStorageOverview_Data{
			StorageTypeCount: []*protobuf.StorageTypeCount{
				{
					StorageType: "IRIS",
					Count:       2,
				},
				{
					StorageType: "MySQL",
					Count:       1,
				},
			},
			StorageStatusCount: []*protobuf.StorageStatusCount{
				{
					Status: int32(protobuf.Status_CONNECTED.Number()),
					Count:  3,
				},
				{
					Status: int32(protobuf.Status_DISCONNECTED.Number()),
					Count:  10,
				},
			},
			StorageStatistics: []*protobuf.StorageStatistics{
				{
					Time:            "2023-01-01 00:00:00.123",
					Id:              "storage-id-01",
					Name:            "storage-name-01",
					Access:          10,
					TotalData:       30,
					RegisteredData:  20,
					AvgResponseTime: 1.5,
				},
				{
					Time:            "2023-01-01 00:00:00.123",
					Id:              "storage-id-02",
					Name:            "storage-name-02",
					Access:          20,
					TotalData:       30,
					RegisteredData:  20,
					AvgResponseTime: 1.5,
				},
				{
					Time:            "2023-01-01 00:00:00.123",
					Id:              "storage-id-03",
					Name:            "storage-name-03",
					Access:          30,
					TotalData:       30,
					RegisteredData:  20,
					AvgResponseTime: 1.5,
				},
				{
					Time:            "2023-01-01 00:00:00.123",
					Id:              "storage-id-04",
					Name:            "storage-name-04",
					Access:          40,
					TotalData:       30,
					RegisteredData:  20,
					AvgResponseTime: 1.5,
				},
			},
			StorageDataCount: []*protobuf.StorageDataCount{
				{
					Id:         "IRIS",
					Name:       "IRIS",
					Total:      100,
					Registered: 10,
				},
				{
					Id:         "MySQL",
					Name:       "MySQL",
					Total:      0,
					Registered: 0,
				},
			},
			StorageResponseTime: []*protobuf.StorageResponseTime{
				{
					Id:           "storage-id-01",
					Name:         "storage-name-01",
					ResponseTime: 0.1,
				},
				{
					Id:           "storage-id-02",
					Name:         "storage-name-02",
					ResponseTime: 0.2,
				},
				{
					Id:           "storage-id-03",
					Name:         "storage-name-03",
					ResponseTime: 0.3,
				},
				{
					Id:           "storage-id-04",
					Name:         "storage-name-04",
					ResponseTime: 0.4,
				},
				{
					Id:           "storage-id-05",
					Name:         "storage-name-05",
					ResponseTime: 0.5,
				},
				{
					Id:           "storage-id-06",
					Name:         "storage-name-06",
					ResponseTime: 0.6,
				},
				{
					Id:           "storage-id-07",
					Name:         "storage-name-07",
					ResponseTime: 0.7,
				},
			},
			History: []*protobuf.StorageHistory{
				{
					Time: &protobuf.DateTime{
						StrDateTime: "2023-01-02 00:00:00.123",
						UtcTime:     1029839845769,
					},
					Id:   "storage id 01",
					Name: "storage name 01",
					ModifiedBy: &protobuf.User{
						Id:       "user 01",
						Name:     "user name",
						Nickname: "user nickname",
						Phone:    "01234815125123",
						Email:    "0123512@123987.com",
					},
					ModifiedInfos: []*protobuf.StorageModifiedInfo{
						{
							Cmd:      protobuf.StorageModifiedInfo_CREATE,
							Key:      "name",
							OldValue: "",
							NewValue: "new Name",
						},
						{
							Cmd:      protobuf.StorageModifiedInfo_UPDATE,
							Key:      "description",
							OldValue: "old desc",
							NewValue: "new desc",
						},
					},
				},
				{
					Time: &protobuf.DateTime{
						StrDateTime: "2023-01-03 00:00:00.123",
						UtcTime:     1029839845769,
					},
					Id:   "storage id 01",
					Name: "storage name 01",
					ModifiedBy: &protobuf.User{
						Id:       "user 01",
						Name:     "user name",
						Nickname: "user nickname",
						Phone:    "01234815125123",
						Email:    "0123512@123987.com",
					},
					ModifiedInfos: []*protobuf.StorageModifiedInfo{
						{
							Cmd:      protobuf.StorageModifiedInfo_DELETE,
							Key:      "name",
							OldValue: "",
							NewValue: "",
						},
						{
							Cmd:      protobuf.StorageModifiedInfo_UPDATE,
							Key:      "basicOptions.HOST",
							OldValue: "1.2.3.4",
							NewValue: "4.3.2.1",
						},
					},
				},
			},
			Event: []*protobuf.StorageEvent{
				{
					Time: &protobuf.DateTime{
						StrDateTime: "2023-01-03 00:00:00.123",
						UtcTime:     1029839845769,
					},
					Id:          "storage id",
					Name:        "storage name",
					EventType:   protobuf.Status_CONNECTED,
					Description: "storage name connected",
				},
				{
					Time: &protobuf.DateTime{
						StrDateTime: "2023-01-04 00:00:00.123",
						UtcTime:     1029839845769,
					},
					Id:          "storage id",
					Name:        "storage name",
					EventType:   protobuf.Status_DISCONNECTED,
					Description: "storage name disconnected",
				},
				{
					Time: &protobuf.DateTime{
						StrDateTime: "2023-01-04 01:00:00.123",
						UtcTime:     1029839845769,
					},
					Id:          "storage id",
					Name:        "storage name",
					EventType:   protobuf.Status_ERROR,
					Description: "storage name : authentication fail",
				},
			},
		},
	}
	return res, nil
}
func (service *StorageService) Search(ctx context.Context, in *protobuf.ReqStorageSearch) (*protobuf.ResStorages, error) {
	res := &protobuf.ResStorages{
		Code: "200",
		Data: &protobuf.ResStorages_Data{
			Storages: []*protobuf.Storage{
				{
					Id:          "storage-id-01",
					Name:        "storage-name-01",
					StorageType: "IRIS",
					Status:      protobuf.Status_CONNECTED,
				},
				{
					Id:          "storage-id-02",
					Name:        "storage-name-02",
					StorageType: "IRIS",
					Status:      protobuf.Status_DISCONNECTED,
				},
				{
					Id:          "storage-id-03",
					Name:        "storage-name-03",
					StorageType: "MySQL",
					Status:      protobuf.Status_ERROR,
				},
				{
					Id:          "storage-id-04",
					Name:        "storage-name-04",
					StorageType: "MariaDB",
					Status:      protobuf.Status_CONNECTED,
				},
				{
					Id:          "storage-id-05",
					Name:        "storage-name-05",
					StorageType: "PostgreSQL",
					Status:      protobuf.Status_CONNECTED,
				},
			},
		},
	}
	return res, nil
}
func (service *StorageService) Status(ctx context.Context, in *protobuf.ReqId) (*protobuf.ResStorage, error) {
	res := &protobuf.ResStorage{
		Code: "200",
		Data: &protobuf.ResStorage_Data{
			Storage: &protobuf.Storage{
				Id:                "storage id 01",
				Name:              "storage name",
				Description:       "storage description",
				SystemMeta:        []*protobuf.Meta{{Key: "meta", Value: "metaValue"}},
				UserMeta:          []*protobuf.Meta{{Key: "user meta", Value: "user meta value"}},
				Tags:              []string{"tag01", "tag02"},
				StorageType:       "IRIS",
				AdaptorId:         "adaptor id",
				BasicOptions:      []*protobuf.InputField{{Key: "key", Value: "value"}},
				AdditionalOptions: []*protobuf.InputField{{Key: "key", Value: "value"}},
				Status:            protobuf.Status_CONNECTED,
				Statistics: &protobuf.StorageStatistics{
					Time:            "2023-01-01 00:00:00.123",
					Id:              "storage id 01",
					Name:            "storage name",
					Access:          10,
					TotalData:       3,
					RegisteredData:  2,
					AvgResponseTime: 1.2,
				},
				DataStatistics: []*protobuf.DataModelStatistics{
					{
						Time:            "2023-10-11 10:10:10.123",
						Id:              "data id 01",
						Name:            "data name 01",
						Access:          10,
						Bookmark:        20,
						Download:        30,
						Rating:          40,
						AvgResponseTime: 1.2,
					},
					{
						Time:            "2023-10-12 10:10:10.123",
						Id:              "data id 02",
						Name:            "data name 02",
						Access:          20,
						Bookmark:        30,
						Download:        40,
						Rating:          50,
						AvgResponseTime: 2.2,
					},
					{
						Time:            "2023-10-13 10:10:10.123",
						Id:              "data id 03",
						Name:            "data name 03",
						Access:          30,
						Bookmark:        40,
						Download:        50,
						Rating:          60,
						AvgResponseTime: 3.2,
					},
					{
						Time:            "2023-10-14 10:10:10.123",
						Id:              "data id 04",
						Name:            "data name 04",
						Access:          40,
						Bookmark:        50,
						Download:        60,
						Rating:          70,
						AvgResponseTime: 4.2,
					},
					{
						Time:            "2023-10-15 10:10:10.123",
						Id:              "data id 05",
						Name:            "data name 05",
						Access:          50,
						Bookmark:        60,
						Download:        70,
						Rating:          80,
						AvgResponseTime: 5.2,
					},
				},
				History: []*protobuf.StorageHistory{
					{
						Time: &protobuf.DateTime{
							StrDateTime: "2023-01-02 00:00:00.123",
							UtcTime:     1029839845769,
						},
						Id:   "storage id 01",
						Name: "storage name 01",
						ModifiedBy: &protobuf.User{
							Id:       "user 01",
							Name:     "user name",
							Nickname: "user nickname",
							Phone:    "01234815125123",
							Email:    "0123512@123987.com",
						},
						ModifiedInfos: []*protobuf.StorageModifiedInfo{
							{
								Cmd:      protobuf.StorageModifiedInfo_CREATE,
								Key:      "name",
								OldValue: "",
								NewValue: "new Name",
							},
							{
								Cmd:      protobuf.StorageModifiedInfo_UPDATE,
								Key:      "description",
								OldValue: "old desc",
								NewValue: "new desc",
							},
						},
					},
					{
						Time: &protobuf.DateTime{
							StrDateTime: "2023-01-03 00:00:00.123",
							UtcTime:     1029839845769,
						},
						Id:   "storage id 01",
						Name: "storage name 01",
						ModifiedBy: &protobuf.User{
							Id:       "user 01",
							Name:     "user name",
							Nickname: "user nickname",
							Phone:    "01234815125123",
							Email:    "0123512@123987.com",
						},
						ModifiedInfos: []*protobuf.StorageModifiedInfo{
							{
								Cmd:      protobuf.StorageModifiedInfo_DELETE,
								Key:      "name",
								OldValue: "",
								NewValue: "",
							},
							{
								Cmd:      protobuf.StorageModifiedInfo_UPDATE,
								Key:      "basicOptions.HOST",
								OldValue: "1.2.3.4",
								NewValue: "4.3.2.1",
							},
						},
					},
				},
				Event: []*protobuf.StorageEvent{
					{
						Time: &protobuf.DateTime{
							StrDateTime: "2023-01-03 00:00:00.123",
							UtcTime:     1029839845769,
						},
						Id:          "storage id",
						Name:        "storage name",
						EventType:   protobuf.Status_CONNECTED,
						Description: "storage name connected",
					},
					{
						Time: &protobuf.DateTime{
							StrDateTime: "2023-01-04 00:00:00.123",
							UtcTime:     1029839845769,
						},
						Id:          "storage id",
						Name:        "storage name",
						EventType:   protobuf.Status_DISCONNECTED,
						Description: "storage name disconnected",
					},
					{
						Time: &protobuf.DateTime{
							StrDateTime: "2023-01-04 01:00:00.123",
							UtcTime:     1029839845769,
						},
						Id:          "storage id",
						Name:        "storage name",
						EventType:   protobuf.Status_ERROR,
						Description: "storage name : authentication fail",
					},
				},
				CreatedBy: &protobuf.User{
					Id:       "creator id 01",
					Name:     "creator user name",
					Nickname: "creator user nickname",
					Phone:    "01234815125123",
					Email:    "0123512@123987.com",
				},
				CreatedAt: &protobuf.DateTime{
					StrDateTime: "2023-01-06 00:00:00.123",
					UtcTime:     1029839845769,
				},
				LastModifiedBy: &protobuf.User{
					Id:       "modified user 01",
					Name:     "modified user name",
					Nickname: "modified user nickname",
					Phone:    "01234815125123",
					Email:    "0123512@123987.com",
				},
				LastModifiedAt: &protobuf.DateTime{
					StrDateTime: "2023-01-07 00:00:00.123",
					UtcTime:     1029839845769,
				},
			},
		},
	}
	return res, nil
}
func (service *StorageService) Default(ctx context.Context, in *protobuf.ReqId) (*protobuf.ResStorage, error) {
	res := &protobuf.ResStorage{
		Code: "200",
		Data: &protobuf.ResStorage_Data{
			Storage: &protobuf.Storage{
				Id:                "storage id 01",
				Name:              "storage name",
				Description:       "storage description",
				SystemMeta:        []*protobuf.Meta{{Key: "meta", Value: "metaValue"}},
				UserMeta:          []*protobuf.Meta{{Key: "user meta", Value: "user meta value"}},
				Tags:              []string{"tag01", "tag02"},
				StorageType:       "IRIS",
				AdaptorId:         "adaptor id",
				BasicOptions:      []*protobuf.InputField{{Key: "key", Value: "value"}},
				AdditionalOptions: []*protobuf.InputField{{Key: "key", Value: "value"}},
				Settings: &protobuf.StorageSetting{
					AutoAddSetting: &protobuf.AutoAddSetting{
						Enable: true,
						Options: []*protobuf.AutoAddSetting_AutoAddSettingOption{
							{
								Regex:      "*",
								DataType:   "STRUCTURED",
								DataFormat: "",
								MinSize:    -1,
								MaxSize:    -1,
								StartDate:  "",
								EndDate:    "",
							},
							{
								Regex:      "*",
								DataType:   "",
								DataFormat: "TABLE",
								MinSize:    -1,
								MaxSize:    -1,
								StartDate:  "2001-01-01",
								EndDate:    "2023-12-31",
							},
						},
					},
					SyncSetting: &protobuf.SyncSetting{
						Enable:   true,
						SyncType: 1,
						Period:   0,
						Week:     0x1f,
						RunTime:  "02:03",
					},
					MonitoringSetting: &protobuf.MonitoringSetting{
						Enable:           false,
						Protocol:         protobuf.MonitoringProtocol_SQL,
						Host:             "localhost",
						Port:             "3308",
						Sql:              "select 1",
						Period:           5,
						Timeout:          30,
						SuccessThreshold: 1,
						FailThreshold:    2,
					},
				},
				Status: protobuf.Status_CONNECTED,
				Statistics: &protobuf.StorageStatistics{
					Time:            "2023-01-01 00:00:00.123",
					Id:              "storage id 01",
					Name:            "storage name",
					Access:          10,
					TotalData:       3,
					RegisteredData:  2,
					AvgResponseTime: 1.2,
				},
				DataStatistics: []*protobuf.DataModelStatistics{
					{
						Time:            "2023-10-11 10:10:10.123",
						Id:              "data id 01",
						Name:            "data name 01",
						Access:          10,
						Bookmark:        20,
						Download:        30,
						Rating:          40,
						AvgResponseTime: 1.2,
					},
					{
						Time:            "2023-10-12 10:10:10.123",
						Id:              "data id 02",
						Name:            "data name 02",
						Access:          20,
						Bookmark:        30,
						Download:        40,
						Rating:          50,
						AvgResponseTime: 2.2,
					},
					{
						Time:            "2023-10-13 10:10:10.123",
						Id:              "data id 03",
						Name:            "data name 03",
						Access:          30,
						Bookmark:        40,
						Download:        50,
						Rating:          60,
						AvgResponseTime: 3.2,
					},
					{
						Time:            "2023-10-14 10:10:10.123",
						Id:              "data id 04",
						Name:            "data name 04",
						Access:          40,
						Bookmark:        50,
						Download:        60,
						Rating:          70,
						AvgResponseTime: 4.2,
					},
					{
						Time:            "2023-10-15 10:10:10.123",
						Id:              "data id 05",
						Name:            "data name 05",
						Access:          50,
						Bookmark:        60,
						Download:        70,
						Rating:          80,
						AvgResponseTime: 5.2,
					},
				},
				History: []*protobuf.StorageHistory{
					{
						Time: &protobuf.DateTime{
							StrDateTime: "2023-01-02 00:00:00.123",
							UtcTime:     1029839845769,
						},
						Id:   "storage id 01",
						Name: "storage name 01",
						ModifiedBy: &protobuf.User{
							Id:       "user 01",
							Name:     "user name",
							Nickname: "user nickname",
							Phone:    "01234815125123",
							Email:    "0123512@123987.com",
						},
						ModifiedInfos: []*protobuf.StorageModifiedInfo{
							{
								Cmd:      protobuf.StorageModifiedInfo_CREATE,
								Key:      "name",
								OldValue: "",
								NewValue: "new Name",
							},
							{
								Cmd:      protobuf.StorageModifiedInfo_UPDATE,
								Key:      "description",
								OldValue: "old desc",
								NewValue: "new desc",
							},
						},
					},
					{
						Time: &protobuf.DateTime{
							StrDateTime: "2023-01-03 00:00:00.123",
							UtcTime:     1029839845769,
						},
						Id:   "storage id 01",
						Name: "storage name 01",
						ModifiedBy: &protobuf.User{
							Id:       "user 01",
							Name:     "user name",
							Nickname: "user nickname",
							Phone:    "01234815125123",
							Email:    "0123512@123987.com",
						},
						ModifiedInfos: []*protobuf.StorageModifiedInfo{
							{
								Cmd:      protobuf.StorageModifiedInfo_DELETE,
								Key:      "name",
								OldValue: "",
								NewValue: "",
							},
							{
								Cmd:      protobuf.StorageModifiedInfo_UPDATE,
								Key:      "basicOptions.HOST",
								OldValue: "1.2.3.4",
								NewValue: "4.3.2.1",
							},
						},
					},
				},
				Event: []*protobuf.StorageEvent{
					{
						Time: &protobuf.DateTime{
							StrDateTime: "2023-01-03 00:00:00.123",
							UtcTime:     1029839845769,
						},
						Id:          "storage id",
						Name:        "storage name",
						EventType:   protobuf.Status_CONNECTED,
						Description: "storage name connected",
					},
					{
						Time: &protobuf.DateTime{
							StrDateTime: "2023-01-04 00:00:00.123",
							UtcTime:     1029839845769,
						},
						Id:          "storage id",
						Name:        "storage name",
						EventType:   protobuf.Status_DISCONNECTED,
						Description: "storage name disconnected",
					},
					{
						Time: &protobuf.DateTime{
							StrDateTime: "2023-01-04 01:00:00.123",
							UtcTime:     1029839845769,
						},
						Id:          "storage id",
						Name:        "storage name",
						EventType:   protobuf.Status_ERROR,
						Description: "storage name : authentication fail",
					},
				},
				CreatedBy: &protobuf.User{
					Id:       "creator id 01",
					Name:     "creator user name",
					Nickname: "creator user nickname",
					Phone:    "01234815125123",
					Email:    "0123512@123987.com",
				},
				CreatedAt: &protobuf.DateTime{
					StrDateTime: "2023-01-06 00:00:00.123",
					UtcTime:     1029839845769,
				},
				LastModifiedBy: &protobuf.User{
					Id:       "modified user 01",
					Name:     "modified user name",
					Nickname: "modified user nickname",
					Phone:    "01234815125123",
					Email:    "0123512@123987.com",
				},
				LastModifiedAt: &protobuf.DateTime{
					StrDateTime: "2023-01-07 00:00:00.123",
					UtcTime:     1029839845769,
				},
			},
		},
	}
	return res, nil
}
func (service *StorageService) Advanced(ctx context.Context, in *protobuf.ReqId) (*protobuf.ResStorage, error) {
	res := &protobuf.ResStorage{
		Code: "200",
		Data: &protobuf.ResStorage_Data{
			Storage: &protobuf.Storage{
				Id:                "storage id 01",
				Name:              "storage name",
				Description:       "storage description",
				SystemMeta:        []*protobuf.Meta{{Key: "meta", Value: "metaValue"}},
				UserMeta:          []*protobuf.Meta{{Key: "user meta", Value: "user meta value"}},
				Tags:              []string{"tag01", "tag02"},
				StorageType:       "IRIS",
				AdaptorId:         "adaptor id",
				BasicOptions:      []*protobuf.InputField{{Key: "key", Value: "value"}},
				AdditionalOptions: []*protobuf.InputField{{Key: "key", Value: "value"}},
				Settings: &protobuf.StorageSetting{
					AutoAddSetting: &protobuf.AutoAddSetting{
						Enable: true,
						Options: []*protobuf.AutoAddSetting_AutoAddSettingOption{
							{
								Regex:      "*",
								DataType:   "STRUCTURED",
								DataFormat: "",
								MinSize:    -1,
								MaxSize:    -1,
								StartDate:  "",
								EndDate:    "",
							},
							{
								Regex:      "*",
								DataType:   "",
								DataFormat: "TABLE",
								MinSize:    -1,
								MaxSize:    -1,
								StartDate:  "2001-01-01",
								EndDate:    "2023-12-31",
							},
						},
					},
					SyncSetting: &protobuf.SyncSetting{
						Enable:   true,
						SyncType: 1,
						Period:   0,
						Week:     0x1f,
						RunTime:  "02:03",
					},
					MonitoringSetting: &protobuf.MonitoringSetting{
						Enable:           false,
						Protocol:         protobuf.MonitoringProtocol_SQL,
						Host:             "localhost",
						Port:             "3308",
						Sql:              "select 1",
						Period:           5,
						Timeout:          30,
						SuccessThreshold: 1,
						FailThreshold:    2,
					},
				},
				Status: protobuf.Status_CONNECTED,
				Statistics: &protobuf.StorageStatistics{
					Time:            "2023-01-01 00:00:00.123",
					Id:              "storage id 01",
					Name:            "storage name",
					Access:          10,
					TotalData:       3,
					RegisteredData:  2,
					AvgResponseTime: 1.2,
				},
				DataStatistics: []*protobuf.DataModelStatistics{
					{
						Time:            "2023-10-11 10:10:10.123",
						Id:              "data id 01",
						Name:            "data name 01",
						Access:          10,
						Bookmark:        20,
						Download:        30,
						Rating:          40,
						AvgResponseTime: 1.2,
					},
					{
						Time:            "2023-10-12 10:10:10.123",
						Id:              "data id 02",
						Name:            "data name 02",
						Access:          20,
						Bookmark:        30,
						Download:        40,
						Rating:          50,
						AvgResponseTime: 2.2,
					},
					{
						Time:            "2023-10-13 10:10:10.123",
						Id:              "data id 03",
						Name:            "data name 03",
						Access:          30,
						Bookmark:        40,
						Download:        50,
						Rating:          60,
						AvgResponseTime: 3.2,
					},
					{
						Time:            "2023-10-14 10:10:10.123",
						Id:              "data id 04",
						Name:            "data name 04",
						Access:          40,
						Bookmark:        50,
						Download:        60,
						Rating:          70,
						AvgResponseTime: 4.2,
					},
					{
						Time:            "2023-10-15 10:10:10.123",
						Id:              "data id 05",
						Name:            "data name 05",
						Access:          50,
						Bookmark:        60,
						Download:        70,
						Rating:          80,
						AvgResponseTime: 5.2,
					},
				},
				History: []*protobuf.StorageHistory{
					{
						Time: &protobuf.DateTime{
							StrDateTime: "2023-01-02 00:00:00.123",
							UtcTime:     1029839845769,
						},
						Id:   "storage id 01",
						Name: "storage name 01",
						ModifiedBy: &protobuf.User{
							Id:       "user 01",
							Name:     "user name",
							Nickname: "user nickname",
							Phone:    "01234815125123",
							Email:    "0123512@123987.com",
						},
						ModifiedInfos: []*protobuf.StorageModifiedInfo{
							{
								Cmd:      protobuf.StorageModifiedInfo_CREATE,
								Key:      "name",
								OldValue: "",
								NewValue: "new Name",
							},
							{
								Cmd:      protobuf.StorageModifiedInfo_UPDATE,
								Key:      "description",
								OldValue: "old desc",
								NewValue: "new desc",
							},
						},
					},
					{
						Time: &protobuf.DateTime{
							StrDateTime: "2023-01-03 00:00:00.123",
							UtcTime:     1029839845769,
						},
						Id:   "storage id 01",
						Name: "storage name 01",
						ModifiedBy: &protobuf.User{
							Id:       "user 01",
							Name:     "user name",
							Nickname: "user nickname",
							Phone:    "01234815125123",
							Email:    "0123512@123987.com",
						},
						ModifiedInfos: []*protobuf.StorageModifiedInfo{
							{
								Cmd:      protobuf.StorageModifiedInfo_DELETE,
								Key:      "name",
								OldValue: "",
								NewValue: "",
							},
							{
								Cmd:      protobuf.StorageModifiedInfo_UPDATE,
								Key:      "basicOptions.HOST",
								OldValue: "1.2.3.4",
								NewValue: "4.3.2.1",
							},
						},
					},
				},
				Event: []*protobuf.StorageEvent{
					{
						Time: &protobuf.DateTime{
							StrDateTime: "2023-01-03 00:00:00.123",
							UtcTime:     1029839845769,
						},
						Id:          "storage id",
						Name:        "storage name",
						EventType:   protobuf.Status_CONNECTED,
						Description: "storage name connected",
					},
					{
						Time: &protobuf.DateTime{
							StrDateTime: "2023-01-04 00:00:00.123",
							UtcTime:     1029839845769,
						},
						Id:          "storage id",
						Name:        "storage name",
						EventType:   protobuf.Status_DISCONNECTED,
						Description: "storage name disconnected",
					},
					{
						Time: &protobuf.DateTime{
							StrDateTime: "2023-01-04 01:00:00.123",
							UtcTime:     1029839845769,
						},
						Id:          "storage id",
						Name:        "storage name",
						EventType:   protobuf.Status_ERROR,
						Description: "storage name : authentication fail",
					},
				},
				CreatedBy: &protobuf.User{
					Id:       "creator id 01",
					Name:     "creator user name",
					Nickname: "creator user nickname",
					Phone:    "01234815125123",
					Email:    "0123512@123987.com",
				},
				CreatedAt: &protobuf.DateTime{
					StrDateTime: "2023-01-06 00:00:00.123",
					UtcTime:     1029839845769,
				},
				LastModifiedBy: &protobuf.User{
					Id:       "modified user 01",
					Name:     "modified user name",
					Nickname: "modified user nickname",
					Phone:    "01234815125123",
					Email:    "0123512@123987.com",
				},
				LastModifiedAt: &protobuf.DateTime{
					StrDateTime: "2023-01-07 00:00:00.123",
					UtcTime:     1029839845769,
				},
			},
		},
	}
	return res, nil
}
func (service *StorageService) Browse(ctx context.Context, in *protobuf.ReqStorageBrowse) (*protobuf.ResStorageBrowse, error) {
	res := &protobuf.ResStorageBrowse{
		Code: "200",
		Data: &protobuf.ResStorageBrowse_Data{
			StorageBrowse: &protobuf.StorageBrowse{
				Id:   "storage-id-01",
				Path: "/data-fabric",
				Data: []*protobuf.StorageBrowseData{
					{
						Name:       "Data-Name-01",
						Type:       1, // 0 - Path, 1 - End
						DataFormat: "TABLE",
						Status:     1,
						Children:   nil,
					},
					{
						Name:       "Data-Path-01",
						Type:       0, // 0 - Path, 1 - End
						DataFormat: "",
						Status:     0,
						Children: []*protobuf.StorageBrowseData{
							{
								Name:       "Data-Name-01-01",
								Type:       1, // 0 - Path, 1 - End
								DataFormat: "TABLE",
								Status:     1,
							},
						},
					},
					{
						Name:       "Data-Name-02",
						Type:       1, // 0 - Path, 1 - End
						DataFormat: "TABLE",
						Status:     1,
						Children:   nil,
					},
					{
						Name:       "Data-Path-02",
						Type:       0, // 0 - Path, 1 - End
						DataFormat: "",
						Status:     0,
						Children: []*protobuf.StorageBrowseData{
							{
								Name:       "Data-File-02-01",
								Type:       1, // 0 - Path, 1 - End
								DataFormat: "",
								Status:     1,
							},
							{
								Name:       "Data-Path-03",
								Type:       0, // 0 - Path, 1 - End
								DataFormat: "",
								Status:     0,
								Children: []*protobuf.StorageBrowseData{
									{
										Name:       "Data-File-03-01",
										Type:       1, // 0 - Path, 1 - End
										DataFormat: "",
										Status:     1,
									},
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
func (service *StorageService) BrowseDefault(ctx context.Context, in *protobuf.ReqStorageBrowse) (*protobuf.ResStorageBrowseDefault, error) {
	res := &protobuf.ResStorageBrowseDefault{
		Code: "200",
		Data: &protobuf.ResStorageBrowseDefault_Data{
			DataDefaultInfo: &protobuf.StorageBrowseDefault{
				StorageId:  "storgae-id-01",
				Path:       "/data-fabric",
				Name:       "DATA-01",
				DataFormat: "TABLE",
				Status:     0,
				DataInfo: &protobuf.StorageBrowseDefault_DataInfo{
					Rows:        10,
					Columns:     5,
					Size:        0,
					Owner:       "data-owner",
					Description: "test data",
					CreatedAt: &protobuf.DateTime{
						StrDateTime: "2022-01-01 01:02:03.123",
						UtcTime:     1029839845769,
					},
					LastModifiedAt: &protobuf.DateTime{
						StrDateTime: "2023-01-01 01:02:03.123",
						UtcTime:     1029839845769,
					},
				},
				ConnectedDataCount: 0,
				ConnectedData:      nil,
				DataStructure: []*protobuf.DataStructure{
					{
						Order:        0,
						Name:         "id",
						ColType:      "number",
						Length:       4,
						DefaultValue: "0",
						Description:  "primary id",
					},
					{
						Order:        1,
						Name:         "name",
						ColType:      "string",
						Length:       0,
						DefaultValue: "",
						Description:  "name",
					},
					{
						Order:        3,
						Name:         "desc",
						ColType:      "string",
						Length:       32,
						DefaultValue: "",
						Description:  "description",
					},
				},
			},
		},
	}
	if in.Path == "/not-connected" {
		return res, nil
	}

	res.Data.DataDefaultInfo.ConnectedDataCount = 2
	res.Data.DataDefaultInfo.ConnectedData = []*protobuf.DataModel{
		{
			Id:          "data-catalog-id-01",
			Name:        "data-catalog-name-01",
			Description: "data-catalog-description-01",
		},
		{
			Id:          "data-catalog-id-02",
			Name:        "data-catalog-name-02",
			Description: "data-catalog-description-02",
		},
		{
			Id:          "data-catalog-id-03",
			Name:        "data-catalog-name-03",
			Description: "data-catalog-description-03",
		},
	}
	return res, nil
}
func (service *StorageService) ConnectTest(ctx context.Context, in *protobuf.ConnInfo) (*protobuf.CommonResponse, error) {
	if len(in.BasicOptions) <= 0 {
		res := &protobuf.CommonResponse{
			Code:   "400",
			ErrMsg: "basic options is empty",
		}
		return res, nil
	}
	res := &protobuf.CommonResponse{
		Code: "200",
	}
	return res, nil
}
func (service *StorageService) AddStorage(ctx context.Context, in *protobuf.Storage) (*protobuf.CommonResponse, error) {
	if len(in.BasicOptions) <= 0 {
		res := &protobuf.CommonResponse{
			Code:   "400",
			ErrMsg: "basic option is empty",
		}
		return res, nil

	}
	if len(in.Name) <= 0 {
		res := &protobuf.CommonResponse{
			Code:   "500",
			ErrMsg: "name is empty",
		}
		return res, nil
	}
	res := &protobuf.CommonResponse{
		Code: "200",
	}
	return res, nil
}
func (service *StorageService) UpdateStorage(ctx context.Context, in *protobuf.Storage) (*protobuf.CommonResponse, error) {
	if len(in.Id) <= 0 {
		res := &protobuf.CommonResponse{
			Code:   "400",
			ErrMsg: "id is empty",
		}
		return res, nil

	}
	if len(in.Name) <= 0 {
		res := &protobuf.CommonResponse{
			Code:   "400",
			ErrMsg: "name is empty",
		}
		return res, nil
	}
	res := &protobuf.CommonResponse{
		Code: "200",
	}
	return res, nil
}
func (service *StorageService) ConnectedData(ctx context.Context, in *protobuf.ReqId) (*protobuf.ResConnectedData, error) {
	if in.Id == "0" {
		res := &protobuf.ResConnectedData{
			Code: "200",
			Data: &protobuf.ResConnectedData_Data{
				ConnectedDataCount:   0,
				CorrelationDataCount: 0,
			},
		}
		return res, nil
	}
	res := &protobuf.ResConnectedData{
		Code: "200",
		Data: &protobuf.ResConnectedData_Data{
			ConnectedDataCount:   1,
			CorrelationDataCount: 1,
		},
	}
	return res, nil
}
func (service *StorageService) DeleteStorage(ctx context.Context, in *protobuf.ReqId) (*protobuf.CommonResponse, error) {
	res := &protobuf.CommonResponse{
		Code: "200",
	}
	return res, nil
}
