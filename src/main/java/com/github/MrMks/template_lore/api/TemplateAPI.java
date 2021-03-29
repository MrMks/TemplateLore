package com.github.MrMks.template_lore.api;

import com.github.MrMks.template_lore.config.ConfigManager;
import com.github.MrMks.template_lore.config.TemplateFile;
import com.github.MrMks.template_lore.config.TemplateParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

public class TemplateAPI {
    private Plugin plugin;
    private boolean loaded;
    public TemplateAPI(Plugin plugin) {
        this.plugin = plugin;
        loaded = true;
    }

    public void unload() {
        loaded = false;
        plugin = null;
    }

    public void addWordStock(String key, List<String> words) throws IllegalStateException{
        if (!loaded) throw new IllegalStateException("This TemplateAPI instance has been unloaded");
        if (ConfigManager.hasWord(key)) plugin.getLogger().warning(String.format("WordStock with name %s has been registered", key));
        else ConfigManager.addWordStock(key, words);
    }

    public void addTemplate(String key, TemplateFile file) throws IllegalStateException{
        addTemplate("default", key, file);
    }

    public void addTemplate(String group, String key, TemplateFile file) {
        if (!loaded) throw new IllegalStateException("This TemplateAPI instance has been unloaded");
        if (ConfigManager.hasTemplate(group, key)) plugin.getLogger().warning(String.format("Template with name %s has been registered", key));
        else ConfigManager.addTemplate(group, key, file);
    }

    public ItemStack getParsedTemplate(String key, UUID player) throws IllegalStateException{
        return getParsedTemplate("default", key, player);
    }

    public ItemStack getParsedTemplate(String group, String key, UUID player) throws IllegalStateException {
        if (!loaded) throw new IllegalStateException("This TemplateAPI instance has been unloaded");
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
        if (ConfigManager.hasTemplate(group, key) && offlinePlayer != null) return TemplateParser.getParsedTemplate(offlinePlayer, group, key);
        else return new ItemStack(Material.AIR);
    }

    public ItemStack getParsedTemplate(TemplateFile file, UUID player) throws IllegalStateException {
        if (!loaded) throw new IllegalStateException("This TemplateAPI instance has been unloaded");
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
        if (file != null && offlinePlayer != null) return TemplateParser.getParsedTemplate(offlinePlayer, file);
        else return new ItemStack(Material.AIR);
    }
}
