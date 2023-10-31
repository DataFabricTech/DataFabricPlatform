package appdata

import (
	"fmt"

	"github.com/sirupsen/logrus"
)

// Configuration 프로그램 설정 정보
type Configuration struct {
	Log       LogConfiguration       `yaml:"log" json:"log"`
	Datastore DatastoreConfiguration `yaml:"datastore" json:"datastore"`
	Server    ServerConfiguration    `yaml:"server" json:"server"`
	// TODO : Need Config? Add User Definition Configuration
	// yaml inline option not supported in viper...
	// Config	  models.Configuration `yaml:"config" json:"config"`
}

// LogConfiguration  로그 설정 정보
type LogConfiguration struct {
	Output string `yaml:"output" json:"output"`
	Level  string `yaml:"level" json:"level"`
	// 파일 출력 시 옵션
	SavePath      string `yaml:"savePath" json:"savePath"`
	SizePerFileMb int32  `yaml:"sizePerFileMb" json:"sizePerFileMb"`
	MaxOfDay      int32  `yaml:"maxOfDay" json:"maxOfDay"`
	MaxAge        int32  `yaml:"maxAge" json:"maxAge"`
	Compress      bool   `yaml:"compress" json:"compress"`
}

// List of supported log output
const (
	LogOutStdout string = "stdout"
	LogOutFile   string = "file"
)

// CheckLogLevel check loglevel and return logrus log level
func CheckLogLevel(lv string) (int, error) {
	switch lv {
	case LvDebug:
		return int(logrus.DebugLevel), nil
	case LvInfo:
		return int(logrus.InfoLevel), nil
	case LvWarn:
		return int(logrus.WarnLevel), nil
	case LvError:
		return int(logrus.ErrorLevel), nil
	case LvSilent:
		return int(logrus.FatalLevel), nil
	default:
		return -1, fmt.Errorf("ERROR. Not Supported Log Level")
	}
}

// List of supported log level
const (
	LvDebug  string = "debug"
	LvInfo   string = "info"
	LvWarn   string = "warn"
	LvError  string = "error"
	LvSilent string = "silent"
)

// DatastoreConfiguration 데이터베이스 설정 정보
type DatastoreConfiguration struct {
	Enabled  bool           `yaml:"enabled" json:"enabled"`
	Database string         `yaml:"database" json:"database"`
	Endpoint EndpointInfo   `yaml:"endPoint" json:"endPoint"`
	ConnPool ConnPool       `yaml:"connPool" json:"connPool"`
	Debug    DatastoreDebug `yaml:"debug" json:"debug"`
}

// List of supported databases
const (
	Mysql    string = "mysql"
	Postgres string = "postgres"
	Sqlite   string = "sqlite3"
)

// EndpointInfo database info
type EndpointInfo struct {
	// uri
	Host string `yaml:"host" json:"host"`
	Port int    `yaml:"port" json:"port"`
	Path string `yaml:"path" json:"path"`
	// Auth
	User string `yaml:"user" json:"user"`
	Pass string `yaml:"pass" json:"pass"`
	// DB
	DBName string `yaml:"dbName" json:"dbName"`
	// Option
	Option string `yaml:"option" json:"option"`
}

// ConnPool connection pool setting
type ConnPool struct {
	MaxIdleConns int `yaml:"maxIdleConns" json:"maxIdleConns"`
	MaxOpenConns int `yaml:"maxOpenConns" json:"maxOpenConns"`
}

// DatastoreDebug datastore debug setting
type DatastoreDebug struct {
	LogLevel      string `yaml:"logLevel" json:"logLevel"`
	SlowThreshold string `yaml:"slowThreshold" json:"slowThreshold"`
}

// ServerConfiguration Server Configuration
type ServerConfiguration struct {
	Debug bool   `yaml:"debug" json:"debug"`
	Host  string `yaml:"host" json:"host"`
	Port  int    `yaml:"port" json:"port"`
}
