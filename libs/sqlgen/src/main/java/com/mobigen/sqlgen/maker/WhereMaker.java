package com.mobigen.sqlgen.maker;

import com.mobigen.sqlgen.generate.WhereStatementProvider;
import com.mobigen.sqlgen.where.Condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Where 절을 만들기 위한 값을 빌드하는 클래스
 * statement provider 를 생성 한다.
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class WhereMaker extends OrderUsable implements MakerInterface {
    private final MakerInterface maker;
    private final List<Condition> conditions;

    private WhereMaker(Builder builder) {
        maker = builder.maker;
        conditions = builder.conditions;
    }

    @Override
    public WhereStatementProvider generate() {
        return new WhereStatementProvider.Builder()
                .withStatementProvider(maker.generate())
                .withConditions(conditions)
                .build();
    }

    protected static class Builder {
        private MakerInterface maker;
        private final List<Condition> conditions = new ArrayList<>();

        protected Builder withConditions(Collection<? extends Condition> conditions) {
            this.conditions.addAll(conditions);
            return this;
        }

        public Builder withMaker(MakerInterface maker) {
            this.maker = maker;
            return this;
        }

        protected WhereMaker build() {
            return new WhereMaker(this);
        }
    }
}
