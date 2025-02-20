package com.mobigen.vdap.server.secrets.converter;

import com.mobigen.vdap.schema.services.connections.search.ElasticSearchConnection;
import com.mobigen.vdap.schema.services.connections.search.elasticSearch.ESAPIAuth;
import com.mobigen.vdap.schema.services.connections.search.elasticSearch.ESBasicAuth;
import com.mobigen.vdap.server.util.JsonUtils;

import java.util.List;

public class ElasticSearchConnectionClassConverter extends ClassConverter {

  private static final List<Class<?>> CONFIG_SOURCE_CLASSES =
      List.of(ESBasicAuth.class, ESAPIAuth.class);

  //
  public ElasticSearchConnectionClassConverter() {
    super(ElasticSearchConnection.class);
  }

  @Override
  public Object convert(Object object) {
    ElasticSearchConnection elasticSearchConnection =
        (ElasticSearchConnection) JsonUtils.convertValue(object, this.clazz);

    tryToConvert(elasticSearchConnection.getAuthType(), CONFIG_SOURCE_CLASSES)
        .ifPresent(elasticSearchConnection::setAuthType);

    return elasticSearchConnection;
  }
}
