package com.mobigen.vdap.schema;

import com.mobigen.vdap.schema.type.TagLabel;

import java.util.List;

public interface FieldInterface {
  String getName();

  String getDisplayName();

  String getDescription();

  String getDataTypeDisplay();

  List<TagLabel> getTags();

  default void setTags(List<TagLabel> tags) {
    /* no-op implementation to be overridden */
  }

  List<? extends FieldInterface> getChildren();
}
