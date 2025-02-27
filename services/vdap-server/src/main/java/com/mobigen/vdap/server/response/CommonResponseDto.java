package com.mobigen.vdap.server.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mobigen.vdap.schema.type.Paging;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponseDto {
    @JsonProperty("code")
    private String code;
    @JsonProperty("errorMsg")
    private String errorMsg;
    @JsonProperty("errorData")
    private Map<String, Object> errorData;
    @JsonProperty("data")
    private Object data;
}
