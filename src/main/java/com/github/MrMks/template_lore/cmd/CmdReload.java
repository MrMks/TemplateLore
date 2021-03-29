package com.github.MrMks.template_lore.cmd;

import com.github.mrmks.mc.dev_tools_b.cmd.AbstractCommand;
import com.github.mrmks.mc.dev_tools_b.cmd.ICommandProperty;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

public class CmdReload extends AbstractCommand {

    private Plugin plugin;
    public CmdReload(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected List<String> tabCompleteSelf(CommandSender commandSender, ICommandProperty iCommandProperty, List<String> list, List<String> list1) {
        return Collections.emptyList();
    }

    @Override
    protected boolean commandSelf(CommandSender commandSender, ICommandProperty iCommandProperty, List<String> list, List<String> list1) {
        plugin.onDisable();
        plugin.onEnable();
        commandSender.sendMessage("§2Plugin " + plugin.getName() + " has been reloaded");
        return true;
    }
}
