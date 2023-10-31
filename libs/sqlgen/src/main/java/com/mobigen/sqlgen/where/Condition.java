package com.mobigen.sqlgen.where;

/**
 * Where 조건 정의용 인터페이스
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public interface Condition {
    String operator();

    String getStatement();
}
