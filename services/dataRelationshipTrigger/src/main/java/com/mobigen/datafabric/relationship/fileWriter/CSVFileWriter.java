package com.mobigen.datafabric.relationship.fileWriter;

import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.List;

import static com.mobigen.datafabric.relationship.utils.PathUtil.combinePath;
import static com.mobigen.datafabric.relationship.utils.PathUtil.ensureDirectoryExists;

@Slf4j
public class CSVFileWriter {
    private PrintWriter printWriter;
    private String[] headers;

    // init 메서드: 파일 생성 및 헤더 작성
    public String init(String path, String fileName, String[] headers) throws IOException {
        String csvFile = combinePath(path, fileName);
        ensureDirectoryExists(csvFile);
        try {
            FileWriter fileWriter = new FileWriter(csvFile, true); // true: append 모드
            printWriter = new PrintWriter(fileWriter);
            // 헤더가 파일에 없을 경우 작성
            printWriter.println(String.join(",", headers));
            this.headers = headers;
        } catch (IOException e) {
            log.error("Error while initializing DataWriter: {}", e.getMessage());
            throw e;
        }
        return csvFile;
    }

    // write 메서드: 데이터를 받아 파일에 추가
    public void write(String[] data) {
        if (printWriter != null) {
            printWriter.println(String.join(",", data));
            printWriter.flush(); // 버퍼에 있는 데이터를 파일로 즉시 전송
        }
    }

    // write 메서드: 객체 리스트를 받아 파일에 추가
    public void write(List<?> objects) {
        if (printWriter != null) {
            for (Object obj : objects) {
                write(obj); // 단일 객체 기록 메서듣 활용
            }
        }
    }

    public void write(Object obj) {
        if (printWriter != null) {
            StringBuilder row = makeRow(obj);
            if (row != null) {
                printWriter.println(row.substring(0, row.length() - 1)); // 마지막 콤마 제거
                printWriter.flush();
            }
        }
    }

    private StringBuilder makeRow(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        StringBuilder row = new StringBuilder();
        for (String header : headers) {
            for (Field field : fields) {
                field.setAccessible(true); // private 필드 접근 허용
                if (field.getName().equalsIgnoreCase(header)) {
                    try {
                        row.append(field.get(obj)).append(",");
                    } catch (IllegalAccessException e) {
                        log.error("Error while writing data: {}", e.getMessage());
                        return null;
                    }
                }
            }
        }
        return row;
    }


    // close 메서드: 파일 닫기
    public void close() {
        if (printWriter != null) {
            printWriter.close();
        }
    }
}
