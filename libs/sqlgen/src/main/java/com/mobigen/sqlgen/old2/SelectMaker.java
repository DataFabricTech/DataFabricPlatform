package com.mobigen.sqlgen.old2;

import com.mobigen.sqlgen.SqlColumn;
import com.mobigen.sqlgen.old2.QueryMaker.FromGatherer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SelectMaker<R> {

    private Function<SelectModel, R> adaptorFunction;
    private final List<QueryMaker<R>> queryMakers = new ArrayList<>();

    public SelectMaker(Function<SelectModel, R> model) {
        this.adaptorFunction = Objects.requireNonNull(model);
    }

    public static FromGatherer<SelectModel> select(SqlColumn... columns) {
        return select(Function.identity(), columns);
    }

    public static <R> FromGatherer<R> select(Function<SelectModel, R> adaptor, SqlColumn... columns) {
        return new FromGatherer.Builder<R>()
                .withColumns(List.of(columns))
                .withSelectMaker(new SelectMaker<>(adaptor))
                .build();
    }

    protected void registerQueryMaker(QueryMaker<R> queryMaker) {
        this.queryMakers.add(queryMaker);
    }

    protected R build() {
        var selectModel = new SelectModel.Builder()
                .withQueryModels(buildModels())
                .build();
        return adaptorFunction.apply(selectModel);
    }

    private List<QueryModel> buildModels() {
        return queryMakers.stream()
                .map(QueryMaker::buildModel)
                .collect(Collectors.toList());
    }
}
