package com.github.MrMks.template_lore;

import com.github.MrMks.dev_tools_b.cmd.CommandRegistry;
import com.github.MrMks.dev_tools_b.cmd.ICmdFunc;
import com.github.MrMks.dev_tools_b.cmd.SenderType;
import com.github.MrMks.dev_tools_b.cmd.SubCommand;
import com.github.MrMks.template_lore.cmd.CmdGet;
import com.github.MrMks.template_lore.cmd.CmdList;
import com.google.common.collect.ImmutableSet;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class CommandManager {
    private static PluginCommand pm;
    public static void register(PluginCommand pm) {
        CommandManager.pm = pm;
        SubCommand root = new SubCommand(pm.getName(), new HashSet<>(pm.getAliases()), "base command for plugin 'Template Lore'","<sub> [args]", SenderType.ANYONE, "tl.perm.*");
        SubCommand reload = new SubCommand("reload", ImmutableSet.of("r"), "reload Template plugin", "", SenderType.ANYONE,"tl.perm.reload", new ICmdFunc() {
            @Override
            public List<String> onTabComplete(CommandSender commandSender, String s, List<String> list) {
                return Collections.emptyList();
            }

            @Override
            public boolean onExecute(CommandSender commandSender, String s, List<String> list) {
                Plugin plugin = pm.getPlugin();
                plugin.onDisable();
                plugin.onEnable();
                commandSender.sendMessage("§2plugin " + plugin.getName() + " has been reload");
                return true;
            }
        });
        SubCommand list = new SubCommand("list", ImmutableSet.of("l"), "list templates", "[page]", SenderType.ANYONE, "tl.perm.list", new CmdList());
        SubCommand get = new SubCommand("get", ImmutableSet.of("g"), "get a parsed template item", "<template> [player]", SenderType.ANYONE, "tl.perm.get", new CmdGet());
        root.addSubCommands(reload, list, get);
        CommandRegistry.register(pm, root);
    }

    public static void unregister() {
        CommandRegistry.unregister(pm);
        pm = null;
    }
}
