package com.mobigen.monitoring.service.scheduler;

public record TaskInfo(String id, String schedule, DatabaseConnectionInfo databaseConnectionInfo) {
}
