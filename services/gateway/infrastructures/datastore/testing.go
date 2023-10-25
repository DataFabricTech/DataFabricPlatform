package datastore

import (
	"testing"

	"github.com/datafabric/gateway/common/appdata"
	"github.com/sirupsen/logrus"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// MakeTestDataStore creates a DataStore for use with unit tests.
func (DataStore) MakeTestDataStore(tb testing.TB, log *logrus.Logger) *DataStore {
	ds := makeUnmigratedTestSQLStore(tb, log)
	err := ds.Migrate()
	require.NoError(tb, err)
	return ds
}

func makeUnmigratedTestSQLStore(tb testing.TB, log *logrus.Logger) *DataStore {
	ds, _ := DataStore{}.New("/tmp", log)
	conf := &appdata.DatastoreConfiguration{
		Database: appdata.Sqlite,
		Endpoint: appdata.EndpointInfo{
			Path:   "tmp.db",
			Option: "mode=memory&cache=shared",
		},
		Debug: appdata.DatastoreDebug{
			LogLevel:      "info",
			SlowThreshold: "1sec",
		},
	}
	ds.Connect(conf)
	return ds
}

// CloseConnection closes underlying database connection.
func CloseConnection(tb testing.TB, ds *DataStore) {
	db, err := ds.Orm.DB()
	assert.NoError(tb, err)
	err = db.Close()
	assert.NoError(tb, err)
}
