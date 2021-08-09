package com.github.mrmks.mc.template.cmd;

import com.github.mrmks.mc.dev_tools_b.cmd.FunctionCfgCommand;
import com.github.mrmks.mc.dev_tools_b.lang.LanguageAPI;
import com.github.mrmks.mc.dev_tools_b.utils.ArraySlice;
import com.github.mrmks.mc.template.config.ConfigManager;
import com.github.mrmks.mc.template.TemplateFile;
import com.github.mrmks.mc.template.TemplateParser;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CmdGet extends FunctionCfgCommand {

    private final ConfigManager cfg;
    public CmdGet(LanguageAPI api, ConfigManager cfg) {
        super(api,
                "tl.cmd.get",
                "get", new String[]{"g"},
                "desc",
                "usg",
                "perm",
                "permMsg");
        this.cfg = cfg;
    }

    public List<String> complete(CommandSender commandSender, String label, String fLabel, ArraySlice<String> args) {
        if (args.size() == 1) {
            List<String> tmps = cfg.getGroups();
            String arg = args.at(0);
            List<String> re = new ArrayList<>();
            for (String tmp : tmps) {
                if (arg.equals("") || tmp.startsWith(arg)) re.add(tmp);
            }
            return re;
        } else if (args.size() == 2) {
            List<String> tmps = cfg.getNames(args.at(0));
            String arg = args.at(1);
            List<String> re = new ArrayList<>();
            for (String tmp : tmps) {
                if (arg.equals("") || tmp.startsWith(arg)) re.add(tmp);
            }
            return re;
        } else if (args.size() == 3) {
            return null;
        }
        else return Collections.emptyList();
    }

    public boolean execute(CommandSender sender, String label, String fLabel, ArraySlice<String> args) {
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
                    name = args.at(0);
                    args = args.slice(1);
                    break;
                case 2:
                default:
                    group = args.at(0);
                    name = args.at(1);
                    args = args.slice(2);
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
                    name = args.at(0);
                    playerName = args.at(1);
                    player = getPlayer(playerName);
                    args = args.slice(2);
                    break;
                case 3:
                default:
                    group = args.at(0);
                    name = args.at(1);
                    playerName = args.at(2);
                    player = getPlayer(playerName);
                    args = args.slice(3);
                    break;
            }
            if (player == null) {
                sender.sendMessage("Can not find online player named " + playerName);
                return true;
            }
        }
        TemplateFile template = cfg.getTemplate(group, name);
        if (template != null) {
            Map<String, String> map = new HashMap<>();
            for (int i = 0; i < args.size() / 2; i++) {
                map.put(args.at(i * 2), args.at(i * 2 + 1));
            }
            ItemStack stack = TemplateParser.parseTemplate(player, template, cfg, map);
            player.getInventory().addItem(stack);
            sender.sendMessage("Player " + player.getName() + " have got the target item");
        } else {
            sender.sendMessage("Can not get template with name " + name + " in group " + group);
        }
        return true;
    }

    private Player getPlayer(String name) {
        return Bukkit.getPlayer(name);
    }
}