package com.github.mrmks.mc.template.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigUtils {
    public static List<ConfigurationSection> getConfigs(File file) {
        if (file == null || !file.exists() || !file.canRead()) return Collections.emptyList();
        List<ConfigurationSection> list = new ArrayList<>();
        getConfigs(file, list);
        return list;
    }

    private static void getConfigs(File file, List<ConfigurationSection> list) {
        assert file != null;
        assert list != null;
        if (file.isFile()) list.add(YamlConfiguration.loadConfiguration(file));
        else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File sub : files) {
                    if (sub != null && sub.exists() && sub.canRead()) getConfigs(sub, list);
                }
            }
        }
    }
}
