package com.mobigen.monitoring.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MonitoringSaveRequestDto {
    private String serviceId;
    private String taskType;
}
