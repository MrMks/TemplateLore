package com.github.mrmks.mc.template;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

/**
 * For easier test, parse method will write here.
 */
public class ParseUtils {
    private static final Random rg = new Random();
    public static String randomInt(String s) {
        if (s == null) return null;
        s = s.trim();
        int bi = s.indexOf(','), ei = s.lastIndexOf(',');
        if (bi != ei || bi < 1 || ei > s.length() - 2) {
            bi = s.indexOf('_');
            ei = s.lastIndexOf('_');
            if (bi != ei || bi < 1 || ei > s.length() - 2) return null;
        }
        String a = s.substring(0, bi).trim(), b = s.substring(bi + 1).trim();
        try {
            int ia = Integer.parseInt(a), ib = Integer.parseInt(b);
            if (ia == ib) return ia < 0 ? a : Integer.toString(ia);
            int t = Math.min(ia, ib);
            ib = Math.max(ia, ib); ia = t;
            int r = ia + rg.nextInt(ib - ia + 1);
            return Integer.toString(r);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String repeat(String s) {
        if (s == null) return null;
        s = s.trim();
        int bi = s.indexOf(','), ei = s.lastIndexOf(',');
        if (bi != ei || bi < 1 || ei > s.length() - 2) return null;
        String a = s.substring(0, bi), b = s.substring(bi + 1).trim();
        try {
            int repeat = Integer.parseInt(b);
            if (repeat < 0) return null;
            if (repeat == 0) return "";
            StringBuilder bd = new StringBuilder(a);
            while (--repeat > 0) bd.append(a);
            return bd.toString();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String repeatSpace(String s) {
        if (s == null) return null;
        s = s.trim();
        try {
            int r = Integer.parseInt(s);
            if (r < 0) return null;
            if (r == 0) return "";
            else {
                char[] cs = new char[r];
                Arrays.fill(cs, ' ');
                return new String(cs);
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String format(String s) {
        if (s == null) return null;
        s = s.trim();
        int bi = s.indexOf(',');
        if (bi < 1 || bi > s.length() - 2) return null;
        String a = s.substring(0, bi).trim(), b = s.substring(bi + 1).trim();
        DecimalFormat f = new DecimalFormat();
        try {
            f.applyPattern(b);
            double d = Double.parseDouble(a);
            return f.format(d);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
