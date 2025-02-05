package com.mobigen.datafabric.relationship.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtil {

    public static String combinePath(String directory, String fileName) {
        Path fullPath = Paths.get(directory, fileName);
        return fullPath.toString();
    }
    public static void ensureDirectoryExists(String filePath) throws IOException {
        // 파일 경로에서 디렉토리 경로 추출
        Path directoryPath = Paths.get(filePath).getParent();
        if (directoryPath != null && !Files.exists(directoryPath)) {
            // 디렉토리가 없으면 생성
            Files.createDirectories(directoryPath);
        }
    }
    public static String getFileName(String filePath) {
        Path path = Paths.get(filePath);
        return path.getFileName().toString();
    }
}
