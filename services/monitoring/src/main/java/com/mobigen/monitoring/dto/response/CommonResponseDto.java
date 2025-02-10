package com.mobigen.monitoring.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommonResponseDto {
    @JsonProperty("code")
    private String code;
    @JsonProperty("errorMsg")
    private String errorMsg;
    @JsonProperty("errorVars")
    private List<String> errorVars;
    @JsonProperty("data")
    private Object data;
}
