package models

import "github.com/datafabric/gateway/protobuf"

type StorageOverview struct {
	StorageTypeCount    *PieChart                       `json:"storageTypeCount,omitempty"`
	StorageStatusCount  *PieChart                       `json:"storageStatusCount,omitempty"`
	StorageStatistics   *BarChart                       `json:"storageStatistics,omitempty"`
	StorageDataCount    *StackBarChart                  `json:"storageDataCount,omitempty"`
	StorageResponseTime []*protobuf.StorageResponseTime `json:"storageResponseTime,omitempty"`
	History             *Grid                           `json:"history,omitempty"`
	Event               *Grid                           `json:"event,omitempty"`
}

func (s *StorageOverview) Convert(in *protobuf.ResStorageOverview) {
	s.StorageTypeCount = new(PieChart)
	s.StorageTypeCount.StorageTypeCountConvert(in.Data.StorageTypeCount)
	s.StorageStatusCount = new(PieChart)
	s.StorageStatusCount.StorageStatusCountConvert(in.Data.StorageStatusCount)
	s.StorageStatistics = new(BarChart)
	s.StorageStatistics.StorageStatisticsConvert(in.Data.StorageStatistics)
	s.StorageDataCount = new(StackBarChart)
	s.StorageDataCount.StorageDataCountConvert(in.Data.StorageDataCount)
	s.StorageResponseTime = in.Data.StorageResponseTime
	s.History = new(Grid)
	s.History.ConvertHistory(in.Data.History)
	s.Event = new(Grid)
	s.Event.ConvertEvent(in.Data.Event)
}

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
	Status            string                   `json:"status,omitempty"`
	Statistics        *PieChart                `json:"statistics,omitempty"`
	DataStatistics    *BarChart                `json:"dataStatistics,omitempty"`
	History           *Grid                    `json:"history,omitempty"`
	Event             *Grid                    `json:"event,omitempty"`
	CreatedBy         *protobuf.User           `json:"createdBy,omitempty"`
	CreatedAt         *protobuf.DateTime       `json:"createdAt,omitempty"`
	LastModifiedBy    *protobuf.User           `json:"lastModifiedBy,omitempty"`
	LastModifiedAt    *protobuf.DateTime       `json:"lastModifiedAt,omitempty"`
}

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
	s.Status = input.Status.String()
	if input.Statistics != nil {
		s.ConvertStatistics(input.Statistics)
	}
	if input.DataStatistics != nil {
		s.ConvertDataStatistics(input.DataStatistics)
	}
	if input.History != nil {
		s.History = new(Grid)
		s.History.ConvertHistory(input.History)
	}
	if input.Event != nil {
		s.Event = new(Grid)
		s.Event.ConvertEvent(input.Event)
	}
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

func (s *Storage) ConvertDataStatistics(input []*protobuf.DataModelStatistics) {
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

type StorageBrowseResponse struct {
	Id   string               `json:"id,omitempty"`
	Path string               `json:"path,omitempty"`
	Data []*StorageBrowseData `json:"data,omitempty"`
}

type StorageBrowseData struct {
	Name       string               `json:"name,omitempty"`
	Type       int                  `json:"type"`
	DataFormat string               `json:"dataFormat,omitempty"`
	Status     int                  `json:"status"`
	Children   []*StorageBrowseData `json:"children,omitempty"`
}

func (sb *StorageBrowseResponse) Convert(in *protobuf.StorageBrowse) {
	sb.Id = in.Id
	sb.Path = in.Path
	sb.Data = []*StorageBrowseData{}
	for _, v := range in.Data {
		tmp := &StorageBrowseData{
			Name:       v.Name,
			Type:       int(v.Type),
			DataFormat: v.DataFormat,
			Status:     int(v.Status),
			Children:   []*StorageBrowseData{},
		}
		ChildConvert(tmp, v.Children)
		sb.Data = append(sb.Data, tmp)
	}
}

func ChildConvert(parent *StorageBrowseData, child []*protobuf.StorageBrowseData) {
	for _, c := range child {
		tmp := &StorageBrowseData{
			Name:       c.Name,
			Type:       int(c.Type),
			DataFormat: c.DataFormat,
			Status:     int(c.Status),
		}
		if c.Children != nil {
			tmp.Children = []*StorageBrowseData{}
			ChildConvert(tmp, c.Children)
		}
		parent.Children = append(parent.Children, tmp)
	}
}
