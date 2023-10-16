package com.mobigen.sqlgen.list;

public class SelectModel {
    private SelectModel(Builder builder) {

    }

    public static class Builder {

        public SelectModel build() {
            return new SelectModel(this);
        }
    }
}
