package main

import (
	"context"
	"fmt"
	"os"
	"os/signal"
	"path/filepath"
	"syscall"
	"time"

	"github.com/datafabric/gateway/common"
	"github.com/datafabric/gateway/common/appdata"
	"github.com/datafabric/gateway/infrastructures/datastore"
	"github.com/datafabric/gateway/infrastructures/router"
	"github.com/datafabric/gateway/injectors"

	"github.com/sirupsen/logrus"
)

// Context context of main
type Context struct {
	Env       *appdata.Environment
	Conf      *appdata.Configuration
	Log       *common.Logger
	CM        *common.ConfigManager
	Datastore *datastore.DataStore
	Router    *router.Router
}

// InitLog Initialize logger
func (c *Context) InitLog() {
	log := common.Logger{}.GetInstance()
	log.SetLogLevel(logrus.DebugLevel)
	c.Log = log
}

// ReadEnv Read value of the environment
func (c *Context) ReadEnv() error {
	c.Env = new(appdata.Environment)
	// Get Home
	homePath := os.Getenv("HOME")
	if len(homePath) > 0 {
		c.Log.Errorf("HOME : %s", homePath)
		c.Env.Home = homePath
	} else {
		dir, err := filepath.Abs(filepath.Dir(os.Args[0]))
		if err != nil {
			return err
		}
		c.Env.Home = dir
	}
	// Get Profile
	profile := os.Getenv("PROFILE")
	if len(profile) > 0 {
		c.Log.Errorf("PROFILE : %s", profile)
		c.Env.Profile = profile
	} else {
		c.Env.Profile = "prod"
	}
	// Get Log Level
	logLevel := os.Getenv("LOG_LEVEL")
	if len(logLevel) > 0 {
		c.Log.Errorf("LOG_LEVEL : %s", logLevel)
		_, err := appdata.CheckLogLevel(logLevel)
		if err != nil {
			return err
		}
		c.Env.LogLevel = logLevel
	} else {
		c.Env.LogLevel = "-"
	}
	// // Get LANG
	// lang := os.Getenv("LANG")
	// if len(lang) > 0 {
	// 	c.Log.Errorf("LANG : %s", lang)
	// 	c.Env.Lang = lang
	// } else {
	// 	c.Env.Lang = "ko"
	// }

	c.Log.Errorf("[ Env ] Read ...................................................................... [ OK ]")
	return nil
}

// ReadConfig Read Configuration File By Viper
func (c *Context) ReadConfig() error {
	c.CM = common.ConfigManager{}.New(c.Log.Logger)
	// Write Config File Info
	configPath := c.Env.Home + "/configs"
	configName := c.Env.Profile
	configType := "yaml"
	// Config file struct
	conf := new(appdata.Configuration)
	// Read
	if err := c.CM.ReadConfig(configPath, configName, configType, conf); err != nil {
		return err
	}
	// Save
	c.Conf = conf

	// Set Watcher
	c.CM.SetOnChanged(configPath, configName, configType,
		func(conf interface{}) {
			c.Log.Info("Next Level")
		}, conf)
	c.Log.Errorf("[ Configuration ] Read ............................................................ [ OK ]")
	return nil
}

// SetLogger set log level, log output. and etc
func (c *Context) SetLogger() error {
	if c.Env.LogLevel != "-" {
		c.Conf.Log.Level = c.Env.LogLevel
	}
	if err := c.Log.Setting(&c.Conf.Log); err != nil {
		return err
	}
	c.Log.Start()
	return nil
}

// InitDatastore Initialize datastore
func (c *Context) InitDatastore() error {
	// Create datastore
	ds, err := datastore.DataStore{}.New(c.Env.Home, c.Log.Logger)
	if err != nil {
		return err
	}
	// Connect
	if err := ds.Connect(&c.Conf.Datastore); err != nil {
		return err
	}

	// Migrate
	if err := ds.Migrate(); err != nil {
		return err
	}

	c.Datastore = ds
	c.Log.Errorf("[ DataStore ] Initialze ........................................................... [ OK ]")
	return nil
}

// InitRouter Initialize router
func (c *Context) InitRouter() error {
	// init echo framework
	r, err := router.Init(c.Log.Logger, c.Conf.Server.Debug)
	if err != nil {
		return err
	}
	c.Router = r
	c.Log.Errorf("[ Router ] Initialze .............................................................. [ OK ]")
	return nil
}

// Initialize env/config load and sub moduel init
func Initialize() (*Context, error) {
	c := new(Context)
	c.Conf = new(appdata.Configuration)

	// 환경 변수, 컨피그를 읽어 들이는 과정에서 로그 출력을 위해
	// 아주 기초적인 부분만을 초기화 한다.
	c.InitLog()

	// Env
	if err := c.ReadEnv(); err != nil {
		return nil, err
	}

	// Read Config
	if err := c.ReadConfig(); err != nil {
		return nil, err
	}

	// Setting Log(from env and conf)
	if err := c.SetLogger(); err != nil {
		return nil, err
	}

	// Datastore
	if c.Conf.Datastore.Enabled {
		if err := c.InitDatastore(); err != nil {
			return nil, err
		}
	}

	// Echo Framework Init
	if err := c.InitRouter(); err != nil {
		return nil, err
	}

	// Write Here : For Other Module Init

	c.Log.Errorf("[ ALL ] Initialze ................................................................. [ OK ]")
	return c, nil
}

// InitDependencyInjection sub model Dependency injection and path regi to server
func (c *Context) InitDependencyInjection() error {
	injector := injectors.NewInjector(c.Router, c.Datastore, c.Log, &c.Conf.AppConfig)
	if err := injector.Init(); err != nil {
		return err
	}
	return nil
}

// StartSubModules Start SubModule And Waiting Signal / Main Loop
func (c *Context) StartSubModules() {
	// Signal
	signalChannel := make(chan os.Signal, 1)
	signal.Notify(signalChannel, os.Interrupt, syscall.SIGKILL, syscall.SIGHUP, syscall.SIGTERM)
	c.Log.Errorf("[ Signal ] Listener Start ......................................................... [ OK ]")

	// Echo Framework
	echoServerErr := make(chan error)
	listenAddr := fmt.Sprintf("%s:%d", c.Conf.Server.Host, c.Conf.Server.Port)
	go func() {
		if err := c.Router.Run(listenAddr); err != nil {
			echoServerErr <- err
		}
	}()
	c.Log.Errorf("[ Router ] Listener Start ......................................................... [ OK ]")

	// Write Here : For Other Sub Modules

	for {
		select {
		case err := <-echoServerErr:
			c.Log.Errorf("[ SERVER ] ERROR[ %s ]", err.Error())
			c.StopSubModules()
			return
		case sig := <-signalChannel:
			c.Log.Errorf("[ SIGNAL ] Receive [ %s ]", sig.String())
			c.StopSubModules()
			return
		case <-time.After(time.Second * 5):
			// 메인 Goroutine에서 주기적으로 무언가를 동작하게 하고 싶다면? 다음을 이용
			// 예 : 성능 시험과 같이 로그가 아닌 통계적인 부분만으로 상태를 체크해야 한다면?
			c.Log.Errorf("I'm Alive...")
		}
	}
}

// StopSubModules Stop Submodules
func (c *Context) StopSubModules() {
	if c.Conf.Datastore.Enabled {
		if err := c.Datastore.Shutdown(); err != nil {
			c.Log.Errorf("[ DataStore ] Shutdown .......................................................... [ Fail ]")
			c.Log.Errorf("[ ERROR ] %s", err.Error())
		} else {
			c.Log.Errorf("[ DataStore ] Shutdown ............................................................ [ OK ]")
		}
	}

	ctx, cancel := context.WithTimeout(context.Background(), time.Duration(15*time.Second))
	defer cancel()
	_ = c.Router.Shutdown(ctx)
	c.Log.Errorf("[ Router ] Shutdown ............................................................... [ OK ]")

	// Write Here : For 사용하는 서브 모듈(Goroutine)들이 안전하게 종료 될 수 있도록 종료 코드를 추가한다.
}

// @title Data Fabric Gateway (Rest To gRPC)
// @version 1.0.0
// @description This is a golang gateway

// @contact.name API Support
// @contact.url http://datafabric.mobigen.com
// @contact.email irisdev@mobigen.com

// @host localhost:8080
// @BashPath /
func main() {
	// Initialize Sub module And Read Env, Config
	c, err := Initialize()
	if err != nil {
		fmt.Println(err.Error())
		return
	}

	// Initialization Interconnection of WebServer Layer
	// controller - application - domain - repository - infrastructures
	if err := c.InitDependencyInjection(); err != nil {
		fmt.Println(err.Error())
		return
	}

	// Start sub module and main loop
	c.StartSubModules()

	// Bye Bye
	c.Log.Shutdown()
}
