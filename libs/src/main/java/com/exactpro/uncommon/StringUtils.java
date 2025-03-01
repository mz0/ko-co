/*
 * SPDX-FileCopyrightText:  Copyright 2025 Exactpro Systems LLC
 * SPDX-License-Identifier: Apache-2.0

 * www.exactpro.com
 * Build Software to Test Software
 */
package com.exactpro.uncommon;

import static com.exactpro.uncommon.NullAware.isNotEmpty;

public class StringUtils {

    /**
     * Abbreviates a String using ellipses. This will turn
     * "Now is the time for all good men" into "Now is the time for..."
     *
     * <p>Specifically:</p>
     * <ul>
     *   <li>If the number of characters in {@code str} is less than or equal to
     *       {@code maxWidth}, return {@code str}.</li>
     *   <li>Else abbreviate it to {@code (substring(str, 0, max-3) + "...")}.</li>
     *   <li>If {@code maxWidth} is less than {@code 4}, throw an
     *       {@link IllegalArgumentException}.</li>
     *   <li>In no case will it return a String of length greater than
     *       {@code maxWidth}.</li>
     * </ul>
     *
     * <pre>
     * StringUtils.abbreviate(null, *)      = null
     * StringUtils.abbreviate("", 4)        = ""
     * StringUtils.abbreviate("abcdefg", 6) = "abc..."
     * StringUtils.abbreviate("abcdefg", 7) = "abcdefg"
     * StringUtils.abbreviate("abcdefg", 8) = "abcdefg"
     * StringUtils.abbreviate("abcdefg", 4) = "a..."
     * StringUtils.abbreviate("abcdefg", 3) = IllegalArgumentException
     * </pre>
     *
     * @param str  the String to check, may be null
     * @param maxWidth  maximum length of result String, must be at least 4
     * @return abbreviated String, {@code null} if null String input
     * @throws IllegalArgumentException if the width is too small
     */
    public static String abbreviate(String str, int maxWidth) {
        String abbrevMarker = "...";
        if (maxWidth < abbrevMarker.length() + 1) {
            throw new IllegalArgumentException("maxWidth=" + maxWidth + " is too small");
        }
        if (isNotEmpty(str) && str.length() > maxWidth) {
            return str.substring(0, maxWidth - abbrevMarker.length()) + abbrevMarker;
        } else {
            return str;
        }
    }

    public static int countMatches(String str, char ch) {
        if (isNotEmpty(str)) {
            int count = 0;
            for (int i = 0; i < str.length(); ++i) {
                if (ch == str.charAt(i)) {
                    ++count;
                }
            }
            return count;
        } else {
            return 0;
        }

    }

    public static String removeStart(String str, String remove) {
        if (isNotEmpty(str) && isNotEmpty(remove)) {
            return str.startsWith(remove) ? str.substring(remove.length()) : str;
        } else {
            return str;
        }
    }

    public static String removeEnd(String str, String remove) {
        if (isNotEmpty(str) && isNotEmpty(remove)) {
            return str.endsWith(remove) ? str.substring(0, str.length() - remove.length()) : str;
        } else {
            return str;
        }
    }

    public static String replace(String text, String searchString, String replacement) {
        if (isNotEmpty(text) && isNotEmpty(searchString) && replacement != null) {
            return text.replaceAll(searchString, replacement);
        } else {
            return text;
        }
    }

    public static boolean isWhitespace(CharSequence cs) {
        if (cs == null) {
            return false;
        } else {
            int sz = cs.length();
            for (int i = 0; i < sz; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }
}
