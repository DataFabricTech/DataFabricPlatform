package com.mobigen.sqlgen.maker;

import com.mobigen.sqlgen.generate.JoinStatementProvider;
import com.mobigen.sqlgen.generate.StatementProvider;
import com.mobigen.sqlgen.model.JoinHow;
import com.mobigen.sqlgen.model.SqlTable;
import com.mobigen.sqlgen.where.Condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class JoinMaker implements MakerInterface {
    private final MakerInterface maker;
    private final SqlTable rightTable;
    private JoinHow how;
    private final List<Condition> conditions;

    private JoinMaker(Builder builder) {
        maker = Objects.requireNonNull(builder.maker);
        rightTable = Objects.requireNonNull(builder.rightTable);
        how = builder.how;
        if (how == null) {
            how = JoinHow.INNER;
        }
        conditions = builder.conditions;
    }

    public JoinMaker join(SqlTable rightTable, JoinHow how, Condition... conditions) {
        return new JoinMaker.Builder()
                .withMaker(this)
                .withTable(rightTable)
                .withHow(how)
                .withConditions(List.of(conditions))
                .build();
    }

    public JoinMaker join(SqlTable rightTable, Condition... conditions) {
        return join(rightTable, JoinHow.INNER, conditions);
    }

    public JoinMaker join(SqlTable rightTable) {
        return join(rightTable, JoinHow.INNER);
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
                .withStatementProvider(maker.generate())
                .withTable(rightTable)
                .withHow(how)
                .withConditions(conditions)
                .build();
    }

    protected static class Builder {
        private MakerInterface maker;
        private SqlTable rightTable;
        private JoinHow how;
        private final List<Condition> conditions = new ArrayList<>();

        protected Builder withMaker(MakerInterface maker) {
            this.maker = maker;
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
