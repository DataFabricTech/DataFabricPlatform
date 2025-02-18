package com.mobigen.monitoring.vo;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AvgResponseTime {
    private UUID serviceId;
    private String serviceName;
    private String serviceDisplayName;
    private Long executeAt;
    private String executeBy;
    private Long queryExecutionTime;
}
