package com.github.mrmks.mc.template.cmd;

import com.github.mrmks.mc.dev_tools_b.cmd.CommandAdaptorSub;
import com.github.mrmks.mc.dev_tools_b.cmd.CommandConfiguration;
import com.github.mrmks.mc.dev_tools_b.lang.LanguageAPI;
import com.github.mrmks.mc.template.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

public class CommandManager {
    private final Plugin plugin;
    public CommandManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void register(LanguageAPI api, PluginCommand root, ConfigManager cfg) {
        CommandConfiguration cmdCfg = new CommandConfiguration(plugin);
        CommandAdaptorSub rootAdaptor = new CommandAdaptorSub(api,"tl.cmd.root", root);
        root.setExecutor(rootAdaptor);
        cmdCfg.loadCommand(rootAdaptor);
        rootAdaptor.addChild(cmdCfg.loadCommand(new CmdReload(api, cfg)));
        rootAdaptor.addChild(cmdCfg.loadCommand(new CmdList(api, cfg)));
        rootAdaptor.addChild(cmdCfg.loadCommand(new CmdGive(api, cfg)));
        cmdCfg.save();
        rootAdaptor.registerHelpTopic(Bukkit.getHelpMap());
    }

    public void unregister(PluginCommand cmd) {
        cmd.setExecutor(null);
    }
}
