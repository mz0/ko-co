/*
 * SPDX-FileCopyrightText:  Copyright 2024 Exactpro Systems LLC
 * SPDX-License-Identifier: Apache-2.0

 * www.exactpro.com
 * Build Software to Test Software
 */
package com.exactpro.uncommon;

import java.util.Collection;
import java.util.Map;

public class NullAware {
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isNotEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    public static boolean isNullOrEmpty(byte[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNotEmpty(byte[] array) {
        return !isNullOrEmpty(array);
    }

    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isNullOrEmpty(collection);
    }

    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean contains(String str, int searchChar) {
        if (isNullOrEmpty(str)) {
            return false;
        } else {
            return str.indexOf(searchChar) >= 0;
        }
    }

    public static <T> T defaultIfNull(T object, T defaultValue) {
        return (T)(object != null ? object : defaultValue);
    }
}
