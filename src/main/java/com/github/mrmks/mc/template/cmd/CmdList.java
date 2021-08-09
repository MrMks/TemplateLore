package com.github.mrmks.mc.template.cmd;

import com.github.mrmks.mc.dev_tools_b.cmd.FunctionCfgCommand;
import com.github.mrmks.mc.dev_tools_b.lang.LanguageAPI;
import com.github.mrmks.mc.dev_tools_b.utils.ArraySlice;
import com.github.mrmks.mc.template.config.ConfigManager;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CmdList extends FunctionCfgCommand {

    private final ConfigManager cfg;
    public CmdList(LanguageAPI api, ConfigManager cfg) {
        super(api,
                "tl.cmd.list",
                "list", new String[]{"l"},
                "tl.cmd.list.desc",
                "tl.cmd.list.usg",
                "tl.perm.cmd.list",
                "tl.cmd.list.permMsg");
        this.cfg =cfg;
    }

    public List<String> complete(CommandSender commandSender, String label, String fLabel, ArraySlice<String> args) {
        if (args.size() > 1) return Collections.emptyList();
        else {
            String prefix = args.isEmpty() ? "" : args.first();
            List<String> res = cfg.getGroups();
            return res.stream().filter(s1 -> !s1.isEmpty() && s1.startsWith(prefix)).sorted().collect(Collectors.toList());
        }
    }

    public boolean execute(CommandSender commandSender, String label, String fLabel, ArraySlice<String> args) {
        int page = 0;
        String group = "default";
        if (args.size() > 1) {
            try {
                page = Integer.parseInt(args.first());
            } catch (Throwable tr) {
                commandSender.sendMessage("arg " + args.first() + " is not a number");
                return true;
            }
            group = args.at(1);
        } else if (args.size() > 0) {
            try {
                page = Integer.parseInt(args.first());
            } catch (Throwable tr) {
                group = args.first();
            }
        }
        List<String> keys = cfg.getNames(group).stream().sorted().collect(Collectors.toList());
        page = Math.max(Math.min(page, (keys.size() - 1) / 10), 0);
        StringBuilder builder = new StringBuilder("Available templates in group "+group+" at page "+page+"\n");
        int first = page * 10;
        for (int i = first; i < Math.min(first + 10, keys.size()); i++){
            builder.append(keys.get(i)).append("\n");
        }
        commandSender.sendMessage(builder.toString());
        return true;
    }
}
