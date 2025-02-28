package com.mobigen.monitoring.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class UnixTimeUtil {
    // unix time(milliseconds)를 LocalDateTime 객체로 변환하는 함수
    public static LocalDateTime convertUnixTimeToLocalDateTime(Long unixTime) {
        return Instant.ofEpochSecond(unixTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    // LocalDateTime 을 unix time 으로 변환하는 함수
    public static Long convertLocalDateTimeToUnixTime() {
        // LocalDateTime 을 Unix Time 으로 변환 (초 단위)
        return LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    // utc milliseconds 구하는 함수
    public static Long getCurrentMillis() {
        return Instant.now().toEpochMilli();
    }
}
