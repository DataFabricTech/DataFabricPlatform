package com.mobigen.vdap.schema;

import com.mobigen.vdap.schema.entity.services.connections.TestConnectionResult;
import com.mobigen.vdap.schema.type.EntityReference;

import java.util.List;

/**
 * Interface to be implemented by all services entities to provide a way to access all the common
 * fields.
 */
public interface ServiceEntityInterface extends EntityInterface {

  ServiceConnectionEntityInterface getConnection();

  ServiceEntityInterface withOwners(List<EntityReference> owners);

  void setPipelines(List<EntityReference> pipelines);

  void setTestConnectionResult(TestConnectionResult testConnectionResult);

  EnumInterface getServiceType();
}
