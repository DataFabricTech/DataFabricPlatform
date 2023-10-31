package models

import "github.com/datafabric/gateway/protobuf"

// Grid 표 데이터 화면 출력용 구조체
type Grid struct {
	ColumnDefine []*ColumnDefine          `json:"colDefs"`
	RowData      []map[string]interface{} `json:"rowData"`
}

func (g *Grid) ConvertHistory(input []*protobuf.StorageHistory) {
	g.ColumnDefine = []*ColumnDefine{
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
	}
	g.RowData = []map[string]interface{}{}
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
		g.RowData = append(g.RowData, row)
	}
}

func (g *Grid) ConvertEvent(input []*protobuf.StorageEvent) {
	g.ColumnDefine = []*ColumnDefine{
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
	}
	g.RowData = []map[string]interface{}{}
	for _, v := range input {
		row := map[string]interface{}{}
		row["time"] = v.Time.StrDateTime
		row["name"] = v.Name
		row["event"] = v.GetEventType().String()
		row["desc"] = v.GetDescription()
		g.RowData = append(g.RowData, row)
	}
}

// ColumnDefine 표 데이터 컬럼 정의
type ColumnDefine struct {
	HeaderName string `json:"headerName"`
	Field      string `json:"field"`
}

// PieChart 파이 차트 화면 출력용 구조체
type PieChart struct {
	Series []*PieSeries `json:"series"`
}

// PieSeries 파이 차트 데이터 정의
type PieSeries struct {
	Data []*PieChartData `json:"data"`
}

// PieChartData 파이 차트 데이터 정의
type PieChartData struct {
	Name string  `json:"name"`
	Y    float64 `json:"y"`
}

func (c *PieChart) StorageTypeCountConvert(in []*protobuf.StorageTypeCount) {
	c.Series = []*PieSeries{
		{
			Data: []*PieChartData{},
		},
	}
	for _, v := range in {
		tmp := &PieChartData{
			Name: v.GetStorageType(),
			Y:    float64(v.GetCount()),
		}
		c.Series[0].Data = append(c.Series[0].Data, tmp)
	}
}

func (c *PieChart) StorageStatusCountConvert(in []*protobuf.StorageStatusCount) {
	c.Series = []*PieSeries{
		{
			Data: []*PieChartData{},
		},
	}
	for _, v := range in {
		var name string
		switch v.GetStatus() {
		case 0:
			name = "CONNECTED"
			break
		case 1:
			name = "DISCONNECTED"
			break
		case 2:
			name = "ERROR"
			break
		default:
			name = "UNKNOWN"
			break
		}
		tmp := &PieChartData{
			Name: name,
			Y:    float64(v.GetCount()),
		}
		c.Series[0].Data = append(c.Series[0].Data, tmp)
	}
}

// BarChart 막대 차트 화면 출력용 구조체
type BarChart struct {
	Categories []string     `json:"categories"`
	Series     []*BarSeries `json:"series"`
}

// BarSeries 파이 차트 데이터 정의
type BarSeries struct {
	Name string    `json:"name"`
	Data []float64 `json:"data"`
}

// StackBarChart 스택 막대 차트 화면 출력용 구조체
type StackBarChart BarChart

func (c *BarChart) StorageStatisticsConvert(in []*protobuf.StorageStatistics) {
	c.Categories = []string{}
	c.Series = []*BarSeries{
		{
			Name: "AccessCount",
		},
	}
	for _, v := range in {
		c.Categories = append(c.Categories, v.GetName())
		c.Series[0].Data = append(c.Series[0].Data, float64(v.GetAccess()))
	}
}

func (c *StackBarChart) StorageDataCountConvert(in []*protobuf.StorageDataCount) {
	c.Categories = []string{}
	c.Series = []*BarSeries{
		{
			Name: "Total",
		},
		{
			Name: "Registered",
		},
	}
	for _, v := range in {
		c.Categories = append(c.Categories, v.GetName())
		c.Series[0].Data = append(c.Series[0].Data, float64(v.GetTotal()))
		c.Series[1].Data = append(c.Series[1].Data, float64(v.GetRegistered()))
	}
}
