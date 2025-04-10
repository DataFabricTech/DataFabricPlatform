package com.mobigen.vdap.server.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageModel<T> {

    @JsonProperty("page")
    @JsonPropertyDescription("Select Page Number")
    private Integer page;
    /**
     * Offset used in case of offset based pagination.
     */
    @JsonProperty("offset")
    @JsonPropertyDescription("Offset used in case of offset based pagination.")
    private Integer offset;
    /**
     * Page Size
     */
    @JsonProperty("size")
    @JsonPropertyDescription("Page Size")
    private Integer size;
    /**
     * Limit used in case of offset based pagination.
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("Limit used in case of offset based pagination.")
    private Integer limit;
    /**
     * Total Elements Count
     */
    @JsonProperty("total_elements")
    @JsonPropertyDescription("Total Elements Count")
    private Integer totalElements;
    /**
     * Total Page Count
     */
    @JsonProperty("total_pages")
    @JsonPropertyDescription("Total Page Count")
    private Integer totalPages;
    /**
     * Contents
     */
    @JsonProperty("contents")
    @JsonPropertyDescription("Contents")
    private List<T> contents;
}
