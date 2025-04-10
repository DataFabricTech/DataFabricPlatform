package com.mobigen.vdap.server.secrets.converter;

import com.mobigen.vdap.schema.ServiceConnectionEntityInterface;
import com.mobigen.vdap.schema.entity.automations.TestServiceConnectionRequest;
import com.mobigen.vdap.schema.type.*;
import com.mobigen.vdap.server.exception.CustomException;
import com.mobigen.vdap.server.util.JsonUtils;
import com.mobigen.vdap.server.util.ReflectionUtil;

import java.util.List;

/** Converter class to get an `TestServiceConnectionRequest` object. */
public class TestServiceConnectionRequestClassConverter extends ClassConverter {

  private static final List<Class<?>> CONNECTION_CLASSES =
      List.of(StorageConnection.class);

  public TestServiceConnectionRequestClassConverter() {
    super(TestServiceConnectionRequest.class);
  }

  @Override
  public Object convert(Object object) {
    TestServiceConnectionRequest testServiceConnectionRequest =
        (TestServiceConnectionRequest) JsonUtils.convertValue(object, this.clazz);

    try {
      Class<?> clazz =
          ReflectionUtil.createConnectionConfigClass(
              testServiceConnectionRequest.getConnectionType(),
              testServiceConnectionRequest.getServiceType());

      tryToConvertOrFail(testServiceConnectionRequest.getConnection(), CONNECTION_CLASSES)
          .ifPresent(testServiceConnectionRequest::setConnection);

      Object newConnectionConfig =
          ClassConverterFactory.getConverter(clazz)
              .convert(
                  ((ServiceConnectionEntityInterface) testServiceConnectionRequest.getConnection())
                      .getConfig());
      ((ServiceConnectionEntityInterface) testServiceConnectionRequest.getConnection())
          .setConfig(newConnectionConfig);
    } catch (Exception e) {
      throw new CustomException(
          String.format(
              "Failed to convert class instance of %s",
              testServiceConnectionRequest.getConnectionType()), e, object);
    }

    return testServiceConnectionRequest;
  }
}
