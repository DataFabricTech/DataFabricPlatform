package com.mobigen.vdap.schema;

import com.mobigen.vdap.schema.type.EntityReference;
import com.mobigen.vdap.schema.type.LifeCycle;
import com.mobigen.vdap.schema.type.TagLabel;

import java.util.List;

@SuppressWarnings("unchecked")
public interface CreateEntity {
  String getName();

  String getDisplayName();

  String getDescription();

  default List<EntityReference> getOwners() {
    return null;
  }

  default List<EntityReference> getReviewers() {
    return null;
  }

  default List<TagLabel> getTags() {
    return null;
  }

  default Object getExtension() {
    return null;
  }

  default List<String> getDataProducts() {
    return null;
  }

  default LifeCycle getLifeCycle() {
    return null;
  }

  <K extends CreateEntity> K withName(String name);

  <K extends CreateEntity> K withDisplayName(String displayName);

  <K extends CreateEntity> K withDescription(String description);

  default void setOwners(List<EntityReference> owners) {}

  default void setTags(List<TagLabel> tags) {
    /* no-op implementation to be overridden */
  }

  default void setReviewers(List<EntityReference> reviewers) {}

  default <K extends CreateEntity> K withExtension(Object extension) {
    return (K) this;
  }

  default <K extends CreateEntity> K withLifeCycle(LifeCycle lifeCycle) {
    return (K) this;
  }
}
