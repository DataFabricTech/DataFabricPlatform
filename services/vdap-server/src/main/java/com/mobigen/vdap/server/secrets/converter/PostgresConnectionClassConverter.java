package com.mobigen.vdap.server.secrets.converter;

import com.mobigen.vdap.schema.security.ssl.ValidateSSLClientConfig;
import com.mobigen.vdap.schema.services.connections.database.PostgresConnection;
import com.mobigen.vdap.schema.services.connections.database.common.basicAuth;
import com.mobigen.vdap.server.util.JsonUtils;

import java.util.List;

/**
 * Converter class to get an `Postgres` object.
 */
public class PostgresConnectionClassConverter extends ClassConverter {

  private static final List<Class<?>> SSL_SOURCE_CLASS = List.of(ValidateSSLClientConfig.class);

  private static final List<Class<?>> CONFIG_SOURCE_CLASSES =
      List.of(basicAuth.class);

  public PostgresConnectionClassConverter() {
    super(PostgresConnection.class);
  }

  @Override
  public Object convert(Object object) {
    PostgresConnection postgresConnection =
        (PostgresConnection) JsonUtils.convertValue(object, this.clazz);

    tryToConvert(postgresConnection.getAuthType(), CONFIG_SOURCE_CLASSES)
        .ifPresent(postgresConnection::setAuthType);

    tryToConvert(postgresConnection.getSslConfig(), SSL_SOURCE_CLASS)
        .ifPresent(postgresConnection::setSslConfig);

    return postgresConnection;
  }
}
