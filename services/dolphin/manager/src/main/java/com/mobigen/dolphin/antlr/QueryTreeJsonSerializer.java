package com.mobigen.dolphin.antlr;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
public class QueryTreeJsonSerializer {
    private final String PARSER_CLASS;

    public QueryTreeJsonSerializer() {
        this(ModelSqlParsingVisitor.class);
    }

    public QueryTreeJsonSerializer(Class<?> clazz) {
        this(clazz.getName());
    }

    public QueryTreeJsonSerializer(String className) {
        if (!className.endsWith("$")) {
            className += "$";
        }
        PARSER_CLASS = className;
    }


    public JSONObject serialize(ParseTree tree) {
        var output = new JSONObject();
        if (tree instanceof TerminalNodeImpl) {
            var token = ((TerminalNodeImpl) tree).getSymbol();
            output.put("type", token.getType());
            output.put("text", token.getText());
        } else {
            String name = tree.getClass().getSimpleName()
                    .replaceAll("Context$", "")
                    .toLowerCase();
            List<JSONObject> children = new ArrayList<>();
            for (var i = 0; i < tree.getChildCount(); i++) {
                children.add(serialize(tree.getChild(i)));
            }
            output.put(name, children);
        }
        return output;
    }


    public ParseTree deserialize(JSONObject jsonObject) {
        int invokingState = -1;
        return deserialize(jsonObject, null, invokingState).get(0);
    }

    public static int getTypeInt(JSONObject jsonObject) {
        var type_ = jsonObject.get("type");
        if (type_ instanceof String) {
            try {
                return (int) ModelSqlParser.class.getDeclaredField((String) type_).get(null);
            } catch (NoSuchFieldException e) {
                return -1;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return (int) type_;
    }

    public List<ParseTree> deserialize(JSONObject jsonObject, ParserRuleContext parent, int invokingState) {
        // parent = null, invokingState = -1
        List<ParseTree> output = new ArrayList<>();
        if (jsonObject.keySet().contains("text") && jsonObject.keySet().contains("type")) {
            // leaf object
            Token symbol;
            if (jsonObject.getString("text").equals("<EOF>")) {
                symbol = new CommonToken(-1, jsonObject.getString("text"));
            } else {
                symbol = new CommonToken(getTypeInt(jsonObject), jsonObject.getString("text"));
            }
            output.add(new TerminalNodeImpl(symbol));
            return output;
        } else {
            try {
                for (var key : jsonObject.keySet()) {
                    var className = key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase();
                    Class<?> clazz_ = Class.forName(PARSER_CLASS + className + "Context");
                    ParserRuleContext node = (ParserRuleContext) clazz_.getDeclaredConstructor(ParserRuleContext.class, int.class)
                            .newInstance(parent, invokingState++);
                    for (var obj : jsonObject.getJSONArray(key)) {
                        var children = deserialize((JSONObject) obj, node, invokingState++);
                        children.forEach(child -> {
                            if (child instanceof TerminalNodeImpl) {
                                node.addChild((TerminalNode) child);
                            } else {
                                node.addChild((RuleContext) child);
                            }
                        });
                    }
                    output.add(node);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException | ClassNotFoundException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }

        }
        return output;
    }

    public String convertTreeToString(ParseTree tree) {
        if (tree instanceof TerminalNodeImpl) {
            if (tree.getText().equals("<EOF>")) {
                return "";
            }
            return tree.getText();
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < tree.getChildCount(); i++) {
                builder.append(convertTreeToString(tree.getChild(i)))
                        .append(" ");
            }
            return builder.toString().strip();
        }
    }
}
