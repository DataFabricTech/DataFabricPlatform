package com.mobigen.monitoring.service.storage;

import com.mobigen.monitoring.domain.Services;
import com.mobigen.monitoring.dto.request.DatabaseConnectionRequest;
import com.mobigen.monitoring.vo.CheckConnectionResponseVo;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public interface DatabaseManagementService {
    public Boolean checkServiceConnection(DatabaseConnectionRequest request);

    public DataSource createDataSource(DatabaseConnectionRequest request);

    public String buildJdbcUrl(DatabaseConnectionRequest request);

    public List<String> getDatabases(DatabaseConnectionRequest request);

    public Map<String, List<String>> getTables(DatabaseConnectionRequest request);

    public Object getRows(DatabaseConnectionRequest request);

    public Object getSchema(DatabaseConnectionRequest request);

    public List<Map<String, Object>> executeQuery(DatabaseConnectionRequest request, String query);

    public String getDriverClassName(String dbType);

    public Integer checkMinioConnection(DatabaseConnectionRequest request);

    public void initializeServiceTable(String userName);

    public void saveConnections(List<Services> services);

    public Integer getModelCount(Services service);

    public CheckConnectionResponseVo getQueryExecutedTime(Services service);

    public List<Services> saveServicesFromFabricServer();

    public DatabaseConnectionRequest getDatabaseConnectionRequest(String serviceId);

    public Object getCpuSpentTime(String serviceId);

    public Object getCpuSpentTime();

    public Object getMemoryUsage(String serviceId);

    public Object getMemoryUsage();

    public Object getDiskUsage(String serviceId);

    public Object getAllDiskUsage();

    public Object getAverageQueryOutcome(String serviceId);

    public Object getAverageQueryOutcome();

    public Object getSlowQueries(String serviceId);

    public Object getSlowQueries();

    void updateDatabaseInfo(String serviceId);

    void saveDatabaseMonitoringInfo(String serviceId, String ownerName);
}
