package com.github.MrMks.template_lore;

import com.github.MrMks.template_lore.cmd.CmdGet;
import com.github.MrMks.template_lore.cmd.CmdList;
import com.github.MrMks.template_lore.cmd.CmdReload;
import com.github.mrmks.mc.dev_tools_b.cmd.CommandPackage;
import com.github.mrmks.mc.dev_tools_b.cmd.CommandProperty;
import com.github.mrmks.mc.dev_tools_b.cmd.CommandRegistry;
import com.github.mrmks.mc.dev_tools_b.cmd.SubCommand;
import com.github.mrmks.mc.dev_tools_b.lang.LanguageAPI;
import com.google.common.collect.ImmutableList;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

public class CommandManager {
    private static PluginCommand pm;
    /*
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

     */

    private Plugin plugin;
    CommandManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void register(LanguageAPI api) {
        CommandProperty rootProperty = new CommandProperty(
                "tl",
                "Base command for plugin 'TemplateLore'",
                "tl.cmd.tl.desc",
                "<command>",
                "tl.cmd.tl.usg");
        SubCommand rootFunction = new SubCommand(api);

        rootFunction.addCommand(new CommandProperty(
                "reload",
                ImmutableList.of("r"),
                "tl.perm.cmd.reload",
                "reload TemplateLore plugin",
                "tl.cmd.tl.reload.desc",
                "",
                "tl.cmd.tl.reload.usg"
        ), new CmdReload(plugin));

        rootFunction.addCommand(new CommandProperty(
                "list",
                ImmutableList.of("l"),
                "tl.perm.cmd.list",
                "list templates",
                "tl.cmd.tl.list.desc",
                "[page]",
                "tl.cmd.tl.list.usg"
        ), new CmdList());

        rootFunction.addCommand(new CommandProperty(
                "get",
                ImmutableList.of("g"),
                "tl.perm.cmd.get",
                "Get a parsed template item",
                "tl.cmd.tl.get.desc",
                "<template> [player] [args...]",
                "tl.cmd.tl.get.usg"
        ), new CmdGet());

        CommandPackage pack = new CommandPackage(rootProperty, rootFunction);
        CommandRegistry.register(plugin, pack);
    }

    public void unregister() {
        CommandRegistry.unregister(plugin);
    }
}
