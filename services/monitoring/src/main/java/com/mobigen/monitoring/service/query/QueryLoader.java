package com.mobigen.monitoring.service.query;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class QueryLoader {
    public static String loadQuery(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(new ClassPathResource(filePath).getURI())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
