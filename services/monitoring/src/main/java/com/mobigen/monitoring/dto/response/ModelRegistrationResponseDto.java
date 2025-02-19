package com.mobigen.monitoring.dto.response;

import com.mobigen.monitoring.vo.ModelRegistrationVo;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ModelRegistrationResponseDto {
    private List<ModelRegistrationVo> models;
    private Long totalCount;
}
