package com.mobigen.monitoring.service.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.mobigen.monitoring.domain.Services;
import com.mobigen.monitoring.dto.request.DatabaseConnectionRequest;
import com.mobigen.monitoring.vo.CheckConnectionResponseVo;
import com.mobigen.monitoring.vo.TableModelInfo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public interface DatabaseManagementService {
    public Boolean checkDatabaseConnection(DatabaseConnectionRequest request);

    public DataSource createDataSource(DatabaseConnectionRequest request);

    public String buildJdbcUrl(DatabaseConnectionRequest request);

    public List<String> getDatabases(DatabaseConnectionRequest request);

    public Map<String, List<String>> getTables(DatabaseConnectionRequest request);

    public Object getRows(DatabaseConnectionRequest request);

    public Object getSchema(DatabaseConnectionRequest request);

    public List<Map<String, Object>> executeQuery(DatabaseConnectionRequest request, String query);

    public String getDriverClassName(String dbType);

    public Integer testMinioConnection(DatabaseConnectionRequest request);

    public void initializeServiceTable(String userName);

    @Transactional
    public void saveConnections(List<Services> services);

    public TableModelInfo getModelCountFromOM(UUID serviceID);

    public Integer getModelCount(Services service);

    public Integer getStorageModelCountFromOM(final UUID serviceId);

    public CheckConnectionResponseVo getQueryExecutedTime(Services service);

    public List<Services> saveServicesToDatabase();

    public void setDatabaseServices(JsonNode databaseServices);

    public void setStorageServiceList(JsonNode storageServices);

    public List<JsonNode> getServiceListFromFabricServer();

    public void setServiceModels(List<JsonNode> currentServices);

    public DatabaseConnectionRequest getDatabaseConnectionRequest(String serviceId);

    public Object getCpuSpentTime(String serviceId);

    public Object getMemoryUsage(String serviceId);

    public Object getDiskUsage(String serviceId);

    public Object getAverageQueryOutcome(String serviceId);

    public Object getSlowQueries(String serviceId);
}
