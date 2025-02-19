package com.mobigen.monitoring.dto.response;

import com.mobigen.monitoring.vo.IngestionHistoryVo;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class IngestionHistoryResponseDto {
    private List<IngestionHistoryVo> ingestionHistories;
    private Long totalCount;
}
