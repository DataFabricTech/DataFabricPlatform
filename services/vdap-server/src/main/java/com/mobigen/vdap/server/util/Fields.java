package com.mobigen.vdap.server.util;

import com.mobigen.vdap.common.utils.CommonUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Fields implements Iterable<String> {
    public static final Fields EMPTY_FIELDS = new Fields(Collections.emptySet());
    private final Set<String> fieldList;

    public Fields(Set<String> fieldList) {
        this.fieldList = fieldList;
    }

    public Fields(Set<String> allowedFields, String fieldsParam) {
        if (CommonUtil.nullOrEmpty(fieldsParam)) {
            fieldList = new HashSet<>();
            return;
        }
        fieldList = new HashSet<>(Arrays.asList(fieldsParam.replace(" ", "").split(",")));
        for (String field : fieldList) {
            if (!allowedFields.contains(field)) {
                throw new IllegalArgumentException(String.format("Invalid field name %s", field));
            }
        }
    }

    public Fields(Set<String> allowedFields, Set<String> fieldsParam) {
        if (CommonUtil.nullOrEmpty(fieldsParam)) {
            fieldList = new HashSet<>();
            return;
        }
        for (String field : fieldsParam) {
            if (!allowedFields.contains(field)) {
                throw new IllegalArgumentException(String.format("Invalid field name %s", field));
            }
        }
        fieldList = new HashSet<>(fieldsParam);
    }

    public void addField(Set<String> allowedFields, String field) {
        if (!allowedFields.contains(field)) {
            throw new IllegalArgumentException(String.format("Invalid field name %s", field));
        }
        fieldList.add(field);
    }

    @Override
    public String toString() {
        return String.join(",", fieldList);
    }

    public boolean contains(String field) {
        return fieldList.contains(field);
    }

    @Override
    public Iterator<String> iterator() {
        return fieldList.iterator();
    }
}
