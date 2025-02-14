package com.mobigen.vdap.server.services.apiservices;

import org.openmetadata.schema.api.services.CreateApiService;
import org.openmetadata.schema.entity.services.ApiService;
import org.openmetadata.service.mapper.EntityMapper;

public class APIServiceMapper implements EntityMapper<ApiService, CreateApiService> {
  @Override
  public ApiService createToEntity(CreateApiService create, String user) {
    return copy(new ApiService(), create, user)
        .withServiceType(create.getServiceType())
        .withConnection(create.getConnection());
  }
}
