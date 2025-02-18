package com.mobigen.vdap.server.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mobigen.vdap.schema.system.EntityError;
import com.mobigen.vdap.schema.type.Paging;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class used for generating JSON response for APIs returning list of objects in the following format: { "data" : [ {
 * json for object 1}, {json for object 2}, ... ] }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultList<T> {

    @JsonProperty("data")
    @NotNull
    private List<T> data;

    @JsonProperty("paging")
    private Paging paging;

    @JsonProperty("errors")
    private List<EntityError> errors;

    public ResultList() {
    }

    public ResultList(List<T> data) {
        this.data = data;
        this.paging = null;
        this.errors = null;
    }

    public ResultList(List<T> data, Integer page, Integer size, Integer total_elements, Integer total_page) {
        this.data = data;
        paging = new Paging()
                        .withPage(page)
                        .withSize(size)
                        .withTotalElements(total_elements)
                        .withTotalPages(total_page);
    }

    public ResultList(List<T> data, Integer offset, Integer limit, Integer total_elements) {
        this.data = data;
        paging = new Paging().withOffset(offset).withLimit(limit).withTotalElements(total_elements);
    }

    /* Conveniently map the data to another type without the need to create a new ResultList */
    public <S> ResultList<S> map(Function<T, S> mapper) {
        return new ResultList<>(data.stream().map(mapper).collect(Collectors.toList()), paging);
    }

    public ResultList(List<T> data, Paging other) {
        this.data = data;
        paging =
                new Paging()
                        .withPage(null)
                        .withSize(null)
                        .withTotalPages(other.getTotalPages())
                        .withOffset(other.getOffset())
                        .withLimit(other.getLimit())
                        .withTotalElements(other.getTotalElements());
    }

    @JsonProperty("data")
    public List<T> getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(List<T> data) {
        this.data = data;
    }

    @JsonProperty("errors")
    public List<EntityError> getErrors() {
        return errors;
    }

    @JsonProperty("errors")
    public void setErrors(List<EntityError> data) {
        this.errors = data;
    }

    @JsonProperty("paging")
    public Paging getPaging() {
        return paging;
    }

    @JsonProperty("paging")
    public ResultList<T> setPaging(Paging paging) {
        this.paging = paging;
        return this;
    }
}
