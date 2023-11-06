package com.mobigen.datafabric.core.collector;

import com.mobigen.datafabric.share.protobuf.StorageOuterClass;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class PostgresCollector implements DataCollector {
    private Integer depth;
    private String path;

    public PostgresCollector() {
        this(1);
    }

    public PostgresCollector(Integer depth) {
        this.depth = depth;
    }

    @Override
    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getQuery() {
        String select;
        if (depth <= 1) {
            select = "schemaname";
        } else {
            select = "schemaname, tablename";
        }
        var sql = "SELECT " + select + " FROM pg_catalog.pg_tables";
        if (path != null && !path.isBlank()) {
            var split = path.split("/");
            if (split.length > 1) {
                var schema = split[1];
                sql = sql + " where schemaname = '" + schema + "'";
            }
        }
        return sql;
    }

    @Override
    public List<StorageOuterClass.StorageBrowseData> parse(ResultSet rs) throws SQLException {
        Map<String, StorageOuterClass.StorageBrowseData.Builder> schema = new HashMap<>();
        Map<String, List<StorageOuterClass.StorageBrowseData>> children = new HashMap<>();
        while (rs.next()) {
            var name = rs.getString(1);
            StorageOuterClass.StorageBrowseData.Builder builder;
            if (!schema.containsKey(name)) {
                builder = StorageOuterClass.StorageBrowseData.newBuilder()
                        .setName(name)
                        .setType(0)
                        .setDataFormat("SCHEMA")
                        .setStatus(0);
                schema.put(name, builder);
            }

            if (depth > 1) {
                var child = StorageOuterClass.StorageBrowseData.newBuilder()
                        .setName(rs.getString(2))
                        .setType(1)
                        .setDataFormat("TABLE")
                        .setStatus(0)
                        .build();
                if (children.containsKey(name)) {
                    children.get(name).add(child);
                } else {
                    List<StorageOuterClass.StorageBrowseData> childrenList = new ArrayList<>();
                    childrenList.add(child);
                    children.put(name, childrenList);
                }
            }
        }
        return schema.entrySet().stream().map(x -> x.getValue()
                        .addAllChildren(children.getOrDefault(x.getKey(), List.of()))
                        .build())
                .collect(Collectors.toList());
    }

}
