package com.mobigen.vdap.schema;

import com.mobigen.vdap.schema.entity.services.connections.TestConnectionResult;
import com.mobigen.vdap.schema.type.EntityReference;

import java.util.List;

public interface StorageEntityInterface extends EntityInterface {

  StorageConnectionEntityInterface getConnection();

  StorageEntityInterface withOwners(List<EntityReference> owners);

  void setPipelines(List<EntityReference> pipelines);

  void setTestConnectionResult(TestConnectionResult testConnectionResult);

  EnumInterface getServiceType();
}
