package com.mobigen.sqlgen.old2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelectModel {

    private final List<QueryModel> queryModels;
    private SelectModel(Builder builder) {
        this.queryModels = Objects.requireNonNull(builder.queryModels);
        if (queryModels.isEmpty()) {
//            throw new InvalidSqlException()
        }
    }

    public String generate() {
        return "";
    }


    protected static class Builder {
        private final List<QueryModel> queryModels = new ArrayList<>();

        public Builder withQueryModels(List<QueryModel> queryModels) {
            this.queryModels.addAll(queryModels);
            return this;
        }
        protected SelectModel build() {
            return new SelectModel(this);
        }
    }
}
