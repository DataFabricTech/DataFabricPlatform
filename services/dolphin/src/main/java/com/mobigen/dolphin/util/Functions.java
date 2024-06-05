package com.mobigen.dolphin.util;

import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class Functions {
    public static String getCatalogName(UUID id) {
        return "catalog_" + id.toString().replace("-", "_");
    }
}
