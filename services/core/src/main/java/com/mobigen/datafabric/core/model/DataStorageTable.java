package com.mobigen.datafabric.core.model;

import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import lombok.Getter;

import java.sql.JDBCType;

/**
 * 실제 Table 의 구조를 정의하는 모델 클래스
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
public class DataStorageTable {
    SqlTable table = SqlTable.of("DataStorage");
    SqlColumn id = SqlColumn.of("id", table, JDBCType.VARCHAR);
    SqlColumn adaptorId = SqlColumn.of("adaptor_id", table, JDBCType.VARCHAR);
    SqlColumn name = SqlColumn.of("name", table, JDBCType.VARCHAR);
    SqlColumn url = SqlColumn.of("url", table, JDBCType.VARCHAR);
    SqlColumn userDesc = SqlColumn.of("user_desc", table, JDBCType.VARCHAR);
    SqlColumn totalData = SqlColumn.of("total_data", table, JDBCType.INTEGER);
    SqlColumn regiData = SqlColumn.of("regi_data", table, JDBCType.INTEGER);
    SqlColumn createdBy = SqlColumn.of("created_by", table, JDBCType.VARCHAR);
    SqlColumn createdAt = SqlColumn.of("created_at", table, JDBCType.TIMESTAMP);
    SqlColumn updatedBy = SqlColumn.of("updated_by", table, JDBCType.VARCHAR);
    SqlColumn updatedAt = SqlColumn.of("updated_at", table, JDBCType.TIMESTAMP);
    SqlColumn deletedBy = SqlColumn.of("deleted_by", table, JDBCType.VARCHAR);
    SqlColumn deletedAt = SqlColumn.of("deleted_at", table, JDBCType.TIMESTAMP);
    SqlColumn status = SqlColumn.of("status", table, JDBCType.INTEGER);
    SqlColumn lastConnectionCheckedAt = SqlColumn.of("last_connection_checked_at", table, JDBCType.TIMESTAMP);
    SqlColumn lastSyncAt = SqlColumn.of("last_sync_at", table, JDBCType.TIMESTAMP);

    SqlColumn syncEnable = SqlColumn.of("sync_enable", table, JDBCType.BOOLEAN);
    SqlColumn syncType = SqlColumn.of("sync_type", table, JDBCType.INTEGER);
    SqlColumn syncWeek = SqlColumn.of("sync_week", table, JDBCType.INTEGER);
    SqlColumn syncRunTime = SqlColumn.of("sync_run_time", table, JDBCType.VARCHAR);

    SqlColumn monitoringEnable = SqlColumn.of("monitoring_enable", table, JDBCType.BOOLEAN);
    SqlColumn monitoringProtocol = SqlColumn.of("monitoring_protocol", table, JDBCType.VARCHAR);
    SqlColumn monitoringHost = SqlColumn.of("monitoring_host", table, JDBCType.VARCHAR);
    SqlColumn monitoringPort = SqlColumn.of("monitoring_port", table, JDBCType.VARCHAR);
    SqlColumn monitoringSql = SqlColumn.of("monitoring_sql", table, JDBCType.VARCHAR);
    SqlColumn monitoringPeriod = SqlColumn.of("monitoring_period", table, JDBCType.INTEGER);
    SqlColumn monitoringTimeout = SqlColumn.of("monitoring_timeout", table, JDBCType.INTEGER);
    SqlColumn monitoringSuccessThreshold = SqlColumn.of("monitoring_success_threshold", table, JDBCType.INTEGER);
    SqlColumn monitoringFailThreshold = SqlColumn.of("monitoring_fail_threshold", table, JDBCType.INTEGER);

    SqlColumn autoAddSettingEnable = SqlColumn.of("auto_add_setting_enable", table, JDBCType.BOOLEAN);
}
