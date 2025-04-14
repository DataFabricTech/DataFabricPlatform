package com.mobigen.monitoring.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StringUtil {
	public static boolean isEmpty(String str) {
        if (str == null)  return true;
        if (str.isEmpty())  return true;
        return false;
    }
}
