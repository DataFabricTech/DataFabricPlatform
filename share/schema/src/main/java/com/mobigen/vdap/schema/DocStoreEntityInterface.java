package com.mobigen.vdap.schema;

/**
 * Interface to be implemented by all doc store entities to provide a way to access all the common
 * fields.
 */
@SuppressWarnings("unused")
public interface DocStoreEntityInterface {
  String getEntityType();

  Object getData();
}
