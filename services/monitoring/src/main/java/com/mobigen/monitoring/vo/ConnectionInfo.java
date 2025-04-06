package com.mobigen.monitoring.vo;

import com.mobigen.monitoring.enums.ConnectionStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ConnectionInfo {
    private ConnectionStatus connectionStatus;
    private Long responseTime;
}
