package com.mobigen.datafabric.core.util;

import com.mobigen.datafabric.share.protobuf.DataLayer;
import com.mobigen.datafabric.share.protobuf.StorageCommon;
import com.mobigen.datafabric.share.protobuf.Utilities;
import org.junit.jupiter.api.Test;

import static com.mobigen.datafabric.core.util.DataLayerUtilFunction.convertDataOfDataLayer;
import static com.mobigen.datafabric.core.util.DataLayerUtilFunction.convertInputField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
class DataLayerUtilFunctionTest {

    @Test
    void convertDataOfDataLayerTest() {
        var result = convertDataOfDataLayer(
                DataLayer.Column.newBuilder()
                        .setType(Utilities.DataType.STRING)
                        .build(),
                DataLayer.Cell.newBuilder()
                        .setStringValue("test")
                        .build());
        assertInstanceOf(String.class, result);
        assertEquals("test", result);

        result = convertDataOfDataLayer(
                DataLayer.Column.newBuilder()
                        .setType(Utilities.DataType.INT32)
                        .build(),
                DataLayer.Cell.newBuilder()
                        .setInt32Value(123)
                        .build());
        assertInstanceOf(Integer.class, result);
        assertEquals(123, result);

        result = convertDataOfDataLayer(
                DataLayer.Column.newBuilder()
                        .setType(Utilities.DataType.INT64)
                        .build(),
                DataLayer.Cell.newBuilder()
                        .setInt64Value(123L)
                        .build());
        assertInstanceOf(Long.class, result);
        assertEquals(123L, result);
    }

    @Test
    void convertInputFieldTest() {
        var result = convertInputField(
                StorageCommon.InputField.newBuilder()
                        .setValueType(Utilities.DataType.STRING)
                        .setValue("test")
                        .build()
        );
        assertInstanceOf(String.class, result);
        assertEquals("test", result);

        result = convertInputField(
                StorageCommon.InputField.newBuilder()
                        .setValueType(Utilities.DataType.INT32)
                        .setValue("123")
                        .build()
        );
        assertInstanceOf(Integer.class, result);
        assertEquals(123, result);

        result = convertInputField(
                StorageCommon.InputField.newBuilder()
                        .setValueType(Utilities.DataType.INT64)
                        .setValue("123")
                        .build()
        );
        assertInstanceOf(Long.class, result);
        assertEquals(123L, result);
    }
}