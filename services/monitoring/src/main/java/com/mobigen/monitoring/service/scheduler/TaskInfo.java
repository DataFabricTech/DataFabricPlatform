package com.mobigen.monitoring.service.scheduler;

public record TaskInfo(
        String serviceId,
        Integer period,
        DatabaseConnectionInfo databaseConnectionInfo
) {}
