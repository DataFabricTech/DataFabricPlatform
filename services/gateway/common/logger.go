package common

import (
	"fmt"
	"os"
	"strings"
	"testing"

	"github.com/datafabric/gateway/common/appdata"
	formatter "github.com/mobigen/gologger"
	"github.com/sirupsen/logrus"
)

// Logger struct have embedding logrus.Logger
type Logger struct {
	*logrus.Logger
}

// Logger log variable
var l *Logger

func init() {
	l = &Logger{logrus.New()}
	l.SetOutput(os.Stdout)
	f := &formatter.Formatter{
		TimestampFormat: "2006-01-02 15:04:05.000",
		ShowFields:      true,
	}
	l.SetFormatter(f)
	l.SetReportCaller(true)
}

// Setting log setting
func (l *Logger) Setting(conf *appdata.LogConfiguration) error {
	switch conf.Output {
	case appdata.LogOutStdout:
		l.SetOutput(os.Stdout)
	case appdata.LogOutFile:
		// TODO : Make Output And Set logrus output
		// TODO : lumberjack customize
		// // 파일 출력 시 옵션
		// SavePath      string `yaml:"savePath" json:"savePath"`
		// SizePerFileMb int32  `yaml:"sizePerFileMb" json:"sizePerFileMb"`
		// MaxOfDay      int32  `yaml:"maxOfDay" json:"maxOfDay"`
		// MaxAge        int32  `yaml:"maxAge" json:"maxAge"`
		// Compress      bool   `yaml:"compress" json:"compress"`
	default:
		return fmt.Errorf("ERROR. Not Supported Log Output[ %s ]", conf.Output)
	}
	lv, err := appdata.CheckLogLevel(conf.Level)
	if err != nil {
		return err
	}
	l.SetLogLevel(logrus.Level(lv))
	return nil
}

// GetInstance return logger instance
func (Logger) GetInstance() *Logger {
	return l
}

// SetLogLevel set log level
func (l *Logger) SetLogLevel(lv logrus.Level) {
	switch lv {
	case logrus.ErrorLevel:
		l.SetLevel(lv)
	case logrus.WarnLevel:
		l.SetLevel(lv)
	case logrus.InfoLevel:
		l.SetLevel(lv)
	case logrus.DebugLevel:
		l.SetLevel(lv)
	default:
		l.Errorf("ERROR. Not Supported Log Level[ %d ]", lv)
	}
}

// GetLogLevel get log level
func (l *Logger) GetLogLevel() string {
	text, _ := l.GetLevel().MarshalText()
	return string(text)
}

// Start Print Start Banner
func (l *Logger) Start() {
	l.Errorf("%s", LINE90)
	l.Errorf(" ")
	l.Errorf("                         START. %s:%s-%s",
		strings.ToUpper(appdata.Name), appdata.Version, appdata.BuildHash)
	l.Errorf(" ")
	l.Errorf("%90s", "Copyright(C) 2021 Mobigen Corporation.  ")
	l.Errorf(" ")
	l.Errorf("%s", LINE90)
}

// Shutdown Print Shutdown
func (l *Logger) Shutdown() {
	l.Errorf("%s", LINE90)
	l.Errorf(" ")
	l.Errorf("                        %s Bye Bye.", strings.ToUpper(appdata.Name))
	l.Errorf(" ")
	l.Errorf("%90s", "Copyright(C) 2021 Mobigen Corporation.  ")
	l.Errorf(" ")
	l.Errorf("%s", LINE90)
}

// For test
// testingWriter is an io.Writer that writes through t.Log.
type testingWriter struct {
	tb testing.TB
}

func (tw *testingWriter) Write(b []byte) (int, error) {
	tw.tb.Log(strings.TrimSpace(string(b)))
	return len(b), nil
}

// MakeTestLogger creates a custom format logrus.Logger
func MakeTestLogger(tb testing.TB) *Logger {
	l = &Logger{logrus.New()}
	l.SetOutput(os.Stdout)
	f := &formatter.Formatter{
		TimestampFormat: "2006-01-02 15:04:05.000",
		ShowFields:      true,
	}
	l.SetFormatter(f)
	l.SetReportCaller(false)
	return l
}
