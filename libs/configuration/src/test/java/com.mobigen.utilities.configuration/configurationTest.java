package com.mobigen.utilities.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class configurationTest {
    @Test
    void configTest() {
        var config = configuration.getConfig(getClass());
        // string
        Assertions.assertEquals("stringValueTest", config.getProperty("stringValue"));
        // int
        Assertions.assertEquals(123, config.getInt("intValue"));
        // boolean
        Assertions.assertEquals("true", config.getProperty("booleanValue"));
        // nested
        Assertions.assertEquals("test's stringTest", config.getProperty("test.stringTest"));

        // nested.nested
        Assertions.assertEquals("123.456.789.10", config.getProperty("test.nestedTest.children"));
    }

    @Test
    void profileTest() {

        System.setProperty("ACTIVE_PROFILE", "dev");
        var config = configuration.getConfig(getClass());
        // string
        Assertions.assertEquals("stringValueTest", config.getProperty("stringValue"));
        // int
        Assertions.assertEquals(123, config.getInt("intValue"));
        // boolean
        Assertions.assertEquals("true", config.getProperty("booleanValue"));

        // nested
        Assertions.assertEquals("test's stringTest", config.getProperty("test.stringTest"));

        // nested.nested
        Assertions.assertEquals("123.456.789.10", config.getProperty("test.nestedTest.children"));
    }
}