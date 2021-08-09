package com.github.mrmks.mc.template;

import com.github.mrmks.mc.dev_tools_b.nbt.*;
import com.github.mrmks.mc.template.config.ConfigManager;
import com.github.mrmks.mc.template.papi.IPAPIParser;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class TemplateParser {

    private static IPAPIParser papiParser = ((player, str) -> str);
    private static final IPAPIParser defParser = papiParser;
    public static void setPapiParser(IPAPIParser parser){
        papiParser  = parser == null ? defParser : parser;
    }

    public static ItemStack parseTemplate(OfflinePlayer player, TemplateFile temp, ConfigManager cfg, Map<String, String> args) {
        if (player == null || temp == null) return new ItemStack(Material.AIR);

        ParseInfo info = new ParseInfo();
        info.player = player;
        info.cfg = cfg;
        info.map = args;

        Material material = Material.matchMaterial(parse(temp.getMaterial(), info));
        if (material == null) return new ItemStack(Material.AIR);
        ItemStack stack = new ItemStack(material);

        int size = temp.isDirectSize() ? temp.getSizeI() : parseInt(parse(temp.getSize(), info));
        size = Math.max(1, Math.min(size, stack.getMaxStackSize()));
        stack.setAmount(size);

        short dmg = temp.isDirectDamage() ? temp.getDamageI() : parseShort(parse(temp.getDamage(), info));
        dmg = (short) Math.max(0, dmg);
        stack.setDurability(dmg);

        ItemMeta meta = stack.getItemMeta();
        String tmp;
        if (temp.isSetName()) {
            String str = parsePrefix(parse(temp.getName(), info));
            if (str != null) meta.setDisplayName(str);
        }

        if (temp.isSetLocName()) {
            if ((tmp = parsePrefix(parse(temp.getLocName(), info))) != null)
                meta.setLocalizedName(tmp);
        }

        if (temp.isSetLore()) {
            List<String> list = new ArrayList<>(temp.getLore().size());
            for (String line : temp.getLore()) {
                tmp = parsePrefix(parse(line, info));
                if (tmp != null) list.add(tmp);
            }
            meta.setLore(list);
        }
        stack.setItemMeta(meta);

        if (temp.isSetNbt()) {
            NBTItem item = new NBTItem(stack);
            if (item.isModifiable()) {
                TagCompound tag = item.getTag();
                ConfigurationSection nbts = temp.getNbtSection();
                parseNbt(tag, nbts, info);
            }
        }
        return stack;
    }

    private static String parsePrefix(String str) {
        if (str == null) return null;
        if (str.startsWith("n||"))
            return "n|" + str.substring(2);
        if (str.startsWith("n|")) return null;
        return str;
    }

    private static Map<String, IParser> meMap;
    private static String parse(String str, ParseInfo info) {
        if (meMap == null) {
            ImmutableMap.Builder<String, IParser> bd = ImmutableMap.builder();
            bd.put("s", TemplateParser::parse_s)
                    .put("l", TemplateParser::parse_l)
                    .put("L", TemplateParser::parse_L)
                    .put("c", TemplateParser::parse_c)
                    .put("C", TemplateParser::parse_C)
                    .put("r", TemplateParser::parse_r)
                    .put("f", TemplateParser::parse_f)
                    .put("m", TemplateParser::parse_m);
            meMap = bd.build();
        }
        str = papiParser.parse(info.getPlayer(), str);
        StringBuilder bd = new StringBuilder(str);

        int i = -1, v = -1, lv, d = 0;
        // ':' is 58, '<' is 60, '>' is 62, '\' is 92;
        int lc = '<', mc = ':', rc = '>';
        int sl = '\\';
        Stack<Integer> bs = new Stack<>(), ms = new Stack<>();
        PrimitiveIterator.OfInt it = str.codePoints().iterator();

        while (it.hasNext()) {
            lv = v;
            v = it.next();
            i += v < Character.MIN_SUPPLEMENTARY_CODE_POINT ? 1 : 2;
            if (v < mc || v > rc) continue;

            if (v == lc) {
                if (lv != sl) bs.push(i - d);
            } else {
                if (v == mc) {
                    if (ms.size() < bs.size()) ms.push(i - d);
                } else if (v == rc) {
                    if (ms.size() > 0) {
                        int bi = bs.pop(), mi = ms.pop(), ei = i - d;
                        if (bi < mi - 1 && mi < ei - 1) {
                            String tk = str.substring(bi + 1, mi).trim();
                            IParser parser = meMap.get(tk);
                            if (parser != null) {
                                String rst = parser.parse(str.substring(mi + 1, ei).trim(), info);
                                if (rst != null) {
                                    bd.replace(bi, ei + 1, rst);
                                    d += rst.length() - (ei - bi + 1);
                                }
                            }
                        }
                    }
                }
            }
        }
        return bd.toString();
    }

    private static void parseNbt(TagCompound base, ConfigurationSection sec, ParseInfo info) {
        Map<String, Object> secMap = sec.getValues(false);
        for (Map.Entry<String, Object> se : secMap.entrySet()) {
            String src_k = se.getKey();
            Object ov = se.getValue();
            String k = parse(src_k, info);

            if (k.isEmpty()) continue;
            if (k.length() == 2 && k.charAt(0) == '|') continue;

            boolean useType = false;
            char tk = 0;
            // test if there is an type token
            if (k.length() > 2) {
                char t = k.charAt(k.length() - 2), et = k.charAt(k.length() - 3);
                if (t == '|') {
                    tk = k.charAt(k.length() - 1);
                    k = k.substring(0, k.length() - 2);
                    if (et == '|') {
                        k = k + tk;
                    } else if (!testToken(tk)) {
                        k = src_k;
                    } else useType = true;
                }
            }

            TagBase tag = null;
            if (useType) {
                if (!base.hasKey(k)) tag = parseTokenNbt(tk, ov, info);
            }
            else {
                if (ov instanceof ConfigurationSection) {
                    if (base.hasKey(k)) {
                        TagBase ex_tag = base.getTag(k);
                        if (ex_tag instanceof TagCompound) {
                            parseNbt((TagCompound) ex_tag, (ConfigurationSection) ov, info);
                        }
                        tag = ex_tag;
                    } else {
                        TagCompound stag = new TagCompound();
                        parseNbt(stag, (ConfigurationSection) ov, info);
                        tag = stag;
                    }
                } else {
                    if (!base.hasKey(k)) tag = parseNonTokenNbt(ov, info);
                }
            }
            if (tag == null) continue;
            base.setTag(k, tag);
        }
    }

    private static boolean testToken(char tk) {
        return tk == 'b' || tk == 's' || tk == 'i' || tk == 'l' || tk == 'f' || tk == 'd' || tk == 'B' || tk == 'I'
                || tk == 'L' || tk == 'S' || tk == 'a';
    }

    private static TagBase parseTokenNbt(char tk, Object ov, ParseInfo info) {
        if (ov == null) return null;
        if (!testToken(tk)) return null;

        if (tk == 'b') {
            return new TagByte(parseNbtNum(ov, info).byteValue());
        } else if (tk == 's') {
            return new TagShort(parseNbtNum(ov, info).shortValue());
        } else if (tk == 'i') {
            return new TagInt(parseNbtNum(ov, info).intValue());
        } else if (tk == 'l') {
            return new TagLong(parseNbtNum(ov, info).longValue());
        } else if (tk == 'f') {
            return new TagFloat(parseNbtNum(ov, info).floatValue());
        } else if (tk == 'd') {
            return new TagDouble(parseNbtNum(ov, info).doubleValue());
        } else if (tk == 'S') {
            String sv = ov.toString();
            if (ov instanceof String) {
                sv = parsePrefix(parse(sv, info));
            }
            if (sv != null) return new TagString(sv);
            else return null;
        } else if (tk == 'B' || tk == 'I' || tk == 'L') {
            Number[] v;
            if (ov instanceof List<?>) {
                List<?> lo = (List<?>) ov;
                v = new Number[lo.size()];
                int i = 0;
                for (Object sov : lo) {
                    Number num = parseNbtNum(sov, info);
                    v[i++] = num;
                }
            } else v = new Number[]{parseNbtNum(ov, info)};
            if (tk == 'B') {
                byte[] cv = new byte[v.length];
                for (int i = 0; i < cv.length; i++) cv[i] = v[i].byteValue();
                return new TagByteArray(cv);
            } else if (tk == 'I') {
                int[] cv = new int[v.length];
                for (int i = 0; i < cv.length; i++) cv[i] = v[i].intValue();
                return new TagIntArray(cv);
            } else {
                long[] cv = new long[v.length];
                for (int i = 0; i < cv.length; i++) cv[i] = v[i].longValue();
                return new TagLongArray(cv);
            }
        } else if (tk == 'a') {
            TagList list = new TagList();
            if (ov instanceof List<?>) {
                List<?> lo = (List<?>) ov;
                if (lo.size() > 1) {
                    String sf = lo.remove(0).toString();
                    if (sf.length() == 1) {
                        char stk = sf.charAt(0);
                        if (testToken(stk)) {
                            EnumTagType ltag = null;
                            for (Object sov : lo) {
                                TagBase tag = parseTokenNbt(stk, sov, info);
                                if (tag == null) continue;
                                if (ltag == null) {
                                    ltag = tag.getType();
                                } else if (ltag == tag.getType()) {
                                    list.addTag(tag);
                                }
                            }
                        }
                    }
                }
            }
            return list;
        }
        return null;
    }

    private static TagBase parseNonTokenNbt(Object ov, ParseInfo info) {
        if (ov == null) return null;
        if (ov instanceof Number) {
            return new TagDouble(((Number) ov).doubleValue());
        } else if (ov instanceof String || ov instanceof Character) {
            String sv = ov.toString();
            sv = parsePrefix(parse(sv, info));
            if (sv == null) {
                return null;
            } else {
                try {
                    return new TagDouble(Double.parseDouble(sv));
                } catch (NumberFormatException e) {
                    return new TagString(sv);
                }
            }
        } else if (ov instanceof List<?>) {
            List<?> lo = (List<?>) ov;
            if (lo.isEmpty()) return new TagList();
            else {
                boolean isNum;
                long tv;
                Object sov = lo.get(0);
                if (sov instanceof Number) {
                    isNum = true;
                    tv = ((Number) sov).longValue();
                } else if (sov instanceof String || sov instanceof Character) {
                    String sv = sov.toString();
                    sv = parsePrefix(parse(sv, info));
                    if (sv != null) {
                        char tk;
                        if (sv.length() == 1 && testToken(tk = sv.charAt(0))) {
                            lo.remove(0);
                            EnumTagType ltp = null;
                            TagList trt = new TagList();
                            for (Object ssov : lo) {
                                TagBase tg = parseTokenNbt(tk, ssov, info);
                                if (tg == null) continue;
                                if (ltp == null) {
                                    ltp = tg.getType();
                                    trt.addTag(tg);
                                } else if (ltp == tg.getType()) {
                                    trt.addTag(tg);
                                }
                            }
                            return trt;
                        } else {
                            try {
                                tv = Long.parseLong(sv);
                                isNum = true;
                            } catch (NumberFormatException e) {
                                tv = 0;
                                isNum = false;
                            }
                        }
                    } else return null;
                } else return null;
                if (isNum) {
                    long[] v = new long[lo.size()];
                    v[0] = tv;
                    Iterator<?> it = lo.iterator();
                    it.next();
                    int i = 1;
                    while (it.hasNext()) {
                        v[i++] = parseNbtNum(it.next(), info).longValue();
                    }
                    return new TagLongArray(v);
                } else return null;
            }
        } else return null;
    }

    private static Number parseNbtNum(Object ov, ParseInfo info) {
        if (ov != null) {
            if (ov instanceof Character || ov instanceof String) {
                String sv = parsePrefix(parse(ov.toString(), info));
                if (sv != null) {
                    try {
                        return Double.parseDouble(sv);
                    } catch (NumberFormatException ignored) {}
                }
            } else if (ov instanceof Number) {
                return (Number) ov;
            }
        }
        return 0;
    }

    private static short parseShort(String str) {
        try {
            return Short.parseShort(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static class ParseInfo {
        OfflinePlayer player;
        ConfigManager cfg;
        Map<String, String> map;

        public OfflinePlayer getPlayer() {
            return player;
        }

        String getWord(String key) {
            if (map.containsKey(key)) return map.get(key);
            else return cfg.getWord(key);
        }

        void cacheWord(String key, String word) {
            map.putIfAbsent(key, word);
        }
    }

    private interface IParser {
        String parse(String s, ParseInfo info);
    }

    private static String parse_s(String s, ParseInfo info) {
        s = info.getWord(s);
        if (s == null || s.isEmpty()) return "";
        return parse(s, info);
    }

    private static String parse_l(String s, ParseInfo info) {
        String r = info.getWord(s);
        r = r == null || r.isEmpty() ? "" : parse(r, info);
        info.cacheWord(s, r);
        return r;
    }

    private static String parse_L(String s, ParseInfo info) {
        String r = info.getWord(s);
        if (r == null) r = "";
        info.cacheWord(s, r);
        return r.isEmpty() ? r : parse(r, info);
    }

    public static String parse_r(String s, ParseInfo info) {
        return ParseUtils.randomInt(s);
    }

    public static String parse_m(String s, ParseInfo info) {
        return FormulaAPI.mathCal(s).toString();
    }

    public static String parse_c(String s, ParseInfo info) {
        return ParseUtils.repeat(s);
    }

    public static String parse_C(String s, ParseInfo info) {
        return ParseUtils.repeatSpace(s);
    }

    public static String parse_f(String s, ParseInfo info) {
        return ParseUtils.format(s);
    }
}