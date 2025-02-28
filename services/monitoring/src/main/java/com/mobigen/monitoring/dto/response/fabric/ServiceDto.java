package com.mobigen.monitoring.dto.response.fabric;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ServiceDto {
    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("fullyQualifiedName")
    private String fullyQualifiedName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("deleted")
    private Boolean deleted;
}
