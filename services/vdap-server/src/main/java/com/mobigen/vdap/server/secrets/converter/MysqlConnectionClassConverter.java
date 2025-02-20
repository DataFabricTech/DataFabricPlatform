package com.mobigen.vdap.server.secrets.converter;

import com.mobigen.vdap.schema.security.ssl.ValidateSSLClientConfig;
import com.mobigen.vdap.schema.services.connections.database.MysqlConnection;
import com.mobigen.vdap.schema.services.connections.database.common.basicAuth;
import com.mobigen.vdap.server.util.JsonUtils;

import java.util.List;

public class MysqlConnectionClassConverter extends ClassConverter {

  private static final List<Class<?>> CONFIG_SOURCE_CLASSES =
      List.of(basicAuth.class);

  private static final List<Class<?>> SSL_SOURCE_CLASS = List.of(ValidateSSLClientConfig.class);

  public MysqlConnectionClassConverter() {
    super(MysqlConnection.class);
  }

  @Override
  public Object convert(Object object) {
    MysqlConnection mysqlConnection = (MysqlConnection) JsonUtils.convertValue(object, this.clazz);

    tryToConvert(mysqlConnection.getAuthType(), CONFIG_SOURCE_CLASSES)
        .ifPresent(mysqlConnection::setAuthType);

    tryToConvert(mysqlConnection.getSslConfig(), SSL_SOURCE_CLASS)
        .ifPresent(mysqlConnection::setSslConfig);

    return mysqlConnection;
  }
}
