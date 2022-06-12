package com.github.mrmks.mc.template;

import com.github.mrmks.mc.marcabone.nbt.*;
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
        if (material == null || material == Material.AIR) return new ItemStack(Material.AIR);
        ItemStack stack = new ItemStack(material);

        int size = temp.isDirectSize() ? temp.getSizeI() : parseInt(parse(temp.getSize(), info));
        size = Math.max(1, Math.min(size, stack.getMaxStackSize()));
        stack.setAmount(size);

        short dmg = temp.isDirectDamage() ? temp.getDamageI() : parseShort(parse(temp.getDamage(), info));
        if (dmg < 0) dmg = 0;
        stack.setDurability(dmg);

        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
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
        }

        if (temp.isSetNbt()) {
            NBTItem item = new NBTItem(stack);
            if (item.isModifiable()) {
                TagCompound tag = item.getTag();
                ConfigurationSection nbts = temp.getNbtSection();
                parseNbt(tag, nbts, info);
                item.setTag(tag);
                stack = item.getItem();
            }
        }
        return stack;
    }

    private static String parsePrefix(String str) {
        if (str == null) return null;
        if (str.startsWith("n||"))
            return "n|" + str.substring(3);
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
                    .put("m", TemplateParser::parse_m)
                    .put("p", (s, i) -> papiParser.parse(i.player, "%"+s+"%"));
            meMap = bd.build();
        }
        ITokenProvider pv = new ITokenProvider() {
            @Override
            public boolean has(String tk) {
                return meMap.containsKey(tk);
            }

            @Override
            public String parse(String tk, String val) {
                return meMap.get(tk).parse(val, info);
            }
        };
        return ParseUtils.parse(str, pv);
    }

    private static void parseNbt(TagCompound base, ConfigurationSection sec, ParseInfo info) {
        Map<String, Object> secMap = sec.getValues(false);
        parseNbt(base, secMap, info);
    }

    private static void parseNbt(TagCompound base, Map<String, Object> secMap, ParseInfo info) {
        for (Map.Entry<String, Object> se : secMap.entrySet()) {
            String src_k = se.getKey();
            Object ov = se.getValue();
            String k = parse(src_k, info);

            if (k.isEmpty()) continue;
            if (k.length() == 2 && k.charAt(0) == '|') continue;

            boolean useType = false;
            char tk = 0;
            // test if there is a type token
            if (k.length() > 2) {
                char t = k.charAt(k.length() - 2);
                if (t == '|') {
                    tk = k.charAt(k.length() - 1);
                    if (testToken(tk)) {
                        k = k.substring(0, k.length() - 2);
                        useType = true;
                    } else {
                        continue;
                    }
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
                } else if (ov instanceof Map<?,?>) {
                    try {
                        if (base.hasKey(k)) {
                            TagBase ex_tag = base.getTag(k);
                            if (ex_tag instanceof TagCompound) {
                                //noinspection unchecked
                                parseNbt((TagCompound) ex_tag, (Map<String, Object>) ov, info);
                            }
                            tag = ex_tag;
                        } else {
                            TagCompound stag = new TagCompound();
                            //noinspection unchecked
                            parseNbt(stag, (Map<String, Object>) ov, info);
                            tag = stag;
                        }
                    } catch (Throwable ignored) {}
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
            return sv == null ? null : new TagString(sv);
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
                if (ov instanceof byte[]) return new TagByteArray((byte[]) ov);
                byte[] cv = new byte[v.length];
                for (int i = 0; i < cv.length; i++) cv[i] = v[i].byteValue();
                return new TagByteArray(cv);
            } else if (tk == 'I') {
                if (ov instanceof int[]) return new TagIntArray((int[]) ov);
                int[] cv = new int[v.length];
                for (int i = 0; i < cv.length; i++) cv[i] = v[i].intValue();
                return new TagIntArray(cv);
            } else {
                if (ov instanceof long[]) return new TagLongArray((long[]) ov);
                long[] cv = new long[v.length];
                for (int i = 0; i < cv.length; i++) cv[i] = v[i].longValue();
                return new TagLongArray(cv);
            }
        } else if (tk == 'a') {
            TagList list = new TagList();
            if (ov instanceof List<?>) {
                List<?> lo = new ArrayList<>((List<?>) ov);
                if (lo.size() > 1) {
                    String sf = lo.remove(0).toString();
                    if (sf.length() == 1) {
                        char stk = sf.charAt(0);
                        if (testToken(stk)) {
                            NBTType ltag = null;
                            for (Object sov : lo) {
                                TagBase tag = parseTokenNbt(stk, sov, info);
                                if (tag == null) continue;
                                if (ltag == null) {
                                    ltag = tag.getType();
                                    list.addTag(tag);
                                } else if (ltag == tag.getType()) {
                                    list.addTag(tag);
                                }
                            }
                        } else if (stk == 'c') {
                            for (Object sov : lo) {
                                try {
                                    TagCompound cmp = new TagCompound();
                                    //noinspection unchecked
                                    parseNbt(cmp, (Map<String, Object>) sov, info);
                                    list.addTag(cmp);
                                } catch (Throwable ignored) {}
                            }
                        }
                    }
                }
            }
            return list;
        }
        return null;
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
            return (short) Math.round(Float.parseFloat(str));
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
        Number nm = FormulaAPI.mathCal(s);
        return nm == null ? null : nm.toString();
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
