package com.mobigen.vdap.server.secrets.masker;

import lombok.Getter;

public class EntityMaskerFactory {
  @Getter private static EntityMasker entityMasker;

  private EntityMaskerFactory() {}

  /** Expected to be called only once when the Application starts */
  public static EntityMasker createEntityMasker() {
    if (entityMasker != null) {
      return entityMasker;
    }
    entityMasker = new PasswordEntityMasker();
    return entityMasker;
  }
}
