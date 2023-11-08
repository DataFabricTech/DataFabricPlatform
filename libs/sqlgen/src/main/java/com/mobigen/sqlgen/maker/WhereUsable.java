package com.mobigen.sqlgen.maker;

import com.mobigen.sqlgen.where.Condition;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public interface WhereUsable extends MakerInterface {
    WhereMaker where(Condition... conditions);
}
