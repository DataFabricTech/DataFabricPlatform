package com.mobigen.dolphin.util;

import com.mobigen.dolphin.antlr.ModelSqlParser;
import org.antlr.v4.runtime.VocabularyImpl;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

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

    public static String convertKeywordName(String name) {
        Character specialChar = '"';
        if (name.startsWith("`")) {
            name = specialChar + name.substring(1, name.length() - 1) + specialChar;
        } else if (Arrays.asList(((VocabularyImpl) ModelSqlParser.VOCABULARY).getSymbolicNames())
                .contains("K_" + name.toUpperCase())) {
            name = specialChar + name + specialChar;
        } else if (!Pattern.matches("^[a-zA-Z_][a-zA-Z0-9_]*$", name)) {
            name = specialChar + name + specialChar;
        }
        return name;
    }
}
