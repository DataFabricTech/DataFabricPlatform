package com.mobigen.monitoring.service.query;

import com.mobigen.monitoring.exception.CustomException;
import com.mobigen.monitoring.exception.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * resources 폴더의 미리 정의해 놓은 파일을 읽어서 query (String) 로 반환해주는 클래스
 * */
@Service
@Slf4j
public class QueryLoader {
    public static String loadQuery(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(new ClassPathResource(filePath).getURI())));
        } catch (IOException e) {
            log.error("[QueryLoader] Cannot load query file [{}]", filePath, e);

            throw new CustomException(ResponseCode.DFM4000, "Failed to load query file [" + filePath + "]", filePath);
        }
    }
}
