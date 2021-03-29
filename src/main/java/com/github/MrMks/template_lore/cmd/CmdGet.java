package com.github.MrMks.template_lore.cmd;

import com.github.MrMks.template_lore.config.ConfigManager;
import com.github.MrMks.template_lore.config.TemplateFile;
import com.github.MrMks.template_lore.config.TemplateParser;
import com.github.mrmks.mc.dev_tools_b.cmd.AbstractCommand;
import com.github.mrmks.mc.dev_tools_b.cmd.ICommandProperty;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CmdGet extends AbstractCommand {
    public List<String> onTabComplete(CommandSender commandSender, String s, List<String> list) {
        if (list.size() == 1) {
            List<String> tmps = ConfigManager.getAllGroups();
            String arg = list.get(0);
            List<String> re = new ArrayList<>();
            for (String tmp : tmps) {
                if (arg.equals("") || tmp.startsWith(arg)) re.add(tmp);
            }
            return re;
        } else if (list.size() == 2) {
            List<String> tmps = ConfigManager.getAllTemplates(list.get(0));
            String arg = list.get(1);
            List<String> re = new ArrayList<>();
            for (String tmp : tmps) {
                if (arg.equals("") || tmp.startsWith(arg)) re.add(tmp);
            }
            return re;
        } else if (list.size() == 3) {
            return null;
        }
        else return Collections.emptyList();
    }

    public boolean onExecute(CommandSender sender, String s, List<String> args) {
        Player player;
        String group;
        String name;
        if (sender instanceof Player) {
            player = (Player) sender;
            switch (args.size()) {
                case 0:
                    return false;
                case 1:
                    group = "default";
                    name = args.remove(0);
                    break;
                case 2:
                default:
                    group = args.remove(0);
                    name = args.remove(0);
                    break;
            }
        } else {
            String playerName;
            switch (args.size()) {
                case 0:
                case 1:
                    return false;
                case 2:
                    group = "default";
                    name = args.remove(0);
                    playerName = args.remove(0);
                    player = Bukkit.getPlayer(playerName);
                    break;
                case 3:
                default:
                    group = args.remove(0);
                    name = args.remove(0);
                    playerName = args.remove(0);
                    player = Bukkit.getPlayer(playerName);
                    break;
            }
            if (player == null) {
                sender.sendMessage("Can not find online player named " + playerName);
                return true;
            }
        }
        TemplateFile template = ConfigManager.getTemplate(group, name);
        if (template != null) {
            Map<String, String> map = new HashMap<>();
            if (args.size() % 2 != 0) args.remove(args.size() - 1);
            for (int i = 0; i < args.size() / 2; i++) {
                map.put(args.get(i * 2), args.get(i * 2 + 1));
            }
            ItemStack stack = TemplateParser.getParsedTemplate(player, template, map);
            player.getInventory().addItem(stack);
            sender.sendMessage("Player " + player.getName() + " have got target item");
        } else {
            sender.sendMessage("Can not get template with name " + name + " in group " + group);
        }
        return true;
    }

    @Override
    protected List<String> tabCompleteSelf(CommandSender commandSender, ICommandProperty iCommandProperty, List<String> labels, List<String> list) {
        return onTabComplete(commandSender, labels.get(labels.size() - 1), list);
    }

    @Override
    protected boolean commandSelf(CommandSender commandSender, ICommandProperty iCommandProperty, List<String> labels, List<String> list) {
        return onExecute(commandSender, labels.get(labels.size() - 1), list);
    }
}
