package models

import "github.com/datafabric/gateway/protobuf"

type Storage struct {
	Id                string                   `json:"id,omitempty"`
	Name              string                   `json:"name,omitempty"`
	Description       string                   `json:"description,omitempty"`
	SystemMeta        []*protobuf.Meta         `json:"systemMeta,omitempty"`
	UserMeta          []*protobuf.Meta         `json:"userMeta,omitempty"`
	Tags              []string                 `json:"tags,omitempty"`
	StorageType       string                   `json:"storageType,omitempty"`
	AdaptorId         string                   `json:"adaptorId,omitempty"`
	BasicOptions      []*protobuf.InputField   `json:"basicOptions,omitempty"`
	AdditionalOptions []*protobuf.InputField   `json:"additionalOptions,omitempty"`
	Settings          *protobuf.StorageSetting `json:"settings,omitempty"`
	Status            protobuf.Status          `json:"status,omitempty"`
	Statistics        *PieChart                `json:"statistics,omitempty"`
	DataStatistics    *BarChart                `json:"dataStatistics,omitempty"`
	History           *Grid                    `json:"history,omitempty"`
	Event             *Grid                    `json:"event,omitempty"`
	CreatedBy         *protobuf.User           `json:"createdBy,omitempty"`
	CreatedAt         *protobuf.DateTime       `json:"createdAt,omitempty"`
	LastModifiedBy    *protobuf.User           `json:"lastModifiedBy,omitempty"`
	LastModifiedAt    *protobuf.DateTime       `json:"lastModifiedAt,omitempty"`
}

// type ResStorage struct {
//
// 	Storage *Storage `json:"storage"`
// }
//
// func ( )

func (s *Storage) Convert(input *protobuf.Storage) {

	s.Id = input.Id
	s.Name = input.Name
	s.Description = input.Description
	s.SystemMeta = input.SystemMeta
	s.UserMeta = input.UserMeta
	s.Tags = input.Tags
	s.StorageType = input.StorageType
	s.AdaptorId = input.AdaptorId
	s.BasicOptions = input.BasicOptions
	s.AdditionalOptions = input.AdditionalOptions
	s.Settings = input.Settings
	s.Status = input.Status
	s.ConvertStatistics(input.Statistics)
	s.ConvertDataStatistics(input.DataStatistics)
	s.ConvertHistory(input.History)
	s.ConvertEvent(input.Event)
	s.CreatedBy = input.CreatedBy
	s.CreatedAt = input.CreatedAt
	s.LastModifiedBy = input.LastModifiedBy
	s.LastModifiedAt = input.LastModifiedAt
}

func (s *Storage) ConvertStatistics(input *protobuf.StorageStatistics) {
	s.Statistics = &PieChart{
		Series: []*PieSeries{
			{
				Data: []*PieChartData{
					{
						Name: "TotalData",
						Y:    float64(input.TotalData),
					},
					{
						Name: "RegisteredData",
						Y:    float64(input.RegisteredData),
					},
					{
						Name: "AccessCount",
						Y:    float64(input.Access),
					},
					{
						Name: "AvgResponseTime",
						Y:    float64(input.AvgResponseTime),
					},
				},
			},
		},
	}
}

func (s *Storage) ConvertDataStatistics(input []*protobuf.DataCatalogStatistics) {
	s.DataStatistics = &BarChart{
		Categories: []string{},
		Series: []*BarSeries{
			{
				Name: "AccessCount",
				Data: []float64{},
			},
		},
	}
	for _, v := range input {
		s.DataStatistics.Categories = append(s.DataStatistics.Categories, v.Name)
		s.DataStatistics.Series[0].Data = append(s.DataStatistics.Series[0].Data, float64(v.Access))
	}
}

func (s *Storage) ConvertHistory(input []*protobuf.StorageHistory) {
	s.History = &Grid{
		ColumnDefine: []*ColumnDefine{
			{
				HeaderName: "시간",
				Field:      "time",
			},
			{
				HeaderName: "사용자",
				Field:      "user",
			},
			{
				HeaderName: "작업종류",
				Field:      "cmd",
			},
			{
				HeaderName: "작업내용",
				Field:      "modifiedInfo",
			},
		},
		RowData: []map[string]interface{}{},
	}
	for _, v := range input {
		row := map[string]interface{}{}
		row["time"] = v.Time.StrDateTime
		row["user"] = v.ModifiedBy.Name
		var strModifyInfo string
		for _, modifiedInfo := range v.GetModifiedInfos() {
			switch modifiedInfo.Cmd {
			case protobuf.StorageModifiedInfo_CREATE:
				strModifyInfo += modifiedInfo.Key + " : " + modifiedInfo.NewValue + "\n"
				break
			case protobuf.StorageModifiedInfo_UPDATE:
				strModifyInfo += modifiedInfo.Key + " : " + modifiedInfo.OldValue + " -> " + modifiedInfo.NewValue + "\n"
				break
			case protobuf.StorageModifiedInfo_DELETE:
				strModifyInfo += modifiedInfo.Key + " : Delete \n"
				break
			}
		}
		row["cmd"] = "수정"
		row["modifiedInfo"] = strModifyInfo
		s.History.RowData = append(s.History.RowData, row)
	}
}

func (s *Storage) ConvertEvent(input []*protobuf.StorageEvent) {
	s.Event = &Grid{
		ColumnDefine: []*ColumnDefine{
			{
				HeaderName: "시간",
				Field:      "time",
			},
			{
				HeaderName: "저장소이름",
				Field:      "name",
			},
			{
				HeaderName: "이벤트",
				Field:      "event",
			},
			{
				HeaderName: "내용",
				Field:      "desc",
			},
		},
		RowData: []map[string]interface{}{},
	}
	for _, v := range input {
		row := map[string]interface{}{}
		row["time"] = v.Time.StrDateTime
		row["name"] = v.Name
		row["event"] = v.GetEventType().String()
		row["desc"] = v.GetDescription()
		s.Event.RowData = append(s.Event.RowData, row)
	}
}
