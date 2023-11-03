package models

import "github.com/datafabric/gateway/protobuf"

type DataCatalog struct {
	Id                string                                  `json:"id,omitempty"`
	Name              string                                  `json:"name,omitempty"`
	Description       string                                  `json:"description,omitempty"`
	Status            string                                  `json:"status,omitempty"`
	DataType          string                                  `json:"dataType,omitempty"`
	DataFormat        string                                  `json:"dataFormat,omitempty"`
	Row               int32                                   `json:"row,omitempty"`
	Size              int32                                   `json:"size,omitempty"`
	DataLocation      []*protobuf.DataLocation                `json:"dataLocation,omitempty"`
	DataRefine        *protobuf.DataRefine                    `json:"dataRefine,omitempty"`
	DataStructure     *Grid                                   `json:"dataStructure,omitempty"`
	Category          []*protobuf.Category                    `json:"category,omitempty"`
	SystemMeta        []*protobuf.Meta                        `json:"systemMeta,omitempty"`
	UserMeta          []*protobuf.Meta                        `json:"userMeta,omitempty"`
	Tag               []string                                `json:"tag,omitempty"`
	Permission        *protobuf.Permission                    `json:"permission,omitempty"`
	DownloadInfo      *protobuf.DownloadInfo                  `json:"downloadInfo,omitempty"`
	RatingAndComments *protobuf.DataCatalog_RatingAndComments `json:"ratingAndComments,omitempty"`
	Statistics        *protobuf.DataCatalogStatistics         `json:"statistics,omitempty"`
	Creator           *protobuf.User                          `json:"creator,omitempty"`
	CreatedAt         *protobuf.DateTime                      `json:"createdAt,omitempty"`
	LastModifier      *protobuf.User                          `json:"lastModifier,omitempty"`
	LastModifiedAt    *protobuf.DateTime                      `json:"lastModifiedAt,omitempty"`
}

func (dc *DataCatalog) Convert(input *protobuf.DataCatalog) {
	dc.Id = input.Id
	dc.Name = input.Name
	dc.Description = input.Description
	dc.DataType = input.DataType
	dc.DataFormat = input.DataFormat
	dc.Row = input.Row
	dc.Size = input.Size
	dc.DataLocation = input.DataLocation
	dc.DataRefine = input.DataRefine
	if input.DataStructure != nil {
		dc.ConvertDataStructure(input.DataStructure)
	}
	dc.Category = input.Category
	dc.SystemMeta = input.SystemMeta
	dc.UserMeta = input.UserMeta
	dc.Tag = input.Tag
	dc.Permission = input.Permission
	dc.DownloadInfo = input.DownloadInfo
	dc.RatingAndComments = input.RatingAndComments
	dc.Statistics = input.Statistics
	dc.Creator = input.Creator
	dc.CreatedAt = input.CreatedAt
	dc.LastModifier = input.LastModifier
	dc.LastModifiedAt = input.LastModifiedAt
}

func (dc *DataCatalog) ConvertDataStructure(input []*protobuf.DataStructure) {
	dc.DataStructure = &Grid{
		ColumnDefine: []*ColumnDefine{
			{
				HeaderName: "순서",
				Field:      "order",
			},
			{
				HeaderName: "컬럼이름",
				Field:      "name",
			},
			{
				HeaderName: "데이터 타입",
				Field:      "columnType",
			},
			{
				HeaderName: "길이",
				Field:      "length",
			},
			{
				HeaderName: "기본값",
				Field:      "default",
			},
			{
				HeaderName: "설명",
				Field:      "description",
			},
		},
		RowData: []map[string]interface{}{},
	}
	for _, v := range input {
		row := map[string]interface{}{}
		row["order"] = v.Order
		row["name"] = v.Name
		row["columnType"] = v.ColType
		row["length"] = v.Length
		row["default"] = v.DefaultValue
		row["description"] = v.Description
		dc.DataStructure.RowData = append(dc.DataStructure.RowData, row)
	}
}
