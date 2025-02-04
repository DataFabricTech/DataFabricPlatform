package com.mobigen.vdap.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mobigen.vdap.common.utils.CommonUtil;
import com.mobigen.vdap.schema.entity.type.Style;
import com.mobigen.vdap.schema.type.*;
import com.mobigen.vdap.schema.utils.EntityInterfaceUtil;

import java.net.URI;
import java.util.*;

/** Interface to be implemented by all entities to provide a way to access all the common fields. */
@SuppressWarnings("unused")
public interface EntityInterface {
  // Lower case entity name to canonical entity name map
  Map<String, String> CANONICAL_ENTITY_NAME_MAP = new HashMap<>();
  Map<String, Class<? extends EntityInterface>> ENTITY_TYPE_TO_CLASS_MAP = new HashMap<>();

  UUID getId();

  String getDescription();

  String getDisplayName();

  String getName();

  default Boolean getDeleted() {
    return null;
  }

  Double getVersion();

  String getUpdatedBy();

  Long getUpdatedAt();

  URI getHref();

  ChangeDescription getChangeDescription();

  default List<EntityReference> getOwners() {
    return null;
  }

  default List<TagLabel> getTags() {
    return null;
  }

  default ProviderType getProvider() {
    return null;
  }

  default List<EntityReference> getFollowers() {
    return null;
  }

  default Votes getVotes() {
    return null;
  }

  default Object getExtension() {
    return null;
  }

  default List<EntityReference> getChildren() {
    return null;
  }

  default List<EntityReference> getReviewers() {
    return null;
  }

  default List<EntityReference> getExperts() {
    return null;
  }

  default EntityReference getDomain() {
    return null;
  }

  default List<EntityReference> getDataProducts() {
    return null;
  }

  default Style getStyle() {
    return null;
  }

  default LifeCycle getLifeCycle() {
    return null;
  }

  default AssetCertification getCertification() {
    return null;
  }

  void setId(UUID id);

  void setDescription(String description);

  void setDisplayName(String displayName);

  void setName(String name);

  void setVersion(Double newVersion);

  void setChangeDescription(ChangeDescription changeDescription);

  default void setDeleted(Boolean flag) {}

  void setUpdatedBy(String admin);

  void setUpdatedAt(Long updatedAt);

  void setHref(URI href);

  default void setTags(List<TagLabel> tags) {
    /* no-op implementation to be overridden */
  }

  default void setOwners(List<EntityReference> owners) {
    /* no-op implementation to be overridden */
  }

  default void setExtension(Object extension) {
    /* no-op implementation to be overridden */
  }

  default void setChildren(List<EntityReference> entityReference) {
    /* no-op implementation to be overridden */
  }

  default void setReviewers(List<EntityReference> entityReference) {
    /* no-op implementation to be overridden */
  }

  default void setExperts(List<EntityReference> entityReference) {
    /* no-op implementation to be overridden */
  }

  default void setDomain(EntityReference entityReference) {
    /* no-op implementation to be overridden */
  }

  default void setDataProducts(List<EntityReference> dataProducts) {
    /* no-op implementation to be overridden */
  }

  default void setFollowers(List<EntityReference> followers) {
    /* no-op implementation to be overridden */
  }

  default void setVotes(Votes vote) {
    /* no-op implementation to be overridden */
  }

  default void setStyle(Style style) {
    /* no-op implementation to be overridden */
  }

  default void setLifeCycle(LifeCycle lifeCycle) {
    /* no-op implementation to be overridden */
  }

  default void setCertification(AssetCertification certification) {
    /* no-op implementation to be overridden */
  }

  <T extends EntityInterface> T withHref(URI href);

  @JsonIgnore
  default EntityReference getEntityReference() {
    return new EntityReference()
        .withId(getId())
        .withName(getName())
        .withDescription(getDescription())
        .withDisplayName(CommonUtil.nullOrEmpty(getDisplayName()) ? getName() : getDisplayName())
        .withType(
            CANONICAL_ENTITY_NAME_MAP.get(this.getClass().getSimpleName().toLowerCase(Locale.ROOT)))
        .withDeleted(getDeleted())
        .withHref(getHref());
  }
}
