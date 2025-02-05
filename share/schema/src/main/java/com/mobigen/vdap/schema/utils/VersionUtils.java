package com.mobigen.vdap.schema.utils;

import com.mobigen.vdap.schema.api.ServerVersion;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

@Slf4j
public final class VersionUtils {

  private VersionUtils() {}

  public static ServerVersion getServerVersion(String resourceName) {
    ServerVersion version = new ServerVersion();
    try {
      InputStream fileInput = VersionUtils.class.getResourceAsStream(resourceName);
      Properties props = new Properties();
      props.load(fileInput);
      version.setVersion(props.getProperty("version", "unknown"));

      String timestampAsString = props.getProperty("timestamp");
      Long timestamp = timestampAsString != null ? Long.valueOf(timestampAsString) : null;
      version.setTimestamp(timestamp);
    } catch (Exception ie) {
      log.warn("Failed to read catalog version file");
    }
    return version;
  }

  public static String[] getVersionFromString(String input) {
    return input.split(Pattern.quote("."));
  }
}
