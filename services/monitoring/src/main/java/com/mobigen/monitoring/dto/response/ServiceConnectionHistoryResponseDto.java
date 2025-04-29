package com.mobigen.monitoring.dto.response;

import com.mobigen.monitoring.vo.ServiceVo;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ServiceConnectionHistoryResponseDto {
    private ServiceVo data;
    private Long totalCount;
}
