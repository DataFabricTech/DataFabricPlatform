package com.mobigen.datafabric.relationship.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public static String getStrCurrentDateTime(String strFormat) {
        // 현재 시간 가져오기
        LocalDateTime now = LocalDateTime.now();
        // 원하는 포맷 설정 (년도 2자리)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(strFormat);
        // 포맷에 맞춰 문자열 반환
        return now.format(formatter);
    }

}
