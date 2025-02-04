package com.mobigen.schema;

import org.openmetadata.schema.auth.TokenType;

import java.util.UUID;

public interface TokenInterface {
  UUID getToken();

  UUID getUserId();

  TokenType getTokenType();

  Long getExpiryDate();

  void setToken(UUID id);

  void setUserId(UUID id);

  void setTokenType(TokenType type);

  void setExpiryDate(Long expiry);
}
