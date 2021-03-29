package com.github.MrMks.template_lore;

import com.github.MrMks.template_lore.config.ConfigManager;
import com.github.MrMks.template_lore.config.TemplateParser;
import com.github.MrMks.template_lore.papi.PlaceholderParser;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        ConfigManager.init(this);
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) TemplateParser.setPapiParser(new PlaceholderParser());
        CommandManager.register(getCommand("tl"));
    }

    @Override
    public void onDisable() {
        CommandManager.unregister();
        ConfigManager.cleanup();
    }
}
