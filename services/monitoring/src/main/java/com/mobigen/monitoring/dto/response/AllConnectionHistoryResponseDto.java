package com.mobigen.monitoring.dto.response;

import com.mobigen.monitoring.enums.ConnectionStatus;
import com.mobigen.monitoring.vo.ConnectionHistoryVo;
import lombok.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AllConnectionHistoryResponseDto {
    private List<ConnectionHistoryVo> connectionHistories;
    private Long recentCollectedTime;
}
