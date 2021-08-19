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

    public static String parse(String str, ITokenProvider pv) {
        StringBuilder bd = new StringBuilder(pv.parse(str));

        int v = -1, lv;
        // ':' is 58, '<' is 60, '>' is 62, '\' is 92;
        IntStack bs = new IntStack(), ms = new IntStack();
        for (int i = 0; i < bd.length(); i++) {
            lv = v;
            v = bd.charAt(i);
            switch (v) {
                default: break;
                case '<':
                    if (lv != '\\') bs.push(i);
                    else bd.deleteCharAt(i - 1);
                    break;
                case ':':
                    if (ms.size() < bs.size()) ms.push(i);
                    break;
                case '>':
                    if (ms.size() > 0) {
                        int bi = bs.pop(), mi = ms.pop();
                        if (bi < mi - 1 && mi < i - 1) {
                            String tk = bd.substring(bi + 1, mi).trim();
                            if (pv.has(tk)) {
                                String rst = pv.parse(tk, bd.substring(mi + 1, i).trim());
                                if (rst != null) {
                                    bd.replace(bi, i + 1, rst);
                                    i += rst.length() - (i - bi + 1);
                                }
                            }
                        }
                    }
            }
        }
        return bd.toString();
    }

    private static class IntStack {
        int ei = 0;
        int[] a = new int[8];

        int size() {
            return ei;
        }

        int pop() {
            if (ei == 0) throw new IndexOutOfBoundsException();
            return a[(ei--) - 1];
        }

        int peek() {
            if (ei == 0) throw new IndexOutOfBoundsException();
            return a[ei - 1];
        }

        void push(int i) {
            ec();
            a[ei++] = i;
        }

        private void ec() {
            if (ei >= a.length) {
                int[] t = new int[a.length * 2];
                System.arraycopy(a, 0, t, 0, a.length);
                a = t;
            }
        }
    }
}
