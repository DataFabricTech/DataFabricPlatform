package com.mobigen.monitoring.vo;

import com.mobigen.monitoring.enums.ConnectionStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class CheckConnectionResponseVo {
    private Long responseTime;
    private ConnectionStatus status;
}
