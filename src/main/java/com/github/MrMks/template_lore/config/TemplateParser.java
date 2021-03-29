package com.github.MrMks.template_lore.config;

import com.github.MrMks.template_lore.formula.FormulaAPI;
import com.github.MrMks.template_lore.papi.IPAPIParser;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateParser {

    private static IPAPIParser papiParser = ((player, str) -> str);
    public static void setPapiParser(IPAPIParser parser){
        papiParser = parser;
    }

    public static ItemStack getParsedTemplate(OfflinePlayer player, String key) {
        return getParsedTemplate(player, "default", key);
    }

    public static ItemStack getParsedTemplate(OfflinePlayer player, String group, String key){
        return getParsedTemplate(player, ConfigManager.getTemplate(group, key), Collections.emptyMap());
    }

    public static ItemStack getParsedTemplate(OfflinePlayer player, String key, Map<String, String> args){
        return getParsedTemplate(player, "default", key, args);
    }

    public static ItemStack getParsedTemplate(OfflinePlayer player, String group, String key, Map<String, String> args){
        return getParsedTemplate(player, ConfigManager.getTemplate(group, key), args);
    }

    public static ItemStack getParsedTemplate(OfflinePlayer player, TemplateFile template){
        return getParsedTemplate(player, template, Collections.emptyMap());
    }

    public static ItemStack getParsedTemplate(OfflinePlayer player, TemplateFile template, Map<String, String> args){
        if (player == null || template == null) return new ItemStack(Material.AIR);

        // l tag map, saves map from command
        HashMap<String, String> map = new HashMap<>(args == null ? Collections.emptyMap() : args);

        // parse name
        String name = parse(player, template.getName(), map).replace("&","§");

        // parse locName
        String locName = parse(player, template.getLocName(), map).replace("&", "§");

        // parse material
        String strMaterial = parse(player, template.getMaterial(), map);
        Material material = Material.matchMaterial(strMaterial);
        if (material == null) {
            try {
                int numMaterial = Integer.parseInt(strMaterial);
                material = Material.getMaterial(numMaterial);
            } catch (NumberFormatException ignore){
                material = Material.IRON_SWORD;
            }
        }

        // parse damage
        String strDamage = parse(player, template.getDamage(), map).replace("&", "§");
        short damage = 0;
        try {
            damage = Short.parseShort(strDamage);
        } catch (NumberFormatException ignored){}

        // parse lore
        List<String> list = new ArrayList<>();
        template.getLore().forEach(s -> list.add(parse(player, s, map).replace("&","§")));

        // generate ItemStack
        ItemStack stack = new ItemStack(material,1,damage);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setLore(list);
            meta.setDisplayName(name);
            meta.setLocalizedName(locName);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private static final Pattern pattern = Pattern.compile("<\\s*([slrm])\\s*:\\s*([^<>]+)\\s*>");
    private static final Pattern patternR = Pattern.compile("(-?\\d+)\\s*[,_]\\s*(-?\\d+)");
    //private static final Pattern patternA = Pattern.compile("\\d+\\s*:\\s*[^<>]");
    private static String parse(OfflinePlayer player, String str, Map<String, String> map){
        str = papiParser.parse(player, str);
        Matcher matcher;
        while ((matcher = pattern.matcher(str)).find()) {
            String type = matcher.group(1);
            String sub = matcher.group(2);
            String resultStr = null;
            switch (type) {
                case "r":
                    Matcher math = patternR.matcher(sub);
                    if (math.find()) {
                        int a = Integer.parseInt(math.group(1));
                        int b = Integer.parseInt(math.group(2));
                        int result = (int) (Math.floor((b - a + 1) * Math.random()) + a);
                        resultStr = String.valueOf(result);
                    }
                    break;
                case "s":
                    resultStr = parse(player, ConfigManager.getRandomWord(sub), map);
                    break;
                case "l":
                    if (map.containsKey(sub)) resultStr = map.get(sub);
                    else {
                        resultStr = parse(player, ConfigManager.getRandomWord(sub), map);
                        map.put(sub, resultStr);
                    }
                    break;
                case "L":
                    if (map.containsKey(sub)) resultStr = map.get(sub);
                    else {
                        resultStr = ConfigManager.getRandomWord(sub);
                        map.put(sub, resultStr);
                    }
                    resultStr = parse(player, resultStr, map);
                    break;
                case "m":
                    resultStr = FormulaAPI.cal(sub).toString();
                    break;
            }
            if (resultStr == null) resultStr = "";
            str = str.replace(matcher.group(0), resultStr);
            //str = str.replaceAll(matcher.group(0), resultStr);
        }
        return str;
    }
}
