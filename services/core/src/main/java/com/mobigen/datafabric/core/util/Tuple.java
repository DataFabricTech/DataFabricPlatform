package com.mobigen.datafabric.core.util;

import lombok.Getter;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
public class Tuple<A, B> {
    A left;
    B right;

    public Tuple(A left, B right) {
        this.left = left;
        this.right = right;
    }
}
