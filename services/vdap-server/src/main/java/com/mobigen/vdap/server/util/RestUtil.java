package com.mobigen.vdap.server.util;

import com.mobigen.vdap.common.utils.CommonUtil;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.TimeZone;
import java.util.UUID;

public final class RestUtil {
    public static final DateFormat DATE_TIME_FORMAT;
    public static final DateTimeFormatter DATE_FORMAT;

    static {
        // Quoted "Z" to indicate UTC, no timezone offset
//        customDateTimePattern = "yyyy-MM-dd HH:mm:ss.SSSZ"
        DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        DATE_TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); // KST 기준

        DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("Asia/Seoul"));
    }

    private RestUtil() {
    }

    /**
     * Remove leading and trailing slashes
     */
    public static String removeSlashes(String s) {
        s = s.startsWith("/") ? s.substring(1) : s;
        s = s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
        return s;
    }

    public static URI getHref(UriInfo uriInfo, String collectionPath) {
        collectionPath = removeSlashes(collectionPath);
        String uriPath = uriInfo.getBaseUri() + collectionPath;
        return URI.create(uriPath);
    }

    public static URI getHref(URI parent, String child) {
        child = removeSlashes(child);
        child = replaceSpaces(child);
        return URI.create(parent.toString() + "/" + child);
    }

    public static String replaceSpaces(String s) {
        s = s.replace(" ", "%20");
        return s;
    }

    public static URI getHref(UriInfo uriInfo, String collectionPath, String resourcePath) {
        collectionPath = removeSlashes(collectionPath);
        resourcePath = removeSlashes(resourcePath);
        URI uri = getHref(uriInfo, collectionPath);
        return getHref(uri, resourcePath);
    }

    public static URI getHref(UriInfo uriInfo, String collectionPath, UUID id) {
        return getHref(uriInfo, collectionPath, id.toString());
    }

    public static int compareDates(String date1, String date2) throws ParseException {
        return LocalDateTime.parse(date1, DATE_FORMAT)
                .compareTo(LocalDateTime.parse(date2, DATE_FORMAT));
    }

    public static String today(int offsetDays) {
        LocalDate localDate = CommonUtil.getDateByOffset(LocalDate.now(), offsetDays);
        return localDate.format(DATE_FORMAT);
    }

    public static void validateCursors(String before, String after) {
        if (before != null && after != null) {
            throw new IllegalArgumentException("Only one of before or after query parameter allowed");
        }
    }

    public static String encodeCursor(String cursor) {
        return cursor == null
                ? null
                : Base64.getUrlEncoder().encodeToString(cursor.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeCursor(String cursor) {
        return cursor == null ? null : new String(Base64.getUrlDecoder().decode(cursor));
    }

    public static void validateTimestampMilliseconds(Long timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp is required");
        }
        // check if timestamp has 12 or more digits
        // timestamp ms between 2001-09-09 and 2286-11-20 will have 13 digits
        // timestamp ms between 1973-03-03 and 2001-09-09 will have 12 digits
        boolean isMilliseconds = String.valueOf(timestamp).length() >= 12;
        if (!isMilliseconds) {
            throw new BadRequestException(
                    String.format(
                            "Timestamp %s is not valid, it should be in milliseconds since epoch", timestamp));
        }
    }
}
