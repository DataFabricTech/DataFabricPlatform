package com.mobigen.datafabric.core.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
public class DynamicJarLoader {
    private String jarPath;
    private Map<String, URLClassLoader> loaderMap = new HashMap<>();

    public DynamicJarLoader(String jarPath) {
        this.jarPath = jarPath; // linux path, no window
        if (!this.jarPath.endsWith("/")) {
            this.jarPath = this.jarPath + "/";
        }
    }

    public boolean load(String jarFileName) {
        if (loaderMap.containsKey(jarFileName)) {
            log.info("unload driver for " + jarFileName);
            unload(jarFileName);
        }
        var jarFilePath = jarPath + jarFileName;
        var jarFile = new File(jarFilePath);
        try {
            log.info("load classloader for " + jarFile);
            var classURL = new URL(jarFile.toString());
            var classLoader = new URLClassLoader(new URL[]{classURL});
            loaderMap.put(jarFileName, classLoader);
            log.info("success to load classloader for " + jarFile);
            return true;
        } catch (MalformedURLException e) {
            log.error("fail to load classloader for " + jarFile, e);
            return false;
        }
    }

    public boolean unload(String jarFileName) {
        var loader = loaderMap.get(jarFileName);
        if (loader == null) {
            return true;
        }
        try {
            loader.close();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            loaderMap.remove(jarFileName);
        }
    }

    public Object newInstance(String jarFileName, String className) {
        var loader = loaderMap.get(jarFileName);
        if (loader == null) {
            return null;
        }
        try {
            log.info("load driver for " + jarFileName);
            var clazz = loader.loadClass(className);
            log.info("success to load driver for " + jarFileName);
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            log.error("fail to load driver for " + jarFileName, e);
            return null;
        }
    }

    public Object newInstance(String className) {
        for (String each : loaderMap.keySet()) {
            var object = newInstance(each, className);
            if (object != null) {
                return object;
            }
        }
        return null;
    }
}
