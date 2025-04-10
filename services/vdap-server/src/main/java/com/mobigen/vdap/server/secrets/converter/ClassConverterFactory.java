package com.mobigen.vdap.server.secrets.converter;

import lombok.Getter;
import com.mobigen.vdap.schema.entity.automations.TestServiceConnectionRequest;
import com.mobigen.vdap.schema.entity.automations.Workflow;
import com.mobigen.vdap.schema.services.connections.database.*;
import com.mobigen.vdap.schema.services.connections.search.ElasticSearchConnection;

import java.util.Map;

/**
 * Factory class to get a `ClassConverter` based on the service class.
 */
public final class ClassConverterFactory {
    private ClassConverterFactory() {
        /* Final Class */
    }

    @Getter
    private static final Map<Class<?>, ClassConverter> converterMap;

    static {
        converterMap =
                Map.ofEntries(
                        Map.entry(ElasticSearchConnection.class, new ElasticSearchConnectionClassConverter()),
                        Map.entry(HiveConnection.class, new HiveConnectionClassConverter()),
                        Map.entry(MysqlConnection.class, new MysqlConnectionClassConverter()),
                        Map.entry(PostgresConnection.class, new PostgresConnectionClassConverter()),
                        Map.entry(
                                TestServiceConnectionRequest.class,
                                new TestServiceConnectionRequestClassConverter()),
                        Map.entry(TrinoConnection.class, new TrinoConnectionClassConverter()),
                        Map.entry(Workflow.class, new WorkflowClassConverter()));
    }

    public static ClassConverter getConverter(Class<?> clazz) {
        return converterMap.getOrDefault(clazz, new DefaultConnectionClassConverter(clazz));
    }
}
