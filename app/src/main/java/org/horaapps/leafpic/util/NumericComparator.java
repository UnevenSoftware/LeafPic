/*
   Copyright (C) 1995 Ian Jackson <iwj10@cus.cam.ac.uk>
   Copyright (C) 2001 Anthony Towns <aj@azure.humbug.org.au>
   Copyright (C) 2008-2014 Free Software Foundation, Inc.

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package org.horaapps.leafpic.util;

/**
 * Copied from GNU coreutils-8.23/lib/filevercmp.c
 *  Java implementation is highly inefficient, because it copies strings symbol by symbol.
 */
public class NumericComparator {

    public static final String TAG = "NumericComparator";

    /**
     * Compare version strings:
     *  This function compares strings S1 and S2:
     *  1) By PREFIX in the same way as strcmp.
     *  2) Then by VERSION (most similarly to version compare of Debian's dpkg).
     *  Leading zeros in version numbers are ignored.
     *  3) If both (PREFIX and  VERSION) are equal, strcmp function is used for
     *  comparison. So this function can return 0 if (and only if) strings S1
     *  and S2 are identical.
     *  It returns number >0 for S1 > S2, 0 for S1 == S2 and number <0 for S1 < S2.
     *  This function compares strings, in a way that if VER1 and VER2 are version
     *  numbers and PREFIX and SUFFIX (SUFFIX defined as (\.[A-Za-z~][A-Za-z0-9~]*)*)
     *  are strings then VER1 < VER2 implies filevercmp (PREFIX VER1 SUFFIX,
     *  PREFIX VER2 SUFFIX) < 0.
     *  This function is intended to be a replacement for strverscmp.
     */
    public static int filevercmp(String s1, String s2) {
        String s1_suffix, s2_suffix;
        int s1_len, s2_len, result;
        /**
         * easy comparison to see if strings are identical
         */
        int simple_cmp = strcmp(s1, s2);
        if (simple_cmp == 0)
            return 0;
        /**
         * special handle for "", "." and ".."
         */
        if (s1 == null || s1.length() == 0)
            return -1;
        if (s2 == null || s2.length() == 0)
            return 1;
        if (0 == strcmp(".", s1))
            return -1;
        if (0 == strcmp(".", s2))
            return 1;
        if (0 == strcmp("..", s1))
            return -1;
        if (0 == strcmp("..", s2))
            return 1;
        /**
         * special handle for other hidden files
         */
        if (s1.codePointAt(0) == '.' && s2.codePointAt(0) != '.')
            return -1;
        if (s1.codePointAt(0) != '.' && s2.codePointAt(0) == '.')
            return 1;
        if (s1.codePointAt(0) == '.' && s2.codePointAt(0) == '.') {
            s1 = s1.substring(1, s1.length());
            s2 = s2.substring(1, s2.length());
        }
        /**
         * "cut" file suffixes
         */
        s1_suffix = match_suffix(s1);
        s2_suffix = match_suffix(s2);
        s1_len = s1.length() - s1_suffix.length();
        s2_len = s2.length() - s2_suffix.length();
        /**
         * restore file suffixes if strings are identical after "cut"
         */
        if ((s1_suffix.length() > 0 || s2_suffix.length() > 0) && (s1_len == s2_len) && 0 == strncmp(s1, s2, s1_len)) {
            s1_len = s1.length();
            s2_len = s2.length();
        }
        result = verrevcmp(s1.substring(0, s1_len), s2.substring(0, s2_len));
        return result == 0 ? simple_cmp : result;
    }

    /**
     * slightly modified verrevcmp function from dpkg
     *  S1, S2 - compared string
     *  S1_LEN, S2_LEN - length of strings to be scanned
     *  This implements the algorithm for comparison of version strings
     *  specified by Debian and now widely adopted.  The detailed
     *  specification can be found in the Debian Policy Manual in the
     *  section on the 'Version' control field.  This version of the code
     *  implements that from s5.6.12 of Debian Policy v3.8.0.1
     *  http://www.debian.org/doc/debian-policy/ch-controlfields.html#s-f-Version
     */
    public static int verrevcmp(String s1, String s2) {
        int s1_pos = 0;
        int s2_pos = 0;
        while (s1_pos < s1.length() || s2_pos < s2.length()) {
            int first_diff = 0;
            while ((s1_pos < s1.length() && !c_isdigit(s1.codePointAt(s1_pos))) || (s2_pos < s2.length() && !c_isdigit(s2.codePointAt(s2_pos)))) {
                int s1_c = (s1_pos >= s1.length()) ? 0 : order(s1.codePointAt(s1_pos));
                int s2_c = (s2_pos >= s2.length()) ? 0 : order(s2.codePointAt(s2_pos));
                if (s1_c != s2_c)
                    return s1_c - s2_c;
                s1_pos++;
                s2_pos++;
            }
            while (s1_pos < s1.length() && s1.codePointAt(s1_pos) == '0') s1_pos++;
            while (s2_pos < s2.length() && s2.codePointAt(s2_pos) == '0') s2_pos++;
            while (s1_pos < s1.length() && s2_pos < s2.length() && c_isdigit(s1.codePointAt(s1_pos)) && c_isdigit(s2.codePointAt(s2_pos))) {
                if (first_diff == 0)
                    first_diff = s1.codePointAt(s1_pos) - s2.codePointAt(s2_pos);
                s1_pos++;
                s2_pos++;
            }
            if (s1_pos < s1.length() && c_isdigit(s1.codePointAt(s1_pos)))
                return 1;
            if (s2_pos < s2.length() && c_isdigit(s2.codePointAt(s2_pos)))
                return -1;
            if (first_diff != 0)
                return first_diff;
        }
        return 0;
    }

    /**
     * The biggest Unicode character value we can have: http://unicode.org/faq/utf_bom.html#gen6
     */
    private static final int UNICODE_MAX = 0x10FFFF;

    /**
     * verrevcmp helper function
     */
    private static int order(int c) {
        if (c_isdigit(c))
            return 0;
        else if (c_isalpha(c))
            return c;
        else if (c == '~')
            return -1;
        else
            return (int) c + UNICODE_MAX + 1;
    }

    /**
     * Match a file suffix defined by this regular expression:
     *  /(\.[A-Za-z~][A-Za-z0-9~]*)*$/
     *  Returns empty string if not found.
     */
    private static String match_suffix(String str) {
        String match = "";
        boolean read_alpha = false;
        while (str.length() > 0) {
            if (read_alpha) {
                read_alpha = false;
                if (!c_isalpha(str.codePointAt(0)) && '~' != str.codePointAt(0))
                    match = "";
            } else if ('.' == str.codePointAt(0)) {
                read_alpha = true;
                if (match.length() == 0)
                    match = str;
            } else if (!c_isalnum(str.codePointAt(0)) && '~' != str.codePointAt(0)) {
                match = "";
            }
            str = str.substring(1, str.length());
        }
        return match;
    }

    /**
     * The strcmp() function compares the two strings s1 and s2.
     *  It returns an integer less than, equal to, or greater than zero if s1 is found,
     *  respectively, to be less than, to match, or be greater than s2.
     */
    private static int strcmp(final String s1, final String s2) {
        return s1.compareTo(s2);
    }

    /**
     * The strncmp() function is similar to strcmp(), except it compares the only first (at most) n bytes of s1 and s2.
     */
    private static int strncmp(final String s1, final String s2, int len) {
        int len1 = Math.min(len, s1.length());
        int len2 = Math.min(len, s2.length());
        return s1.substring(0, len1).compareTo(s2.substring(0, len2));
    }

    private static boolean c_isdigit(int c) {
        return Character.isDigit(c);
    }

    private static boolean c_isalpha(int c) {
        return Character.isLetter(c);
    }

    private static boolean c_isalnum(int c) {
        return Character.isLetterOrDigit(c);
    }
}
