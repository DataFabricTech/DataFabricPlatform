package com.mobigen.sqlgen.maker;

import com.mobigen.sqlgen.maker.where.Condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WhereMaker implements MakerInterface {
    private QueryMaker queryMaker;
    private List<Condition> conditions;
    private WhereMaker(Builder builder) {
        queryMaker = builder.queryMaker;
        conditions = builder.conditions;
    }

    @Override
    public WhereStatementProvider generate() {
        return new WhereStatementProvider.Builder()
                .withSelectStatementProvider(queryMaker.generate())
                .withConditions(conditions)
                .build();
    }

    protected static class Builder{
        private QueryMaker queryMaker;
        private final List<Condition> conditions = new ArrayList<>();

        protected Builder withConditions(Collection<? extends Condition> conditions) {
            this.conditions.addAll(conditions);
            return this;
        }

        public Builder withQueryMaker(QueryMaker queryMaker) {
            this.queryMaker = queryMaker;
            return this;
        }

        protected WhereMaker build() {
            return new WhereMaker(this);
        }
    }
}
