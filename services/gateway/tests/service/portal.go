package service

import (
	"context"
	"github.com/datafabric/gateway/protobuf"
	"github.com/golang/protobuf/ptypes/empty"
)

type PortalService struct {
	protobuf.UnimplementedPortalServiceServer
}

func (service *PortalService) Search(ctx context.Context, req *protobuf.ReqSearch) (*protobuf.ResSearch, error) {
	res := &protobuf.ResSearch{
		Code: "200",
		Data: &protobuf.ResSearch_Data{
			SearchResponse: &protobuf.SearchResponse{
				Pageable: &protobuf.Pageable{
					Page: &protobuf.Page{
						Size:       10,
						TotalSize:  1,
						SelectPage: 2,
						TotalPage:  3,
					},
					Sort: []*protobuf.Sort{{
						Order:     0,
						Field:     "name",
						Direction: protobuf.Direction_ASC,
					}},
				},
				Filters: map[string]*protobuf.ListMapStrNumber{
					"dataType": {
						Value: []*protobuf.ListMapStrNumber_MapStrNumber{
							{
								Key:   "STRUCTURED",
								Value: 10,
							},
							{
								Key:   "UNSTRUCTURED",
								Value: 20,
							},
							{
								Key:   "SEMI_STRUCTURED",
								Value: 30,
							},
						},
					},
					"dataFormat": {
						Value: []*protobuf.ListMapStrNumber_MapStrNumber{
							{
								Key:   "TABLE",
								Value: 10,
							},
							{
								Key:   "WORD",
								Value: 20,
							},
							{
								Key:   "HTML",
								Value: 30,
							},
						},
					},
					"StorageType": {
						Value: []*protobuf.ListMapStrNumber_MapStrNumber{
							{
								Key:   "IRIS",
								Value: 10,
							},
							{
								Key:   "HDFS",
								Value: 20,
							},
							{
								Key:   "MinIO",
								Value: 30,
							},
						},
					},
				},
				Contents: &protobuf.SearchContent{
					DataModels: []*protobuf.DataModel{
						{
							Id:          "1",
							Name:        "2",
							Description: "Test Data",
							Status:      "CONNECTED",
							DataType:    "STRUCTURED",
							DataFormat:  "TABLE",
							DataLocation: []*protobuf.DataLocation{
								{
									StorageId:    "1",
									DatabaseName: "database",
									DataPath:     "",
									TableName:    "tableName",
									FileName:     "",
									Scope:        "",
									SheetName:    "",
									CallRange:    "",
									Separator:    "",
									BeginTime:    "",
									EndTime:      "",
								},
							},
							DataRefine: &protobuf.DataRefine{
								Json:  "create data refine json",
								Query: `select * from data`,
							},
							DataStructure: []*protobuf.DataStructure{
								{
									Order:        1,
									Name:         "id",
									ColType:      "number",
									Length:       4,
									DefaultValue: "null",
									Description:  "id",
								},
								{
									Order:        2,
									Name:         "name",
									ColType:      "string",
									Length:       32,
									DefaultValue: "null",
									Description:  "name",
								},
							},
							Category: []*protobuf.Category{
								{Name: "category01"},
								{Name: "category02"},
							},
							SystemMeta: []*protobuf.Meta{
								{
									Key:   "meta",
									Value: "metaValue",
								},
								{
									Key:   "meta02",
									Value: "metaValue02",
								},
							},
							UserMeta: []*protobuf.Meta{
								{
									Key:   "user meta 01",
									Value: "user meta value 01",
								},
								{
									Key:   "user meta 02",
									Value: "user meta value 02",
								},
							},
							Tag: []string{"tag01", "tag02"},
							Permission: &protobuf.Permission{
								Read:  true,
								Write: true,
							},
							DownloadInfo: &protobuf.DownloadInfo{
								Status: 1,
								Url:    "http://localhost:8080/download/wawawawaw",
							},
							RatingAndComments: &protobuf.DataModel_RatingAndComments{
								AvgRating: 7.8,
								RatingAndComment: []*protobuf.RatingAndComment{
									{
										User: &protobuf.User{
											Id:       "id",
											Name:     "name",
											Nickname: "nickName",
											Phone:    "012341234",
											Email:    "01234@1234.123",
										},
										LastModifiedAt: &protobuf.DateTime{
											StrDateTime: "2023-01-01 00:00:00.123",
											UtcTime:     123456789123,
										},
										Rating:  10,
										Comment: "comment good",
									},
								},
							},
							Statistics: &protobuf.DataModelStatistics{
								Time:            "2022-11-11 11:11:11.123",
								Id:              "data id",
								Name:            "data name",
								Access:          1,
								Bookmark:        2,
								Download:        3,
								Rating:          4.5,
								AvgResponseTime: 1.5,
							},
							Creator: &protobuf.User{
								Id:       "create user id",
								Name:     "creator name",
								Nickname: "creator nickname",
								Phone:    "012345679",
								Email:    "012345@012345.com",
							},
							CreatedAt: &protobuf.DateTime{
								StrDateTime: "2022-11-11 11:11:11.123",
								UtcTime:     123456789123,
							},
							LastModifier: &protobuf.User{
								Id:       "modify user id",
								Name:     "modify userName",
								Nickname: "modify user nickname",
								Phone:    "012345123451",
								Email:    "012345@129123.123",
							},
							LastModifiedAt: &protobuf.DateTime{
								StrDateTime: "2022-12-12 12:12:12.123",
								UtcTime:     123456789123,
							},
						},
					},
					Storages: []*protobuf.Storage{
						{
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
				},
			},
		}}
	return res, nil
}

func (service *PortalService) RecentSearches(ctx context.Context, req *empty.Empty) (*protobuf.ResRecentSearches, error) {
	res := &protobuf.ResRecentSearches{
		Code: "200",
		Data: &protobuf.ResRecentSearches_Data{
			RecentSearches: []string{
				"keyword01", "keyword02", "keyword03", "keyword04", "keyword05",
			},
		},
	}
	return res, nil
}
