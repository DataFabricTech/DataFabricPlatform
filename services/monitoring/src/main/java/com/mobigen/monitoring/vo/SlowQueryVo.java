package com.mobigen.monitoring.vo;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SlowQueryVo {
    private String sqlText;
    private Float execTime;
}
