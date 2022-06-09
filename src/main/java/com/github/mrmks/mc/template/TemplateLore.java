package com.github.mrmks.mc.template;

import com.github.mrmks.mc.template.cmd.CommandManager;
import com.github.mrmks.mc.template.config.ConfigManager;
import com.github.mrmks.mc.template.papi.PlaceholderParser;
import com.github.mrmks.mc.marcabone.lang.LanguageAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class TemplateLore extends JavaPlugin {

    private CommandManager cmdManager;
    private ConfigManager cfgManager;

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) TemplateParser.setPapiParser(new PlaceholderParser());
        if (cfgManager == null) cfgManager = new ConfigManager(this);
        if (cmdManager == null) cmdManager = new CommandManager(this);
        cmdManager.register(new LanguageAPI(this), getCommand("tl"), cfgManager);
    }

    @Override
    public void onDisable() {
        if (cmdManager != null) cmdManager.unregister(getCommand("tl"));
        if (cfgManager != null) cfgManager.cleanup();
    }
}
