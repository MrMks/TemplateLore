package com.github.MrMks.template_lore;

import com.github.MrMks.template_lore.config.ConfigManager;
import com.github.MrMks.template_lore.config.TemplateParser;
import com.github.MrMks.template_lore.papi.PlaceholderParser;
import com.github.mrmks.mc.dev_tools_b.lang.LanguageAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private CommandManager cmdManager;

    @Override
    public void onLoad() {
        super.onLoad();
        if (cmdManager == null) cmdManager = new CommandManager(this);
    }

    @Override
    public void onEnable() {
        ConfigManager.init(this);
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) TemplateParser.setPapiParser(new PlaceholderParser());
        cmdManager.register(new LanguageAPI(this));
    }

    @Override
    public void onDisable() {
        cmdManager.unregister();
        ConfigManager.cleanup();
    }
}
