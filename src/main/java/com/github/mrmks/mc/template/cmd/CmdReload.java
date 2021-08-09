package com.github.mrmks.mc.template.cmd;

import com.github.mrmks.mc.dev_tools_b.cmd.FunctionCfgCommand;
import com.github.mrmks.mc.dev_tools_b.lang.LanguageAPI;
import com.github.mrmks.mc.dev_tools_b.lang.LanguageHelper;
import com.github.mrmks.mc.dev_tools_b.utils.ArraySlice;
import com.github.mrmks.mc.template.config.ConfigManager;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdReload extends FunctionCfgCommand {

    private final ConfigManager cfg;
    private final LanguageAPI api;
    public CmdReload(LanguageAPI api, ConfigManager cfg) {
        super(api, "tl.cmd.reload", "reload", null, "tl.cmd.reload.desc", "tl.cmd.reload.usg", "tl.perm.cmd.reload", "tl.cmd.reload.permMsg");
        this.cfg = cfg;
        this.api = api;
    }

    @Override
    public List<String> complete(CommandSender commandSender, String label, String fLabel, ArraySlice<String> args) {
        return Collections.emptyList();
    }

    @Override
    public boolean execute(CommandSender sender, String label, String fLabel, ArraySlice<String> args) {
        LanguageHelper helper = getHelper(sender);
        sender.sendMessage(helper.trans("tl.cmd.reload.reloading_cfg"));
        cfg.reload();
        sender.sendMessage(helper.trans("tl.cmd.reload.reloading_lapi"));
        api.reload();
        sender.sendMessage(helper.trans("tl.cmd.reload.reload_finish"));
        return true;
    }
}
