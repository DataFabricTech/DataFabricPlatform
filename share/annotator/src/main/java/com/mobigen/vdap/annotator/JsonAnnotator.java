package com.mobigen.vdap.annotator;

import org.jsonschema2pojo.CompositeAnnotator;

public class JsonAnnotator extends CompositeAnnotator {

  public JsonAnnotator() {
    // we can add multiple annotators
    super(
        new PasswordAnnotator());
//        new DeprecatedAnnotator());
  }
}
