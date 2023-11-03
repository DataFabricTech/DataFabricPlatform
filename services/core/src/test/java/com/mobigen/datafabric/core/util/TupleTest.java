package com.mobigen.datafabric.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
class TupleTest {
    private Tuple<String, Integer> tuple1;

    @BeforeEach
    void setUp() {
        tuple1 = new Tuple<>("test", 123);
    }

    @Test
    void getLeft() {
        assertEquals("test", tuple1.getLeft());
    }

    @Test
    void getRight() {
        assertEquals(123, tuple1.getRight());
    }
}