package com.mobigen.vdap.server.secrets.converter;

import com.mobigen.vdap.schema.services.connections.database.HiveConnection;
import com.mobigen.vdap.schema.services.connections.database.MysqlConnection;
import com.mobigen.vdap.schema.services.connections.database.PostgresConnection;
import com.mobigen.vdap.server.util.JsonUtils;

import java.util.List;

/** Converter class to get an `DatalakeConnection` object. */
public class HiveConnectionClassConverter extends ClassConverter {

  private static final List<Class<?>> CONFIG_SOURCE_CLASSES =
      List.of(MysqlConnection.class, PostgresConnection.class);

  public HiveConnectionClassConverter() {
    super(HiveConnection.class);
  }

  @Override
  public Object convert(Object object) {
    HiveConnection hiveConnection = (HiveConnection) JsonUtils.convertValue(object, this.clazz);

    tryToConvert(hiveConnection.getMetastoreConnection(), CONFIG_SOURCE_CLASSES)
        .ifPresent(hiveConnection::setMetastoreConnection);

    return hiveConnection;
  }
}
