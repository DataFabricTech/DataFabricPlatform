package com.mobigen.libs.configuration;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class Config {
    public Configuration getConfig() {
        var config = new PropertiesConfiguration();
        try {
            var baseFileName = "application";
            var profile = System.getenv("ACTIVE_PROFILE");
            var extendArr = new String[]{"yml", "yaml", "properties"};
            boolean type = true;

            URL resourceFile = null;

            for (var extend : extendArr) {
                String fileName;
                if (profile != null)
                    fileName = baseFileName + "-" + profile + "." + extend;
                else
                    fileName = baseFileName + "." + extend;

                resourceFile = getClass().getClassLoader().getResource(fileName);
                if (extend.equals("properties")) type = false;
                if (resourceFile != null)
                    break;
            }


            if (type) { // yml
                var yml = new Yaml();
                InputStream in = resourceFile.openStream();

                Map<String, Object> ymlConfig = yml.load(in);
                var properties = new Properties();

                toProperties("", ymlConfig, properties);
                File tempFile = File.createTempFile("temp",".properties");
                properties.store(new FileWriter(tempFile), "Converted form YAML");

                config.load(tempFile);
                tempFile.delete();
            } else { // profiles
                config.load(resourceFile.getFile());
            }
        } catch (Exception e) {
            return null;
        }

        return config;
    }

    private void toProperties(String prefix, Map<String, Object> map, Properties properties) {
        for (Map.Entry<String, Object> entry: map.entrySet()) {
            var key = prefix.isEmpty() ? entry.getKey(): String.format("%s.%s", prefix, entry.getKey());
            var value = entry.getValue();
            if (value instanceof Map) {
                toProperties(key, (Map<String, Object>) value, properties);
            } else if (value instanceof List){
                var list = (List<Object>) value;
                for (int i = 0; i < list.size(); i++) {
                    properties.put(key + "[" + i + "]", list.get(i).toString());
                }
            } else {
                properties.put(key, value.toString());
            }
        }
    }


    private String toProperties(TreeMap<String, Map<String, Object>> config) {
        StringBuilder sb = new StringBuilder();

        for (String key : config.keySet())
            sb.append(toString(key, config.get(key)));

        return sb.toString();
    }

    private String toString(String key, Object mapr) {
        StringBuilder sb = new StringBuilder();

        if (!(mapr instanceof Map)) {
            sb.append(key + "=" + mapr + "\n");
            return sb.toString();
        }

        Map<String, Object> map = (Map<String, Object>) mapr;

        for (String mapKey : map.keySet()) {
            if (map.get(mapKey) instanceof Map) {
                sb.append(toString(key + "." + mapKey, map.get(mapKey)));
            } else {
                sb.append(String.format("%s.%s=%s%n", key, mapKey, map.get(mapKey).toString()));
            }
        }

        return sb.toString();
    }
}
