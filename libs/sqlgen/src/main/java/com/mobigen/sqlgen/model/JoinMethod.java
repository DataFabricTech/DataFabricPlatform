package com.mobigen.sqlgen.model;

import lombok.Getter;

/**
 * 조인 방법을 나열한 것
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
public enum JoinMethod {
    INNER("inner join"),
    LEFT_OUTER("left outer join"),
    RIGHT_OUTER("right outer join"),
    FULL_OUTER("full outer join"),
    CROSS("cross join"),
    LEFT("left join"),
    RIGHT("right join"),
    ;

    private final String value;

    JoinMethod(String value) {
        this.value = value;
    }
}
