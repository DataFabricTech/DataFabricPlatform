package com.mobigen.monitoring.vo;

import com.mobigen.monitoring.domain.ConnectionHistory;
import com.mobigen.monitoring.enums.ConnectionStatus;
import lombok.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ServiceVo {
    private UUID serviceID;
    private String name;
    private String displayName;
    private String serviceType;
    private Long createdAt;
    private Long updatedAt;
    private boolean deleted = false;
    private ConnectionStatus connectionStatus;
    private Integer monitoringPeriod;
    private Boolean monitoring;
    private List<ConnectionHistory> connectionHistories;
}
