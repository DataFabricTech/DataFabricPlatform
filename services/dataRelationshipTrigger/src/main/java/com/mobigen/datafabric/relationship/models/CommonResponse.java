package com.mobigen.datafabric.relationship.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse {
    @JsonProperty("code")
    private Integer code;
    @JsonProperty("message")
    private String message;
}
