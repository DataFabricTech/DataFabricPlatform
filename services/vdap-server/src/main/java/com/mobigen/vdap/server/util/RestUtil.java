package com.mobigen.vdap.server.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.util.Arrays;
import java.util.UUID;


@Slf4j
public final class RestUtil {

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

    public static URI getHref(URI baseUri, String child) {
        child = removeSlashes(child);
        child = replaceSpaces(child);
        return URI.create(baseUri.toString() + "/" + child);
    }

    public static String replaceSpaces(String s) {
        s = s.replace(" ", "%20");
        return s;
    }

    public static URI getHref(URI baseUri, String collectionPath, String resourcePath) {
        collectionPath = removeSlashes(collectionPath);
        resourcePath = removeSlashes(resourcePath);
        URI uri = getHref(baseUri, collectionPath);
        return getHref(uri, resourcePath);
    }

    public static URI getHref(URI baseUri, String collectionPath, UUID id) {
        return getHref(baseUri, collectionPath, id.toString());
    }

    public static String getControllerBasePath(Class<?> controllerClass) {
        if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);
            String path = Arrays.toString(requestMapping.value());
            log.debug("Class-level path: {}", path);
            return path;
        }
        return null;
    }
}
