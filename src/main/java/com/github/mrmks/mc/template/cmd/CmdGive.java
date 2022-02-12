package com.github.mrmks.mc.template.cmd;

import com.github.mrmks.mc.dev_tools_b.cmd.FunctionCommand;
import com.github.mrmks.mc.dev_tools_b.lang.LanguageAPI;
import com.github.mrmks.mc.dev_tools_b.lang.LanguageHelper;
import com.github.mrmks.mc.dev_tools_b.utils.ArraySlice;
import com.github.mrmks.mc.dev_tools_b.utils.StringReplace;
import com.github.mrmks.mc.template.TemplateFile;
import com.github.mrmks.mc.template.TemplateParser;
import com.github.mrmks.mc.template.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CmdGive extends FunctionCommand {

    private final ConfigManager cfg;
    public CmdGive(LanguageAPI lapi, ConfigManager cfg) {
        super(lapi,
                "tl.cmd.give",
                "give", null,
                "tl.cmd.give.desc",
                "tl.cmd.give.usg",
                "tl.perm.cmd.give",
                "tl.cmd.give.permMsg");
        this.cfg = cfg;
    }

    @Override
    public boolean execute(CommandSender sender, String s, List<String> list, ArraySlice<String> args) {
        if (args.size() < 2) return false;
        Player player;
        String group, name, playerName;
        player = getPlayer(playerName = args.at(0));
        if (args.size() < 3) {
            group = ConfigManager.GROUP;
            name = args.at(1);
            args = args.slice(2);
        } else {
            group = args.at(1);
            name = args.at(2);
            args = args.slice(3);
        }
        if (player == null) {
            LanguageHelper helper = getHelper(sender);
            sender.sendMessage(helper.trans("tl.cmd.give.offline_player", "player", playerName));
            return true;
        }
        TemplateFile template = cfg.getTemplate(group, name);
        if (template != null) {
            Map<String, String> map = new HashMap<>();
            for (int i = 0; i < args.size() / 2; i++) {
                map.put(args.at(i * 2), args.at(i * 2 + 1));
            }
            ItemStack stack = TemplateParser.parseTemplate(player, template, cfg, map);
            player.getInventory().addItem(stack);
            LanguageHelper helper = getHelper(sender);
            sender.sendMessage(new StringReplace(helper.trans("tl.cmd.give.parse_success")).replace("player", player.getName()).replace("itemName", stack.getItemMeta().getDisplayName()).toString());
        } else {
            LanguageHelper helper = getHelper(sender);
            sender.sendMessage(new StringReplace(helper.trans("tl.cmd.give.not_exist")).replace("name", name).replace("group", group).toString());
        }
        return true;
    }

    @Override
    public List<String> complete(CommandSender commandSender, String s, List<String> list, ArraySlice<String> args) {
        if (args.size() == 1) {
            return null;
        } else if (args.size() == 2) {
            List<String> tmps = cfg.getGroups();
            String arg = args.at(0);
            List<String> re = new ArrayList<>();
            for (String tmp : tmps) {
                if (arg.equals("") || tmp.startsWith(arg)) re.add(tmp);
            }
            return re;
        } else if (args.size() == 3) {
            List<String> tmps = cfg.getNames(args.at(0));
            String arg = args.at(1);
            List<String> re = new ArrayList<>();
            for (String tmp : tmps) {
                if (arg.equals("") || tmp.startsWith(arg)) re.add(tmp);
            }
            return re;
        }
        else return Collections.emptyList();
    }

    @SuppressWarnings("deprecation")
    private Player getPlayer(String name) {
        return name == null || name.isEmpty() ? null : Bukkit.getPlayer(name);
    }
}
