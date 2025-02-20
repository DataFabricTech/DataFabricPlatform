package com.mobigen.vdap.common.annotations;

import org.jsonschema2pojo.CompositeAnnotator;

public class JsonAnnotator extends CompositeAnnotator {

  public JsonAnnotator() {
    // we can add multiple annotators
    super(
        new PasswordAnnotator());
//        new DeprecatedAnnotator());
  }
}
