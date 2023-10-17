package com.mobigen.sqlgen.maker;

import com.mobigen.sqlgen.maker.generate.JoinStatementProvider;
import com.mobigen.sqlgen.maker.generate.SelectStatementProvider;
import com.mobigen.sqlgen.maker.generate.StatementProvider;
import com.mobigen.sqlgen.maker.where.Condition;
import com.mobigen.sqlgen.model.SqlTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class JoinMaker implements MakerInterface {
    private final QueryMaker queryMaker;
    private final SqlTable rightTable;
    private JoinHow how;
    private final List<Condition> conditions;

    private JoinMaker(Builder builder) {
        queryMaker = Objects.requireNonNull(builder.queryMaker);
        rightTable = Objects.requireNonNull(builder.rightTable);
        how = builder.how;
        if (how == null) {
            how = JoinHow.INNER;
        }
        conditions = builder.conditions;
    }

    public WhereMaker where(Condition... conditions) {
        return new WhereMaker.Builder()
                .withMaker(this)
                .withConditions(List.of(conditions))
                .build();
    }

    @Override
    public StatementProvider generate() {
        return new JoinStatementProvider.Builder()
                .withSelectStatementProvider(queryMaker.generate())
                .withTable(rightTable)
                .withHow(how)
                .withConditions(conditions)
                .build();
    }

    protected static class Builder {
        private QueryMaker queryMaker;
        private SqlTable rightTable;
        private JoinHow how;
        private final List<Condition> conditions = new ArrayList<>();

        protected Builder withQueryMaker(QueryMaker queryMaker) {
            this.queryMaker = queryMaker;
            return this;
        }

        protected Builder withTable(SqlTable rightTable) {
            this.rightTable = rightTable;
            return this;
        }

        protected Builder withHow(JoinHow how) {
            this.how = how;
            return this;
        }

        protected Builder withConditions(Collection<? extends Condition> conditions) {
            this.conditions.addAll(conditions);
            return this;
        }

        protected JoinMaker build() {
            return new JoinMaker(this);
        }
    }
}
