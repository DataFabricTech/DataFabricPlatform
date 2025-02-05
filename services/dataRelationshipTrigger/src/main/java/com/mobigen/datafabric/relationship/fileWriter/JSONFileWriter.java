package com.mobigen.datafabric.relationship.fileWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobigen.datafabric.relationship.data.Metadata;
import com.mobigen.datafabric.relationship.utils.PathUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class JSONFileWriter {
    private final String prefixPath;

    private final ObjectMapper objectMapper;
    public JSONFileWriter(String prefixPath) {
        this.objectMapper = new ObjectMapper();
        this.prefixPath = prefixPath;
    }

    // JSON 파일로 메타데이터를 저장
    public String writeObjectToJsonFile(Metadata meta) throws IOException {
        String filePath = PathUtil.combinePath(this.prefixPath, meta.getId() + ".json");
        PathUtil.ensureDirectoryExists(filePath);
        File file = new File(filePath);
        try(FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(fileOutputStream, meta);
        } catch (IOException e) {
            throw new IOException("Error while writing metadata to file", e);
        }
        return filePath;
    }
}
