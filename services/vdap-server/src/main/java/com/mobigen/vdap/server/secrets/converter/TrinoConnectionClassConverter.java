package com.mobigen.vdap.server.secrets.converter;

import com.mobigen.vdap.schema.services.connections.database.TrinoConnection;
import com.mobigen.vdap.schema.services.connections.database.common.basicAuth;
import com.mobigen.vdap.schema.services.connections.database.common.jwtAuth;
import com.mobigen.vdap.server.util.JsonUtils;

import java.util.List;

public class TrinoConnectionClassConverter extends ClassConverter {

  private static final List<Class<?>> CONFIG_SOURCE_CLASSES =
      List.of(basicAuth.class, jwtAuth.class);

  public TrinoConnectionClassConverter() {
    super(TrinoConnection.class);
  }

  @Override
  public Object convert(Object object) {
    TrinoConnection trinoConnection = (TrinoConnection) JsonUtils.convertValue(object, this.clazz);

    tryToConvert(trinoConnection.getAuthType(), CONFIG_SOURCE_CLASSES)
        .ifPresent(trinoConnection::setAuthType);

    return trinoConnection;
  }
}
