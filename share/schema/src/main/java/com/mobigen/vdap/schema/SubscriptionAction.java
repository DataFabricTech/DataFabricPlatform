package com.mobigen.vdap.schema;

import java.util.Collections;
import java.util.Set;

public interface SubscriptionAction {
  default Set<String> getReceivers() {
    return Collections.emptySet();
  }

  default Boolean getSendToAdmins() {
    return false;
  }

  default Boolean getSendToOwners() {
    return false;
  }

  default Boolean getSendToFollowers() {
    return false;
  }
}
