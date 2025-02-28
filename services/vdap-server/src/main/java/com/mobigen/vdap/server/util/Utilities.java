package com.mobigen.vdap.server.util;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import com.fasterxml.uuid.Generators;
import jakarta.servlet.http.HttpServletRequest;

public class Utilities {
    public static final DateFormat DATE_TIME_FORMAT;
    public static final DateTimeFormatter DATE_FORMAT;

    static {
//        customDateTimePattern = "yyyy-MM-dd HH:mm:ss.SSSZ"
        DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        DATE_TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); // KST 기준

        DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("Asia/Seoul"));
    }

    private Utilities() {
    }

    public static List<String> getLastSevenDays(long currentEpochTimestampInMilli) {
        List<String> lastSevenDays = new ArrayList<>();

        // Create a formatter for the date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d");

        // Calculate and add the dates for the last seven days
        for (int i = 6; i >= 0; i--) {
            long dayEpochTimestamp =
                    currentEpochTimestampInMilli
                            - ((long) i * 24 * 60 * 60 * 1000); // Subtracting seconds for each day
            LocalDateTime dateTime =
                    LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(dayEpochTimestamp), ZoneId.systemDefault());
            lastSevenDays.add(dateTime.format(formatter));
        }

        return lastSevenDays;
    }

    public static String getMonthAndDateFromEpoch(long epochTimestamp) {
        return getFormattedDateFromEpoch(epochTimestamp, "MMM d");
    }

    public static String getDateFromEpoch(long epochTimestampInMilli) {
        return getFormattedDateFromEpoch(epochTimestampInMilli, "d");
    }

    public static String cleanUpDoubleQuotes(String input) {
        return input.replaceAll("\"", "");
    }

    public static String doubleQuoteRegexEscape(String input) {
        return String.format("%s(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", input);
    }

    private static String getFormattedDateFromEpoch(long epochTimestamp, String format) {
        Instant instant = Instant.ofEpochMilli(epochTimestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        // Define a custom date formatter
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(format);

        return dateTime.format(dateFormat);
    }

    public static UUID generateUUID() {
        return Generators.timeBasedEpochGenerator().generate();
    }

    public static LocalDateTime getLocalDateTime() {
        return LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    public static URI getBaseUri(HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();

        String contextPath = request.getContextPath(); // "/app" 같은 컨텍스트 경로

        // 기본 포트(80, 443)일 경우 포트 생략
        boolean isDefaultPort = (scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443);
        String portString = isDefaultPort ? "" : ":" + port;

        // 최종 Base URI 구성
        return URI.create(String.format("%s://%s%s%s", scheme, host, portString, contextPath));
    }

}
