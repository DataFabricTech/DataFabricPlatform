package com.mobigen.datafabric.core.util;

import com.mobigen.datafabric.share.protobuf.DataLayer;
import com.mobigen.datafabric.share.protobuf.StorageCommon;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class DataLayerUtilFunction {

    public static Object convertDataOfDataLayer(DataLayer.Column column, DataLayer.Cell cell) {
        return switch (column.getType()) {
            case STRING, UNRECOGNIZED -> cell.getStringValue();
            case INT32 -> cell.getInt32Value();
            case INT64 -> cell.getInt64Value();
            case BOOL -> cell.getBoolValue();
            case BYTES -> cell.getBytesValue();
            case FLOAT -> cell.getFloatValue();
            case DOUBLE -> cell.getDoubleValue();
            case DATETIME -> cell.getTimeValue();
        };
    }

    public static Object convertInputField(StorageCommon.InputField inputField) {
        return switch (inputField.getValueType()) {
            case STRING, UNRECOGNIZED -> inputField.getValue();
            case INT32 -> Integer.valueOf(inputField.getValue());
            case INT64 -> Long.valueOf(inputField.getValue());
            case BOOL -> Boolean.valueOf(inputField.getValue());
            case BYTES -> Byte.valueOf(inputField.getValue());
            case FLOAT -> Float.valueOf(inputField.getValue());
            case DOUBLE -> Double.valueOf(inputField.getValue());
            case DATETIME -> inputField.getValue();
        };
    }
}
