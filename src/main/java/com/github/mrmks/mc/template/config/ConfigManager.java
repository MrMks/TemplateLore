package com.github.mrmks.mc.template.config;

import com.github.mrmks.mc.template.TemplateFile;
import com.github.mrmks.mc.template.WordStock;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

public class ConfigManager {

    public static final String GROUP = "dft";

    private final Map<String, WordStock> words;
    private final Map<String, Map<String, TemplateFile>> templates;
    private final File folder;

    public ConfigManager(Plugin plugin) {
        this.folder = plugin.getDataFolder();
        this.words = new HashMap<>();
        this.templates = new HashMap<>();
        reload();
    }

    private void readWordStock(){
        File file = new File(folder, "wordStock");
        if (!file.exists() && !file.mkdir()) return;
        List<ConfigurationSection> list = ConfigUtils.getConfigs(file);
        for (ConfigurationSection cfg : list) {
            Set<String> ks = cfg.getKeys(false);
            for (String k : ks) {
                if (cfg.isConfigurationSection(k)) continue;
                List<String> sl = cfg.isList(k) ? cfg.getStringList(k) : Collections.singletonList(cfg.getString(k));
                if (sl.isEmpty()) continue;
                WordStock ws = new WordStock();
                for (String s : sl) {
                    if (s == null) continue;
                    if (s.length() < 2) {
                        ws.addWord(1, s);
                    } else {
                        int ti1 = s.indexOf('|');
                        if (ti1 <= 0 || ti1 > s.length() - 1) {
                            ws.addWord(1, s);
                        } else {
                            int ti2 = ti1 == s.length() - 1 ? -1 : s.indexOf('|', ti1 + 1);
                            if (ti2 < 0 || ti2 > ti1 + 1) {
                                String ns = s.substring(0, ti1);
                                try {
                                    int w = Integer.parseInt(ns);
                                    ws.addWord(w, s.substring(ti2 + 1));
                                } catch (NumberFormatException e) {
                                    ws.addWord(1, s);
                                }
                            } else {
                                ws.addWord(1, s.substring(0, ti1) + s.substring(ti2));
                            }
                        }
                    }
                }
                words.put(k, ws);
            }
        }
    }

    private void readTemplate(){
        File file = new File(folder, "template");
        if (!file.exists() && !file.mkdir()) return;
        List<ConfigurationSection> list = ConfigUtils.getConfigs(file);
        for (ConfigurationSection cfg : list) {
            Set<String> ks = cfg.getKeys(false);
            for (String k : ks) {
                if (!cfg.isConfigurationSection(k)) continue;
                ConfigurationSection scfg = cfg.getConfigurationSection(k);
                String group = scfg.getString("group", "");
                if (group.isEmpty()) group = GROUP;
                TemplateFile tf = new TemplateFile(scfg);
                if (!tf.isValid()) continue;
                if (templates.containsKey(group)) {
                    templates.get(group).put(k, tf);
                } else {
                    Map<String, TemplateFile> tm = new HashMap<>();
                    tm.put(k, tf);
                    templates.put(group, tm);
                }
            }
        }
    }

    public boolean hasWord(String key) {
        return words.containsKey(key);
    }

    public String getWord(String key) {
        if (words.containsKey(key)) return words.get(key).getWord();
        else return "";
    }

    public List<String> getGroups(){
        return new ArrayList<>(templates.keySet());
    }

    public List<String> getNames(String group){
        return new ArrayList<>(templates.getOrDefault(group, Collections.emptyMap()).keySet());
    }

    public boolean hasTemplate(String key) {
        return hasTemplate(GROUP, key);
    }

    public boolean hasTemplate(String group, String key) {
        return templates.containsKey(group) && templates.get(group).containsKey(key);
    }

    public TemplateFile getTemplate(String key) {
        return getTemplate(GROUP, key);
    }

    public TemplateFile getTemplate(String group, String key) {
        if (hasTemplate(group, key)) return templates.get(group).get(key);
        else return null;
    }

    public void reload() {
        words.clear();
        templates.clear();
        readWordStock();
        readTemplate();
    }

    public void cleanup() {
        words.clear();
        templates.clear();
    }
}
