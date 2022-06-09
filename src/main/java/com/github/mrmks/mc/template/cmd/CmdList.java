package com.github.mrmks.mc.template.cmd;

import com.github.mrmks.mc.marcabone.cmd.FunctionCommand;
import com.github.mrmks.mc.marcabone.lang.LanguageAPI;
import com.github.mrmks.mc.marcabone.lang.LanguageHelper;
import com.github.mrmks.mc.marcabone.utils.ArraySlice;
import com.github.mrmks.mc.marcabone.utils.StringReplace;
import com.github.mrmks.mc.template.config.ConfigManager;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CmdList extends FunctionCommand {

    private final ConfigManager cfg;
    public CmdList(LanguageAPI api, ConfigManager cfg) {
        super(api,
                "tl.cmd.list",
                "list", null,
                "tl.cmd.list.desc",
                "tl.cmd.list.usg",
                "tl.perm.cmd.list",
                "tl.cmd.list.permMsg");
        this.cfg =cfg;
    }

    public List<String> complete(CommandSender commandSender, String label, List<String> fLabel, ArraySlice<String> args) {
        if (args.size() > 1) return Collections.emptyList();
        else {
            String prefix = args.isEmpty() ? "" : args.first();
            List<String> res = cfg.getGroups();
            return res.stream().filter(s1 -> !s1.isEmpty() && s1.startsWith(prefix)).sorted().collect(Collectors.toList());
        }
    }

    public boolean execute(CommandSender commandSender, String label, List<String> fLabel, ArraySlice<String> args) {
        int page = 0;
        String group;
        if (args.size() > 1) {
            group = args.first();
            try {
                page = Integer.parseInt(args.at(1)) - 1;
            } catch (Throwable tr) {
                LanguageHelper helper = getHelper(commandSender);
                commandSender.sendMessage(helper.trans("tl.cmd.list.page_not_num", "arg", args.at(1)));
                return true;
            }
        } else if (args.size() > 0) {
            group = args.first();
        } else {
            group = ConfigManager.GROUP;
        }
        List<String> keys = cfg.getNames(group).stream().sorted().collect(Collectors.toList());
        page = Math.max(Math.min(page, (keys.size() - 1) / 10), 0);
        LanguageHelper helper = getHelper(commandSender);
        StringBuilder builder = new StringBuilder(new StringReplace(helper.trans("tl.cmd.list.list_title"))
                .replace("group", group).replace("page", Integer.toString(page + 1)).toString()).append('\n');
        int first = page * 10;
        for (int i = first; i < Math.min(first + 10, keys.size()); i++){
            builder.append(keys.get(i)).append("\n");
        }
        commandSender.sendMessage(builder.toString());
        return true;
    }
}
