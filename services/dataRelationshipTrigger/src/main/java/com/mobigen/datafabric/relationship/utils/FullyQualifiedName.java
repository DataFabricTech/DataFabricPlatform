package com.mobigen.datafabric.relationship.utils;

import lombok.SneakyThrows;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.codec.binary.Hex;
import org.openmetadata.schema.FqnBaseListener;
import org.openmetadata.schema.FqnLexer;
import org.openmetadata.schema.FqnParser;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FullyQualifiedName {

    // Quoted name of format "sss" or unquoted string sss
    private static final Pattern namePattern = Pattern.compile("^(\")([^\"]+)(\")$|^(.*)$");

    private FullyQualifiedName() {
        /* Utility class with private constructor */
    }

    /** Add to an existing valid FQN the given string */
    public static String add(String fqn, String part) {
        return fqn + "." + quoteName(part);
    }

    /** From the given set of string, build FQN. */
    public static String build(String... strings) {
        List<String> list = new ArrayList<>();
        for (String string : strings) {
            list.add(quoteName(string));
        }
        return String.join(".", list);
    }

    @SneakyThrows
    public static String hash(String input) {
        if (input != null) {
            byte[] checksum = MessageDigest.getInstance("MD5").digest(input.getBytes());
            return Hex.encodeHexString(checksum);
        }
        return null;
    }

    public static String buildHash(String... strings) {
        List<String> list = new ArrayList<>();
        for (String string : strings) {
            list.add(hash(quoteName(string)));
        }
        return String.join(".", list);
    }

    public static String buildHash(String fullyQualifiedName) {
        if (fullyQualifiedName != null && !fullyQualifiedName.isEmpty()) {
            String[] split = split(fullyQualifiedName);
            return buildHash(split);
        }
        return fullyQualifiedName;
    }

    public static String[] split(String string) {
        SplitListener listener = new SplitListener();
        walk(string, listener);
        return listener.split();
    }

    private static <L extends FqnBaseListener> void walk(String string, L listener) {
        FqnLexer fqnLexer = new FqnLexer(CharStreams.fromString(string));
        CommonTokenStream tokens = new CommonTokenStream(fqnLexer);
        FqnParser fqnParser = new FqnParser(tokens);
        fqnParser.setErrorHandler(new BailErrorStrategy());
        FqnParser.FqnContext fqn = fqnParser.fqn();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener, fqn);
    }

    public static String getParentFQN(String fqn) {
        // Split fqn of format a.b.c.d and return the parent a.b.c
        String[] split = split(fqn);
        return getParentFQN(split);
    }

    public static String getParentFQN(String... fqnParts) {
        // Fqn parts a b c d are given from fqn a.b.c.d
        if (fqnParts.length <= 1) {
            return null;
        }
        if (fqnParts.length == 2) {
            return fqnParts[0];
        }

        String parent = build(fqnParts[0]);
        for (int i = 1; i < fqnParts.length - 1; i++) {
            parent = add(parent, fqnParts[i]);
        }
        return parent;
    }

    public static String getRoot(String fqn) {
        // Split fqn of format a.b.c.d and return the root a
        String[] split = split(fqn);
        if (split.length <= 1) {
            return null;
        }
        return split[0];
    }

    public static boolean isParent(String childFqn, String parentFqn) {
        // Returns true if the childFqn is indeed the child of parentFqn
        return childFqn.startsWith(parentFqn) && childFqn.length() > parentFqn.length();
    }

    private static class SplitListener extends FqnBaseListener {
        final List<String> list = new ArrayList<>();

        public String[] split() {
            return list.toArray(new String[0]);
        }

        @Override
        public void enterQuotedName(FqnParser.QuotedNameContext ctx) {
            list.add(ctx.getText());
        }

        @Override
        public void enterUnquotedName(FqnParser.UnquotedNameContext ctx) {
            list.add(ctx.getText());
        }
    }

    /** Adds quotes to name as required */
    public static String quoteName(String name) {
        Matcher matcher = namePattern.matcher(name);
        if (!matcher.find() || matcher.end() != name.length()) {
            throw new IllegalArgumentException("Invalid name " + name);
        }

        // Name matches quoted string "sss".
        // If quoted string does not contain "." return unquoted sss, else return quoted "sss"
        if (matcher.group(1) != null) {
            String unquotedName = matcher.group(2);
            return unquotedName.contains(".") ? name : unquotedName;
        }

        // Name matches unquoted string sss
        // If unquoted string contains ".", return quoted "sss", else unquoted sss
        String unquotedName = matcher.group(4);
        if (!unquotedName.contains("\"")) {
            return unquotedName.contains(".") ? "\"" + name + "\"" : unquotedName;
        }
        throw new IllegalArgumentException("Invalid name " + name);
    }

    /** Adds quotes to name as required */
    public static String unquoteName(String name) {
        Matcher matcher = namePattern.matcher(name);
        if (!matcher.find() || matcher.end() != name.length()) {
            throw new IllegalArgumentException("Invalid name " + name);
        }

        // Name matches quoted string "sss".
        // If quoted string does not contain "." return unquoted sss, else return quoted "sss"
        if (matcher.group(1) != null) {
            return matcher.group(2);
        }
        return name;
    }

    public static String getTableFQN(String columnFQN) {
        // Split columnFQN of format databaseServiceName.databaseName.tableName.columnName
        String[] split = split(columnFQN);
        if (split.length != 5) {
            throw new IllegalArgumentException("Invalid fully qualified column name " + columnFQN);
        }
        // Return table FQN of format databaseService.tableName
        return build(split[0], split[1], split[2], split[3]);
    }

    public static String GetContainerTableFQN(String columnFQN) {
        // Split columnFQN of format ObjectServiceName.BucketName.ContainerName.columnName
        String[] split = split(columnFQN);
        if (split.length != 4) {
            throw new IllegalArgumentException("Invalid fully qualified column name of container data " + columnFQN);
        }
        // Return Container TableData FQN of format objectService.BucketName.ContainerName
        return build(split[0], split[1], split[2]);
    }

    public static String getColumnName(String columnFQN) {
        return FullyQualifiedName.split(columnFQN)[4]; // Get from column name from FQN
    }

}
