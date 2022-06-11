package com.github.mrmks.mc.template;

import com.github.mrmks.mc.marcabone.nbt.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class TemplateDumper {
    public static YamlConfiguration dumpItemStack(ItemStack stack, String name) {
        YamlConfiguration cfg = new YamlConfiguration();

        String tmpName = name != null && !name.isEmpty() ? name :
                        stack.getType().name().toLowerCase(Locale.ENGLISH) + "_" + System.currentTimeMillis();

        ConfigurationSection cs = cfg.createSection(tmpName);

        cs.set("id", stack.getType().name());
        cs.set("damage", stack.getDurability());
        cs.set("size", stack.getAmount());
        cs.set("group", "dump");

        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) cs.set("name", prefixStr(meta.getDisplayName()));
            if (meta.hasLocalizedName()) cs.set("locName", prefixStr(meta.getLocalizedName()));
            if (meta.hasLore()) cs.set("lore", meta.getLore().stream().map(TemplateDumper::prefixStr).collect(Collectors.toList()));
        }

        NBTItem ni = new NBTItem(stack);
        if (ni.isModifiable() && ni.hasTag()) {
            readCmp(cs.createSection("nbt"), ni.getTag());
        }

        return cfg;
    }

    private static void readCmp(ConfigurationSection cs, TagCompound cmp) {
        Set<String> ks = cmp.keySet();
        for (String k : ks) {
            TagBase tb = cmp.getTag(k);
            NBTType type = tb.getType();
            if (type == NBTType.COMPOUND) {
                if (k.equals("display")) continue;
                readCmp(cs.createSection(k), (TagCompound) tb);
            } else if (type != NBTType.END) {
                Object obj = getVal(tb);
                if (obj != null) cs.set(k + '|' + getToken(type), obj);
            }
        }
    }

    private static Map<String, Object> readCmp(TagCompound cmp) {
        Map<String, Object> map = new HashMap<>();
        Set<String> ks = cmp.keySet();
        for (String k : ks) {
            TagBase tb = cmp.getTag(k);
            NBTType type = tb.getType();
            if (type == NBTType.COMPOUND) {
                map.put(k, readCmp((TagCompound) tb));
            } else if (type != NBTType.END) {
                Object obj = getVal(tb);
                if (obj != null) map.put(k + '|' + getToken(type), obj);
            }
        }
        return map;
    }


    private static char getToken(NBTType type) {
        switch (type) {
            case BYTE: return 'b';
            case BYTE_ARRAY: return 'B';
            case SHORT: return 's';
            case INT: return 'i';
            case INT_ARRAY: return 'I';
            case LONG: return 'l';
            case LONG_ARRAY: return 'L';
            case FLOAT: return 'f';
            case DOUBLE: return 'd';
            case STRING: return 'S';
            case LIST: return 'a';
        }
        return '\0';
    }

    private static Object getVal(TagBase base) {
        switch (base.getType()) {
            case BYTE: return ((TagByte) base).getData();
            case BYTE_ARRAY: return ((TagByteArray) base).getData();
            case SHORT: return ((TagShort) base).getData();
            case INT: return ((TagInt) base).getData();
            case INT_ARRAY: return ((TagIntArray) base).getData();
            case LONG: return ((TagLong) base).getData();
            case LONG_ARRAY: return ((TagLongArray) base).getData();
            case FLOAT: return ((TagFloat) base).getData();
            case DOUBLE: return ((TagDouble) base).getData();
            case STRING: return prefixStr(((TagString) base).getData());

        }
        if (base.getType() == NBTType.LIST) {
            TagList tl = (TagList) base;
            List<Object> list = new LinkedList<>();
            int sz = tl.size();
            for (int i = 0; i < sz; i++) {
                TagBase tb = tl.getTag(i);
                if (tb.getType() == NBTType.END) break;
                else {
                    if (list.isEmpty()) {
                        list.add(tb.getType() == NBTType.COMPOUND ? 'c' : getToken(tb.getType()));
                    }
                    list.add(getVal(tb));
                }
            }
            return list;
        } else if (base.getType() == NBTType.COMPOUND) {
            return readCmp((TagCompound) base);
        }

        return null;
    }

    private static String prefixStr(String src) {
        if (src.startsWith("n|")) return "n||" + src.substring(2);
        else return src;
    }
}
