package com.mobigen.monitoring.dto.response;

import com.mobigen.monitoring.vo.ServicesResponse;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ServicesResponseDto {
    private List<ServicesResponse> data;
    private Long totalCount;
}
