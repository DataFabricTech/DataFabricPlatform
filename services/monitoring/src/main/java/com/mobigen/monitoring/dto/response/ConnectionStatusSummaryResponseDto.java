package com.mobigen.monitoring.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ConnectionStatusSummaryResponseDto {
    private Long total;
    private Long connected;
    private Long disconnected;
    private Long connectError;
}
