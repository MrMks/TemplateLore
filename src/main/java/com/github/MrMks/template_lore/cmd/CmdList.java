package com.github.MrMks.template_lore.cmd;

import com.github.MrMks.template_lore.config.ConfigManager;
import com.github.mrmks.mc.dev_tools_b.cmd.AbstractCommand;
import com.github.mrmks.mc.dev_tools_b.cmd.ICommandProperty;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CmdList extends AbstractCommand {

    public List<String> onTabComplete(CommandSender commandSender, String s, List<String> list) {
        if (list.size() > 1) return Collections.emptyList();
        else {
            String arg = list.isEmpty() ? "" : list.get(0);
            List<String> res = ConfigManager.getAllGroups();
            return res.stream().filter(s1 -> !s1.isEmpty() && s1.startsWith(arg)).collect(Collectors.toList());
        }
    }

    public boolean onExecute(CommandSender commandSender, String s, List<String> list) {
        int page = 0;
        String group = "default";
        if (list.size() > 1) {
            try {
                page = Integer.parseInt(list.get(0));
            } catch (Throwable tr) {
                commandSender.sendMessage("arg " + list.get(0) + " is not a number");
                return true;
            }
            group = list.get(1);
        } else if (list.size() > 0) {
            try {
                page = Integer.parseInt(list.get(0));
            } catch (Throwable tr) {
                group = list.get(0);
            }
        }
        List<String> keys = ConfigManager.getAllTemplates(group);
        page = Math.max(Math.min(page, (keys.size() - 1) / 10), 0);
        StringBuilder builder = new StringBuilder("Available templates in group "+group+" at page "+page+"\n");
        int first = page * 10;
        for (int i = first; i < Math.min(first + 10, keys.size()); i++){
            builder.append(keys.get(i)).append("\n");
        }
        commandSender.sendMessage(builder.toString());
        return true;
    }

    @Override
    protected List<String> tabCompleteSelf(CommandSender sender, ICommandProperty property, List<String> labels, List<String> args) {
        return onTabComplete(sender, labels.get(labels.size() - 1), args);
    }

    @Override
    protected boolean commandSelf(CommandSender commandSender, ICommandProperty iCommandProperty, List<String> labels, List<String> list) {
        return onExecute(commandSender, labels.get(labels.size() - 1), list);
    }
}
