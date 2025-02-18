package com.mobigen.monitoring.dto.response;

import com.mobigen.monitoring.vo.ResponseTimeVo;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ResponseTimesResponseDto {
    private List<ResponseTimeVo> responseTimes;
    private Long totalSize;
}
