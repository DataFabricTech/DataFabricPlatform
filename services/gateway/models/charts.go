package models

// Grid 표 데이터 화면 출력용 구조체
type Grid struct {
	ColumnDefine []*ColumnDefine          `json:"colDefs"`
	RowData      []map[string]interface{} `json:"rowData"`
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
