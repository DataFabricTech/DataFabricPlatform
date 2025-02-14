package com.mobigen.vdap.server.services.ingestionpipelines;

import org.openmetadata.schema.api.services.ingestionPipelines.CreateIngestionPipeline;
import org.openmetadata.schema.entity.services.ingestionPipelines.IngestionPipeline;
import org.openmetadata.schema.services.connections.metadata.OpenMetadataConnection;
import org.openmetadata.service.OpenMetadataApplicationConfig;
import org.openmetadata.service.mapper.EntityMapper;
import org.openmetadata.service.util.OpenMetadataConnectionBuilder;

public class IngestionPipelineMapper
    implements EntityMapper<IngestionPipeline, CreateIngestionPipeline> {
  private final OpenMetadataApplicationConfig openMetadataApplicationConfig;

  public IngestionPipelineMapper(OpenMetadataApplicationConfig openMetadataApplicationConfig) {
    this.openMetadataApplicationConfig = openMetadataApplicationConfig;
  }

  @Override
  public IngestionPipeline createToEntity(CreateIngestionPipeline create, String user) {
    OpenMetadataConnection openMetadataServerConnection =
        new OpenMetadataConnectionBuilder(openMetadataApplicationConfig).build();

    return copy(new IngestionPipeline(), create, user)
        .withPipelineType(create.getPipelineType())
        .withAirflowConfig(create.getAirflowConfig())
        .withOpenMetadataServerConnection(openMetadataServerConnection)
        .withSourceConfig(create.getSourceConfig())
        .withLoggerLevel(create.getLoggerLevel())
        .withService(create.getService());
  }
}
