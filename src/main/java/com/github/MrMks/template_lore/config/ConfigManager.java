package com.github.MrMks.template_lore.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class ConfigManager {
    private static final Map<String, List<String>> wordStock = new HashMap<>();
    private static final Map<String, Map<String,TemplateFile>> templateMap = new HashMap<>();
    public static void init(Plugin plugin){
        readWordStock(plugin);
        readTemplate(plugin);
    }

    public static void cleanup(){
        wordStock.clear();
        templateMap.clear();
    }

    public static TemplateFile getTemplate(String key) {
        return getTemplate("default", key);
    }

    public static TemplateFile getTemplate(String group, String key){
        return templateMap.getOrDefault(group,Collections.emptyMap()).get(key);
    }

    public static String getRandomWord(String key){
        List<String> list = wordStock.get(key);
        if (list != null && !list.isEmpty()) {
            int index = (int) Math.floor(Math.random() * list.size());
            return list.get(index);
        } else return "";
    }

    public static List<String> getAllGroups(){
        return new ArrayList<>(templateMap.keySet());
    }

    public static List<String> getAllTemplates(String group){
        return new ArrayList<>(templateMap.getOrDefault(group, Collections.emptyMap()).keySet());
    }

    private static void readWordStock(Plugin plugin){
        List<FileConfiguration> list = new ArrayList<>();
        File file = new File(plugin.getDataFolder(), "wordStock");
        if (!file.exists()) file.mkdirs();
        getConfigList(file, plugin.getLogger(), list);
        for (FileConfiguration cfg : list) {
            wordStock.putAll(readWordStock(cfg, plugin.getLogger()));
        }
    }

    public static HashMap<String, List<String>> readWordStock(FileConfiguration cfg, Logger logger) {
        HashMap<String, List<String>> map = new HashMap<>();
        Set<String> keySet = cfg.getKeys(false);
        for (String key : keySet) {
            try {
                map.put(key, cfg.getStringList(key));
            } catch (Throwable tr) {
                logger.warning("Error to parse word stock: " + key);
                tr.printStackTrace();
            }
        }
        return map;
    }

    private static void readTemplate(Plugin plugin){
        List<FileConfiguration> list = new ArrayList<>();
        File file = new File(plugin.getDataFolder(), "template");
        if (!file.exists()) file.mkdirs();
        getConfigList(file, plugin.getLogger(), list);
        for (FileConfiguration cfg : list) {
            HashMap<String, HashMap<String, TemplateFile>> mapHashMap = readTemplate(cfg, plugin.getLogger());
            for (Map.Entry<String, HashMap<String, TemplateFile>> entry : mapHashMap.entrySet()) {
                templateMap.putIfAbsent(entry.getKey(), new HashMap<>());
                templateMap.get(entry.getKey()).putAll(entry.getValue());
            }
        }
    }

    private static HashMap<String, HashMap<String, TemplateFile>> readTemplate(FileConfiguration cfg, Logger logger) {
        HashMap<String, HashMap<String,TemplateFile>> map = new HashMap<>();
        Set<String> keySet = cfg.getKeys(false);
        for (String key : keySet) {
            try {
                ConfigurationSection subCfg = cfg.getConfigurationSection(key);
                String name = subCfg.getString("name", "");
                String locName = subCfg.getString("locName", "");
                String strMaterial = subCfg.getString("id");
                String damage = subCfg.getString("damage");
                List<String> lore = subCfg.getStringList("lore");
                String group = subCfg.getString("group", "default");
                if (!map.containsKey(group)) map.put(group, new HashMap<>());
                map.get(group).put(key, new TemplateFile(strMaterial, damage, name, locName, lore));
            } catch (Throwable tr) {
                logger.warning("Error while parsing template: " + key);
                tr.printStackTrace();
            }
        }
        return map;
    }

    private static void getConfigList(File file, Logger logger, List<FileConfiguration> list) {
        if (file == null) return;
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File subFile : files) {
                        getConfigList(subFile, logger, list);
                    }
                }
            } else if (file.isFile()){
                list.add(YamlConfiguration.loadConfiguration(file));
            }
        }
    }

    public static boolean hasWord(String key) {
        return wordStock.containsKey(key);
    }

    public static boolean hasTemplate(String key) {
        return hasTemplate("default", key);
    }

    public static boolean hasTemplate(String group, String key) {
        return templateMap.containsKey(key);
    }

    public static void addWordStock(String key, List<String> words) {
        wordStock.putIfAbsent(key, words);
    }

    public static void addTemplate(String key, TemplateFile file) {
        addTemplate("default", key, file);
    }

    public static void addTemplate(String group, String key, TemplateFile file) {
        templateMap.putIfAbsent(group, new HashMap<>());
        templateMap.get(group).putIfAbsent(key, file);
    }
}
