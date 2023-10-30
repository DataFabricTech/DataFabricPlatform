package models

// CommonResponse 공통 응답 메시지 : For Frontend
type CommonResponse struct {
	Code   int         `json:"code"`
	ErrMsg string      `json:"errMsg"`
	Data   interface{} `json:"data"`
}
