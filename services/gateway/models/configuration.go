package models

// Configuration 프로그램 설정 정보
type Configuration struct {
	Services map[string]Service `yaml:"services" json:"services,omitempty"`
}

// Service 서비스 연결 정보
type Service struct {
	Host string `yaml:"host" json:"host"`
	Port int    `yaml:"port" json:"port"`
}
