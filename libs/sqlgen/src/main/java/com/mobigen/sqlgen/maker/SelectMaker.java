package com.mobigen.sqlgen.maker;


import com.mobigen.sqlgen.generate.SelectStatementProvider;
import com.mobigen.sqlgen.model.JoinMethod;
import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import com.mobigen.sqlgen.where.Condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Select 절을 만들기 위한 값을 빌드하는 클래스
 * statement provider 를 생성 한다.
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class SelectMaker implements MakerInterface {
    private final SqlTable table;
    private final List<SqlColumn> selectColumns;


    private SelectMaker(Builder builder) {
        this.selectColumns = Objects.requireNonNull(builder.selectColumns);
        this.table = Objects.requireNonNull(builder.table);
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

    public WhereMaker where(Condition... conditions) {
        return new WhereMaker.Builder()
                .withMaker(this)
                .withConditions(List.of(conditions))
                .build();
    }

    @Override
    public SelectStatementProvider generate() {
        return new SelectStatementProvider.Builder()
                .withSelectColumns(selectColumns)
                .withTable(table)
                .build();
    }

    public static FromGatherer select(SqlColumn... columns) {
        return new FromGatherer.Builder()
                .withSelectMakerBuilder(new SelectMaker.Builder()
                        .withSelectColumns(List.of(columns))
                )
                .build();
    }

    public static class FromGatherer implements MakerInterface {
        private final SelectMaker.Builder selectMakerBuilder;

        private FromGatherer(Builder builder) {
            selectMakerBuilder = Objects.requireNonNull(builder.selectMakerBuilder);
        }

        public SelectMaker from(SqlTable table) {
            return selectMakerBuilder
                    .from(table)
                    .build();
        }

        @Override
        public SelectStatementProvider generate() {
            return new SelectStatementProvider.Builder()
                    .withSelectColumns(selectMakerBuilder.selectColumns)
                    .build();
        }

        protected static class Builder {
            private SelectMaker.Builder selectMakerBuilder;

            protected Builder withSelectMakerBuilder(SelectMaker.Builder selectMakerBuilder) {
                this.selectMakerBuilder = selectMakerBuilder;
                return this;
            }

            protected FromGatherer build() {
                return new FromGatherer(this);
            }
        }
    }

    public static class Builder {
        private final List<SqlColumn> selectColumns = new ArrayList<>();
        private SqlTable table;

        protected Builder withSelectColumns(Collection<? extends SqlColumn> columns) {
            this.selectColumns.addAll(columns);
            return this;
        }

        protected Builder from(SqlTable table) {
            this.table = table;
            return this;
        }

        protected SelectMaker build() {
            return new SelectMaker(this);
        }
    }
}
