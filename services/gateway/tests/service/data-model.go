package service

import (
	"context"
	"github.com/datafabric/gateway/protobuf"
)

type DataModelService struct {
	protobuf.UnimplementedDataModelServiceServer
}

func (s *DataModelService) Preview(ctx context.Context, in *protobuf.ReqId) (*protobuf.DataModelPreview, error) {
	if in.Id == "err-id" {
		res := &protobuf.DataModelPreview{
			Code:   "1234",
			ErrMsg: "not found data model",
		}
		return res, nil
	}
	res := &protobuf.DataModelPreview{
		Code: "200",
		Data: &protobuf.DataModelPreview_Data{
			DataPreview: &protobuf.DataModel{
				Id:          "data-id-01",
				Name:        "Test Data",
				Description: "Test Data Description",
				Status:      "CONNECTED",
				DataType:    "STRUCTURED",
				DataFormat:  "TABLE",
				DataLocation: []*protobuf.DataLocation{
					{
						StorageId:    "storageId",
						DatabaseName: "database",
						// DataPath:     "",
						TableName: "tableName",
						// FileName:     "",
						// Scope:        "",
						// SheetName:    "",
						// CallRange:    "",
						// Separator:    "",
						// BeginTime:    "",
						// EndTime:      "",
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
					{
						Order:        3,
						Name:         "desc",
						ColType:      "string",
						Length:       32,
						DefaultValue: "null",
						Description:  "desc",
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
					AvgRating: 6.7,
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
	}
	return res, nil

}
func (s *DataModelService) Default(ctx context.Context, in *protobuf.ReqId) (*protobuf.DataModelDefault, error) {
	if in.Id == "err-id" {
		res := &protobuf.DataModelDefault{
			Code:   "1234",
			ErrMsg: "not found data model",
		}
		return res, nil
	}
	res := &protobuf.DataModelDefault{
		Code: "200",
		Data: &protobuf.DataModelDefault_Data{
			DataModel: &protobuf.DataModel{
				Id:          "data-id-01",
				Name:        "Test Data",
				Description: "Test Data Description",
				Status:      "CONNECTED",
				DataType:    "STRUCTURED",
				DataFormat:  "TABLE",
				DataLocation: []*protobuf.DataLocation{
					{
						StorageId:    "storageId",
						DatabaseName: "database",
						// DataPath:     "",
						TableName: "tableName",
						// FileName:     "",
						// Scope:        "",
						// SheetName:    "",
						// CallRange:    "",
						// Separator:    "",
						// BeginTime:    "",
						// EndTime:      "",
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
					{
						Order:        3,
						Name:         "desc",
						ColType:      "string",
						Length:       32,
						DefaultValue: "null",
						Description:  "desc",
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
					AvgRating: 6.7,
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
	}
	return res, nil
}

func (s *DataModelService) UserMetadata(ctx context.Context, in *protobuf.ReqMetaUpdate) (*protobuf.CommonResponse, error) {
	if in.Id == "0" {
		res := &protobuf.CommonResponse{
			Code:   "200",
			ErrMsg: "Not Found Data model for update user metadata",
		}
		return res, nil
	}
	res := &protobuf.CommonResponse{
		Code: "200",
	}
	return res, nil
}
func (s *DataModelService) Tag(ctx context.Context, in *protobuf.ReqTagUpdate) (*protobuf.CommonResponse, error) {
	if in.Id == "0" {
		res := &protobuf.CommonResponse{
			Code:   "200",
			ErrMsg: "Not Found Data model for tag",
		}
		return res, nil
	}
	res := &protobuf.CommonResponse{
		Code: "200",
	}
	return res, nil
}
func (s *DataModelService) DownloadRequest(ctx context.Context, in *protobuf.ReqId) (*protobuf.CommonResponse, error) {
	if in.Id == "0" {
		res := &protobuf.CommonResponse{
			Code:   "200",
			ErrMsg: "Not Found Data model for download request",
		}
		return res, nil
	}
	res := &protobuf.CommonResponse{
		Code: "200",
	}
	return res, nil
}
func (s *DataModelService) AddComment(ctx context.Context, in *protobuf.ReqRatingAndComment) (*protobuf.CommonResponse, error) {
	if in.Id == "0" {
		res := &protobuf.CommonResponse{
			Code:   "200",
			ErrMsg: "Not Found Data model for add comment",
		}
		return res, nil
	}
	res := &protobuf.CommonResponse{
		Code: "200",
	}
	return res, nil
}
func (s *DataModelService) UpdateComment(ctx context.Context, in *protobuf.ReqRatingAndComment) (*protobuf.CommonResponse, error) {
	if in.Id == "0" {
		res := &protobuf.CommonResponse{
			Code:   "200",
			ErrMsg: "Not Found Data model for Update Comment",
		}
		return res, nil
	}
	res := &protobuf.CommonResponse{
		Code: "200",
	}
	return res, nil
}
func (s *DataModelService) DelComment(ctx context.Context, in *protobuf.ReqId) (*protobuf.CommonResponse, error) {
	if in.Id == "0" {
		res := &protobuf.CommonResponse{
			Code:   "200",
			ErrMsg: "Not Found Data model for Delete Comment",
		}
		return res, nil
	}
	res := &protobuf.CommonResponse{
		Code: "200",
	}
	return res, nil
}

func (s *DataModelService) AllDataSummary(ctx context.Context, in *protobuf.DataModelSearch) (*protobuf.ResDataModels, error) {
	res := &protobuf.ResDataModels{
		Code: "200",
		Data: &protobuf.ResDataModels_Data{
			Pageable: &protobuf.Pageable{
				Page: &protobuf.Page{
					Size:       in.Pageable.Page.Size,
					TotalSize:  1000,
					SelectPage: in.Pageable.Page.SelectPage,
					TotalPage:  10,
				},
				Sort: []*protobuf.Sort{
					{
						Order:     1,
						Field:     "name",
						Direction: protobuf.Direction_ASC,
					},
					{
						Order:     2,
						Field:     "createdAt",
						Direction: protobuf.Direction_DESC,
					},
				},
			},
			DataModels: []*protobuf.DataModel{
				{
					Id:          "data-id-01",
					Name:        "Test Data",
					Description: "Test Data Description",
					Status:      "CONNECTED",
					DataType:    "STRUCTURED",
					DataFormat:  "TABLE",
					DataLocation: []*protobuf.DataLocation{
						{
							StorageId:    "storageId",
							DatabaseName: "database",
							// DataPath:     "",
							TableName: "tableName",
							// FileName:     "",
							// Scope:        "",
							// SheetName:    "",
							// CallRange:    "",
							// Separator:    "",
							// BeginTime:    "",
							// EndTime:      "",
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
						{
							Order:        3,
							Name:         "desc",
							ColType:      "string",
							Length:       32,
							DefaultValue: "null",
							Description:  "desc",
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
						AvgRating: 4.5,
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
				{
					Id:          "data-id-02",
					Name:        "Test Data 2",
					Description: "Test Data Description 2",
					Status:      "DISCONNECTED",
					DataType:    "STRUCTURED",
					DataFormat:  "TABLE",
					DataLocation: []*protobuf.DataLocation{
						{
							StorageId:    "storageId 02",
							DatabaseName: "database-test",
							// DataPath:     "",
							TableName: "tableName-test",
							// FileName:     "",
							// Scope:        "",
							// SheetName:    "",
							// CallRange:    "",
							// Separator:    "",
							// BeginTime:    "",
							// EndTime:      "",
						},
					},
					DataRefine: &protobuf.DataRefine{
						Json:  "create data refine json",
						Query: `select * from tableName-test where id = 1`,
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
						Status: protobuf.DownloadInfo_READY,
					},
					RatingAndComments: &protobuf.DataModel_RatingAndComments{
						AvgRating: 8.9,
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
		},
	}
	return res, nil
}
func (s *DataModelService) AllData(ctx context.Context, in *protobuf.DataModelSearch) (*protobuf.ResDataModels, error) {
	res := &protobuf.ResDataModels{
		Code: "200",
		Data: &protobuf.ResDataModels_Data{
			Pageable: &protobuf.Pageable{
				Page: &protobuf.Page{
					Size:       in.Pageable.Page.Size,
					TotalSize:  1000,
					SelectPage: in.Pageable.Page.SelectPage,
					TotalPage:  10,
				},
				Sort: []*protobuf.Sort{
					{
						Order:     1,
						Field:     "name",
						Direction: protobuf.Direction_ASC,
					},
					{
						Order:     2,
						Field:     "createdAt",
						Direction: protobuf.Direction_DESC,
					},
				},
			},
			DataModels: []*protobuf.DataModel{
				{
					Id:          "data-id-01",
					Name:        "Test Data",
					Description: "Test Data Description",
					Status:      "CONNECTED",
					DataType:    "STRUCTURED",
					DataFormat:  "TABLE",
					DataLocation: []*protobuf.DataLocation{
						{
							StorageId:    "storageId",
							DatabaseName: "database",
							// DataPath:     "",
							TableName: "tableName",
							// FileName:     "",
							// Scope:        "",
							// SheetName:    "",
							// CallRange:    "",
							// Separator:    "",
							// BeginTime:    "",
							// EndTime:      "",
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
						{
							Order:        3,
							Name:         "desc",
							ColType:      "string",
							Length:       32,
							DefaultValue: "null",
							Description:  "desc",
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
						AvgRating: 4.5,
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
				{
					Id:          "data-id-02",
					Name:        "Test Data 2",
					Description: "Test Data Description 2",
					Status:      "DISCONNECTED",
					DataType:    "STRUCTURED",
					DataFormat:  "TABLE",
					DataLocation: []*protobuf.DataLocation{
						{
							StorageId:    "storageId 02",
							DatabaseName: "database-test",
							// DataPath:     "",
							TableName: "tableName-test",
							// FileName:     "",
							// Scope:        "",
							// SheetName:    "",
							// CallRange:    "",
							// Separator:    "",
							// BeginTime:    "",
							// EndTime:      "",
						},
					},
					DataRefine: &protobuf.DataRefine{
						Json:  "create data refine json",
						Query: `select * from tableName-test where id = 1`,
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
						Status: protobuf.DownloadInfo_READY,
					},
					RatingAndComments: &protobuf.DataModel_RatingAndComments{
						AvgRating: 8.9,
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
		},
	}
	return res, nil
}
