package com.mobigen.monitoring.vo;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class TableAuditInfo {
    private Integer insertNum;
    private Integer updateNum;
    private Integer deleteNum;
}
