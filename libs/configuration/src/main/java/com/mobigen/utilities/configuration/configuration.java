package com.mobigen.utilities.configuration;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

public class configuration {
    public static Configuration getConfig(Class targetClass) {
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

                resourceFile = targetClass.getClassLoader().getResource(fileName);
                if (extend.equals("properties")) type = false;
                if (resourceFile != null)
                    break;
            }


            if (type) { // yml
                var yaml = new Yaml();
                File tempFile = File.createTempFile("temp",".properties");
                InputStream in = resourceFile.openStream();

                TreeMap<String, Map<String, Object>> ymlConfig = yaml.loadAs(in, TreeMap.class);

                FileWriter writer = new FileWriter(tempFile);
                writer.write(toProperties(ymlConfig));
                writer.close();

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

    private static String toProperties(TreeMap<String, Map<String, Object>> config) {
        StringBuilder sb = new StringBuilder();

        for (String key : config.keySet())
            sb.append(toString(key, config.get(key)));

        return sb.toString();
    }

    private static String toString(String key, Object mapr) {
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
