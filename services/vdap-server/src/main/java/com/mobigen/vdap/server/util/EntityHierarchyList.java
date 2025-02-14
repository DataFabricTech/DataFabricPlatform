package com.mobigen.vdap.server.util;

import com.mobigen.vdap.schema.entity.data.EntityHierarchy;

import java.util.List;

public class EntityHierarchyList extends ResultList<EntityHierarchy> {
  @SuppressWarnings("unused")
  public EntityHierarchyList() {}

  public EntityHierarchyList(List<EntityHierarchy> data) {
    super(data, null, null, data.size());
  }
}
