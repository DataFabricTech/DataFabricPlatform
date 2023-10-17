package com.mobigen.sqlgen.model;

import lombok.Getter;

@Getter
public enum JoinHow {
    INNER("inner join"),
    LEFT_OUTER("left outer join"),
    RIGHT_OUTER("right outer join"),
    FULL_OUTER("full outer join"),
    CROSS("cross join"),
    LEFT("left inner join"), // left inner
    RIGHT("right inner join"),
    ; // right inner

    private final String value;

    JoinHow(String value) {
        this.value = value;
    }
}
