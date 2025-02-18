package com.mobigen.vdap.server.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommonResponse {
    @JsonProperty("code")
    private String code;
    @JsonProperty("errorMsg")
    private String errorMsg;
    @JsonProperty("errorVars")
    private List<String> errorVars;
    @JsonProperty("data")
    private Object data;
}
