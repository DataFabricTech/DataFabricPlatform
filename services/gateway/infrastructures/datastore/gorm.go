package datastore

import (
	"fmt"
	"regexp"
	"strconv"
	"strings"
	"time"

	"gorm.io/driver/mysql"
	"gorm.io/driver/postgres"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	"github.com/datafabric/gateway/common/appdata"
	"github.com/sirupsen/logrus"
)

// DataStore .. struct
type DataStore struct {
	// Orm gorm
	Orm *gorm.DB
	// LogWriter for gorm logger interface
	LogWriter *GormLogWriter
	// HomePath home path
	HomePath string
}

// New create datastore
func (DataStore) New(homePath string, log *logrus.Logger) (ds *DataStore, err error) {
	ds = new(DataStore)
	lw, err := GormLogWriter{}.New(log)
	if err != nil {
		return nil, err
	}
	ds.LogWriter = lw
	ds.HomePath = homePath
	return ds, nil
}

// Connect Connect Database
func (ds *DataStore) Connect(conf *appdata.DatastoreConfiguration) error {
	// Get Config
	ormConf, err := ds.GetGormConfig(conf)
	if err != nil {
		return err
	}
	// Make DSN and Get Dialector
	var dsn string
	var dialector gorm.Dialector
	switch conf.Database {
	case appdata.Mysql:
		// refer : https://github.com/go-sql-driver/mysql#dsn-data-source-name
		// [username[:password]@][protocol[(address)]]/dbname[?param1=value1&...&paramN=valueN]
		// ex : "user:pass@tcp(127.0.0.1:3306)/dbname?charset=utf8mb4&parseTime=True&loc=Local"
		dsn = fmt.Sprintf("%s:%s@tcp(%s:%d)/%s?%s",
			conf.Endpoint.User, conf.Endpoint.Pass,
			conf.Endpoint.Host, conf.Endpoint.Port,
			conf.Endpoint.DBName, conf.Endpoint.Option)
		dialector = mysql.Open(dsn)
	case appdata.Postgres:
		// dsn := "host=localhost user=gorm password=gorm dbname=gorm port=9920 sslmode=disable TimeZone=Asia/Shanghai"
		dsn = fmt.Sprintf("host=%s user=%s password=%s dbname=%s port=%d %s",
			conf.Endpoint.Host, conf.Endpoint.User,
			conf.Endpoint.Pass, conf.Endpoint.DBName,
			conf.Endpoint.Port, conf.Endpoint.Option)
		dialector = postgres.Open(dsn)
	case appdata.Sqlite:
		// refer https://github.com/mattn/go-sqlite3#dsn-examples
		// ex : file:test.db?cache=shared&mode=memory
		// dsn := "/iris-cloud/db/cloud.db
		if len(conf.Endpoint.Option) > 0 {
			dsn = fmt.Sprintf("file://%s/%s?%s", ds.HomePath, conf.Endpoint.Path, conf.Endpoint.Option)
		} else {
			dsn = fmt.Sprintf("file://%s/%s", ds.HomePath, conf.Endpoint.Path)
		}
		dialector = sqlite.Open(dsn)
	default:
		return fmt.Errorf("ERROR. Not Supported Database[ %s ]", conf.Database)
	}

	db, err := gorm.Open(dialector, ormConf)
	if err != nil {
		ds.LogWriter.Log.Errorf("[ ERROR ] [ %s ]", err.Error())
		return err
	}

	// Connection Pool
	sqlDB, err := db.DB()
	switch conf.Database {
	case appdata.Sqlite:
		// Serialize all access to the database. Sqlite3 doesn't allow multiple writers.
		sqlDB.SetMaxOpenConns(1)
	default:
		// SetMaxIdleConns sets the maximum number of connections in the idle connection pool.
		sqlDB.SetMaxIdleConns(conf.ConnPool.MaxIdleConns)
		// SetMaxOpenConns sets the maximum number of open connections to the database.
		sqlDB.SetMaxOpenConns(conf.ConnPool.MaxOpenConns)
		// SetConnMaxLifetime sets the maximum amount of time a connection may be reused.
		sqlDB.SetConnMaxLifetime(time.Hour)
	}
	ds.Orm = db
	return nil
}

// GetGormConfig Get Gorm Config
func (ds *DataStore) GetGormConfig(conf *appdata.DatastoreConfiguration) (*gorm.Config, error) {
	ormConf := new(gorm.Config)
	// For Logger
	logger, err := ds.CreateLogger(conf)
	if err != nil {
		return nil, err
	}
	ormConf.Logger = logger

	return ormConf, nil
}

// CreateLogger create gorm logger
func (ds *DataStore) CreateLogger(conf *appdata.DatastoreConfiguration) (
	logger.Interface, error) {
	var level logger.LogLevel
	switch conf.Debug.LogLevel {
	case "silent":
		level = logger.Silent
	case "error":
		level = logger.Error
	case "warn":
		level = logger.Warn
	case "info":
		level = logger.Info
	default:
		return nil, fmt.Errorf("ERROR. Not Supported Log Level[ %s ]. "+
			"Supported List[ silent, error, warn, info ]", conf.Debug.LogLevel)
	}
	if level == logger.Silent {
		return nil, nil
	}

	if ds.LogWriter == nil {
		return nil, fmt.Errorf("ERROR. Gorm Debug Mode Need To Log Writer. ")
	}

	var slowThreshold time.Duration
	re := regexp.MustCompile("[0-9]+")
	num := re.FindAllString(conf.Debug.SlowThreshold, -1)
	if len(num) != 1 {
		return nil, fmt.Errorf("ERROR. Not Supported SlowThreshold[ %s ]", conf.Debug.SlowThreshold)
	}
	iNum, _ := strconv.Atoi(num[0])
	if strings.Index(conf.Debug.SlowThreshold, "min") > 0 {
		slowThreshold = time.Duration(iNum) * time.Minute
	} else if strings.Index(conf.Debug.SlowThreshold, "sec") > 0 {
		slowThreshold = time.Duration(iNum) * time.Second
	} else if strings.Index(conf.Debug.SlowThreshold, "ms") > 0 {
		slowThreshold = time.Duration(iNum) * time.Millisecond
	} else {
		return nil, fmt.Errorf("ERROR. Not Supported slow threshold")
	}
	ormLogger := logger.New(ds.LogWriter, logger.Config{
		SlowThreshold:             slowThreshold,
		Colorful:                  false,
		IgnoreRecordNotFoundError: true,
		LogLevel:                  level,
	})
	return ormLogger, nil
}

// Migrate database migration
func (ds *DataStore) Migrate() error {
	// Migrate the schema
	// ds.Orm.AutoMigrate(&models.Sample{})

	// ds.Orm.Config.DisableForeignKeyConstraintWhenMigrating = true
	// ds.Orm.Config.DisableForeignKeyConstraintWhenMigrating = false
	return nil
}

// RowLevelMigrate 로우 레벨 데이터베이스 마이그레이션
func (ds *DataStore) RowLevelMigrate() error {
	//
	return nil
}

// ChangeSetting change datastore setting by config
func (ds *DataStore) ChangeSetting(conf *appdata.DatastoreConfiguration) error {
	// for logger
	if ds.Orm.Logger == nil {
		logger, err := ds.CreateLogger(conf)
		if err != nil {
			return err
		}
		ds.Orm.Logger = logger
	} else {
		var level logger.LogLevel
		switch conf.Debug.LogLevel {
		case "silent":
			level = logger.Silent
		case "error":
			level = logger.Error
		case "warn":
			level = logger.Warn
		case "info":
			level = logger.Info
		default:
			return fmt.Errorf("ERROR. Not Supported Log Level[ %s ]. "+
				"Supported List[ silent, error, warn, info ]", conf.Debug.LogLevel)
		}
		ds.Orm.Config.Logger.LogMode(level)
	}
	return nil
}

// Shutdown sqldb close
func (ds *DataStore) Shutdown() error {
	sqlDB, err := ds.Orm.DB()
	if err != nil {
		return err
	}
	if err = sqlDB.Close(); err != nil {
		return err
	}
	return nil
}

// GormLogWriter struct
type GormLogWriter struct {
	Log *logrus.Logger
}

// New create gorm log writer
func (GormLogWriter) New(log *logrus.Logger) (writer *GormLogWriter, err error) {
	if log == nil {
		return nil, fmt.Errorf("[ GORM ] ERROR. Not Set logrus.Logger")
	}
	writer = new(GormLogWriter)
	writer.Log = log
	return writer, nil
}

// Printf - GORM Log Formatter
func (g *GormLogWriter) Printf(format string, v ...interface{}) {
	// User or Gorm Log
	if strings.Index(format, `\n[info]`) > 0 ||
		strings.Index(format, `\n[warn]`) > 0 ||
		strings.Index(format, `\n[error]`) >= 0 {
		// 사용자 메시지( gorm.logger.Info, gorm.logger.Warn 등)
		// Parameter 정보
		// format : %s\n[{level}] message
		// v[0] : file:line

		// 위와 같이 메시지가 구성되어 있어
		// 굳이 복잡한 Format 변경을 진행하기 보다
		// gorm.Logger를 이용한 로그 출력은 자제 부탁
		return
	}
	// sql query Log
	if len(v) == 4 {
		switch v[2].(type) {
		case string:
			g.Log.Debugf("[ GORM ] Time[ %.3f ] Row[ %s ] Query[ %s ]", v[1], v[2], v[3])
		case int, int64, int32:
			g.Log.Debugf("[ GORM ] Time[ %.3f ] Row[ %d ] Query[ %s ]", v[1], v[2], v[3])
		}
	} else {
		g.Log.Debugf("%+v", v)
	}
}
