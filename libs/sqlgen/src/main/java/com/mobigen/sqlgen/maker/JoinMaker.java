package com.mobigen.sqlgen.maker;

import com.mobigen.sqlgen.generate.JoinStatementProvider;
import com.mobigen.sqlgen.generate.StatementProvider;
import com.mobigen.sqlgen.model.JoinMethod;
import com.mobigen.sqlgen.model.SqlTable;
import com.mobigen.sqlgen.where.Condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Join 절을 만들기 위한 값을 빌드하는 클래스
 * statement provider 를 생성 한다.
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class JoinMaker extends OrderUsable implements WhereUsable {
    private final MakerInterface maker;
    private final SqlTable rightTable;
    private JoinMethod how;
    private final List<Condition> conditions;

    private JoinMaker(Builder builder) {
        maker = Objects.requireNonNull(builder.maker);
        rightTable = Objects.requireNonNull(builder.rightTable);
        how = builder.how;
        if (how == null) {
            how = JoinMethod.INNER;
        }
        conditions = builder.conditions;
    }

    public JoinMaker join(SqlTable rightTable, JoinMethod how, Condition... conditions) {
        return new JoinMaker.Builder()
                .withMaker(this)
                .withTable(rightTable)
                .withHow(how)
                .withConditions(List.of(conditions))
                .build();
    }

    public JoinMaker join(SqlTable rightTable, Condition... conditions) {
        return join(rightTable, JoinMethod.INNER, conditions);
    }

    public JoinMaker join(SqlTable rightTable) {
        return join(rightTable, JoinMethod.INNER);
    }

    @Override
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
        private JoinMethod how;
        private final List<Condition> conditions = new ArrayList<>();

        protected Builder withMaker(MakerInterface maker) {
            this.maker = maker;
            return this;
        }

        protected Builder withTable(SqlTable rightTable) {
            this.rightTable = rightTable;
            return this;
        }

        protected Builder withHow(JoinMethod how) {
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
