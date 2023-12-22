package com.mobigen.datafabric.core.collector;

import com.mobigen.datafabric.core.util.JdbcConnector;
import com.mobigen.datafabric.share.protobuf.StorageOuterClass;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class JdbcCollector implements DataCollector {
    private Integer depth;
    private String path;

    public JdbcCollector() {
        this("/", 1);
    }

    public JdbcCollector(String path, Integer depth) {
        this.path = path;
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

    private List<String> splitPath() {
        if (path == null) {
            return List.of();
        }
        return List.of(path.replace("/", " ").strip().split(" "));
    }

    @Override
    public List<StorageOuterClass.StorageBrowseData> collect(String urlFormat, Map<String, Object> options, Properties properties, String driver) {
        try (var engine = new JdbcConnector.Builder()
                .withUrlFormat(urlFormat)
                .withUrlOptions(options)
                .withAdvancedOptions(properties)
                .withDriver(driver)
                .build()) {
            var conn = engine.connect();
            String schemaPattern = null;
            String tablePattern = null;
            var split = splitPath();
            if (!split.isEmpty()) {
                schemaPattern = split.get(0);
                if (split.size() > 1) {
                    tablePattern = split.get(1);
                }
            }
            var rs = conn.getMetadata(schemaPattern, tablePattern);

            Map<String, StorageOuterClass.StorageBrowseData.Builder> schema = new HashMap<>();
            Map<String, List<StorageOuterClass.StorageBrowseData>> children = new HashMap<>();
            while (rs.next()) {
                var name = rs.getString(2);
                StorageOuterClass.StorageBrowseData.Builder builder;
                if (!schema.containsKey(name)) {
                    if (name == null) {
                        name = "";
                    }
                    builder = StorageOuterClass.StorageBrowseData.newBuilder()
                            .setName(name)
                            .setType(0)
                            .setDataFormat("SCHEMA")
                            .setStatus(0);
                    schema.put(name, builder);
                }

                if (depth > 1) {
                    var dataFormat = rs.getString(4);
                    if (dataFormat == null) {
                        dataFormat = "";
                    }
                    var child = StorageOuterClass.StorageBrowseData.newBuilder()
                            .setName(rs.getString(3))
                            .setType(1)
                            .setDataFormat(dataFormat)
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
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString("/".replace("/", " ").strip().split(" ")));
        System.out.println(Arrays.toString("/pro".replace("/", " ").strip().split(" ")));
        System.out.println(Arrays.toString("/pro/".replace("/", " ").strip().split(" ")));
        System.out.println(Arrays.toString("/pro/tt".replace("/", " ").strip().split(" ")));

//        var a = new JDBCCollector();
//        a.setDepth(2);
//        a.setPath("/");
//        var properties = new Properties();
//        properties.put("user", "testUser");
//        properties.put("password", "testUser");
//        var result = a.test("jdbc:postgresql://{host}:{port}/{database}", Map.of("host", "192.168.107.28", "port", "14632", "database", "testdb"), properties);
//        System.out.println(result);
    }

}
