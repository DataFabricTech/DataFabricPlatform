package com.mobigen.monitoring.vo;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class QueryStatisticsVo {
    private Integer successQueries;
    private Integer failedQueries;
}
