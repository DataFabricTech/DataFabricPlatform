package models

import "github.com/datafabric/gateway/protobuf"

type SupportedStorageTypes struct {
	SupportedStorageType []*SupportedStorageType `json:"supportedStorageType,omitempty"`
}

func (st *SupportedStorageTypes) Convert(input []*protobuf.SupportedStorageType) {
	for _, v := range input {
		tmp := SupportedStorageType{}
		tmp.Convert(v)
		st.SupportedStorageType = append(st.SupportedStorageType, &tmp)
	}
}

type SupportedStorageType struct {
	Name       string        `json:"name,omitempty"` // 기본 정보(이름, 아이콘, 기타 등등)
	Icon       []byte        `json:"icon,omitempty"`
	ConnSchema []*InputField `json:"connSchema,omitempty"` // 최소한(필수)으로 필요한 연결 설정 정보 리스트 ( HOST, PORT, DBNAME 등 )
	AuthSchema []*AuthSchema `json:"authSchema,omitempty"` // 지원하는 인증 리스트
}

type AuthSchema struct {
	AuthType        string        `json:"authType"`                  // 인증 종류 ( NONE, USER_PASSWORD 등 )
	AuthInputFields []*InputField `json:"authInputFields,omitempty"` // 인증 종류에 따라 필요한 데이터
}

func (st *SupportedStorageType) Convert(input *protobuf.SupportedStorageType) {
	st.Name = input.Name
	st.Icon = input.Icon
	st.ConvertConnSchema(input.ConnSchema)
	st.ConvertAuthSchema(input.AuthSchema)
}

func (st *SupportedStorageType) ConvertConnSchema(in []*protobuf.InputField) {
	for _, v := range in {
		tmp := InputField{}
		tmp.Key = v.Key
		tmp.Required = v.Required
		tmp.ValueType = v.ValueType
		tmp.Default = v.Default
		tmp.Description = v.Description
		tmp.Value = v.Value
		st.ConnSchema = append(st.ConnSchema, &tmp)
	}
}

func (st *SupportedStorageType) ConvertAuthSchema(in []*protobuf.AuthSchema) {
	for _, v := range in {
		tmp := AuthSchema{}
		tmp.AuthType = v.GetAuthType().String()
		tmp.AuthInputFields = []*InputField{}
		for _, auth := range v.AuthInputFields {
			authSchema := InputField{}
			authSchema.Key = auth.Key
			authSchema.Required = auth.Required
			authSchema.ValueType = auth.ValueType
			authSchema.Default = auth.Default
			authSchema.Description = auth.Description
			authSchema.Value = auth.Value
			tmp.AuthInputFields = append(tmp.AuthInputFields, &authSchema)
		}
		st.AuthSchema = append(st.AuthSchema, &tmp)
	}
}

type Adaptors struct {
	Adaptors []*Adaptor `json:"adaptors,omitempty"`
}

func (a *Adaptors) Convert(input *protobuf.ResAdaptors_Data) {
	for _, v := range input.Adaptors {
		tmp := Adaptor{}
		tmp.Convert(v)
		a.Adaptors = append(a.Adaptors, &tmp)
	}
}

type Adaptor struct {
	Id           string        `json:"id,omitempty"`           // 아이디
	Name         string        `json:"name,omitempty"`         // 이름
	StorageType  string        `json:"storageType,omitempty"`  // 저장소 유형 이름
	Version      string        `json:"version,omitempty"`      // 버전
	Path         string        `json:"path,omitempty"`         // JDBC 드라이버 경로
	Class        string        `json:"class,omitempty"`        // Class 이름
	SupportedURL []string      `json:"supportedURL,omitempty"` // 연결에 사용되는 URL 리스트
	SelectURL    string        `json:"selectURL,omitempty"`    // 사용자가 선택(편집)한 URL
	BasicOptions []*InputField `json:"basicOptions,omitempty"` // 저장소 유형에 따라 요구되는 필수 정보와 + 어댑터(등록자)가 설정한 요구 설정
	// repeated AuthSchema authOptions = 10;
	// AuthSchema selectAuth = 10;
	AdditionalOptions []*InputField `json:"additionalOptions,omitempty"` // 저장소 유형에서 요구하는 입력 정보 + 어댑터에서 요구하는 입력 정보
}

func (a *Adaptor) Convert(input *protobuf.Adaptor) {
	a.Id = input.Id
	a.Name = input.Name
	a.StorageType = input.StorageType
	a.Version = input.Version
	a.Path = input.Path
	a.Class = input.Class
	a.SupportedURL = input.SupportedURL
	a.SelectURL = input.SelectURL
	a.ConvertBasicOptions(input.BasicOptions)
	a.ConvertAdditionalOptions(input.AdditionalOptions)
}

type InputField struct {
	Key         string `json:"key,omitempty"`         // 입력 받을 데이터의 이름
	Required    bool   `json:"required,omitempty"`    // 필수 / 선택 정보
	ValueType   string `json:"valueType,omitempty"`   // 입력 받을 데이터의 타입
	Default     string `json:"default,omitempty"`     // 기본값 or 예시
	Description string `json:"description,omitempty"` // 설명
	Value       string `json:"value,omitempty"`       // 사용자가 입력한 데이터
}

func (a *Adaptor) ConvertBasicOptions(in []*protobuf.InputField) {
	for _, v := range in {
		tmp := InputField{}
		tmp.Key = v.Key
		tmp.Required = v.Required
		tmp.ValueType = v.ValueType
		tmp.Default = v.Default
		tmp.Description = v.Description
		tmp.Value = v.Value
		a.BasicOptions = append(a.BasicOptions, &tmp)
	}
}

func (a *Adaptor) ConvertAdditionalOptions(in []*protobuf.InputField) {
	for _, v := range in {
		tmp := InputField{}
		tmp.Key = v.Key
		tmp.Required = v.Required
		tmp.ValueType = v.ValueType
		tmp.Default = v.Default
		tmp.Description = v.Description
		tmp.Value = v.Value
		a.AdditionalOptions = append(a.AdditionalOptions, &tmp)
	}
}
