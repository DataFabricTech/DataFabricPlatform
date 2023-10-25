package common

import (
	"github.com/fsnotify/fsnotify"
	"github.com/sirupsen/logrus"
	"github.com/spf13/viper"
)

// ConfigManager config manager
type ConfigManager struct {
	Viper *viper.Viper
	Log   *logrus.Logger
}

// New create config manager
func (ConfigManager) New(log *logrus.Logger) *ConfigManager {
	return &ConfigManager{
		Viper: viper.New(),
		Log:   log,
	}
}

// ReadConfig load config file
func (cm *ConfigManager) ReadConfig(path, name, extension string, config interface{}) error {
	cm.Viper.AddConfigPath(path)
	cm.Viper.SetConfigName(name)
	cm.Viper.SetConfigType(extension)
	err := cm.Viper.ReadInConfig()
	if err != nil {
		return err
	}
	err = cm.Viper.Unmarshal(config)
	if err != nil {
		return err
	}
	return nil
}

// SetOnChanged ..
func (cm *ConfigManager) SetOnChanged(path, name, extension string,
	callback func(config interface{}), config interface{}) error {
	cm.Viper.AddConfigPath(path)
	cm.Viper.SetConfigName(name)
	cm.Viper.SetConfigType(extension)
	cm.Viper.WatchConfig()
	cm.Viper.OnConfigChange(func(e fsnotify.Event) {
		// kind of event
		// Create Op = 1 << iota
		// Write
		// Remove
		// Rename
		// Chmod
		cm.Log.Errorf("[ CONF ] Config File[ %s ] Changed\n", e.Name)
		err := cm.Viper.ReadInConfig()
		if err != nil {
			cm.Log.Errorf("[ CONF ] ERROR. Failed To Read Config[ %s ]", err.Error())
			return
		}
		err = cm.Viper.Unmarshal(config)
		if err != nil {
			cm.Log.Errorf("[ CONF ] ERROR. Failed To Unmarshal Config[ %s ]", err.Error())
			return
		}
		callback(config)
	})
	return nil
}
