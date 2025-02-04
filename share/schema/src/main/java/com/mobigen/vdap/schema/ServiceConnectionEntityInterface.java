package com.mobigen.vdap.schema;

/**
 * Interface to be implemented by all services entities to provide a way to access all the common
 * fields.
 */
public interface ServiceConnectionEntityInterface {

  Object getConfig();

  void setConfig(Object config);
}
