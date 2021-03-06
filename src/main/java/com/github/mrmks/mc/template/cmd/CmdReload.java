package com.github.mrmks.mc.template.cmd;

import com.github.mrmks.mc.marcabone.cmd.FunctionCommand;
import com.github.mrmks.mc.marcabone.lang.LanguageAPI;
import com.github.mrmks.mc.marcabone.lang.LanguageHelper;
import com.github.mrmks.mc.marcabone.utils.ArraySlice;
import com.github.mrmks.mc.template.config.ConfigManager;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdReload extends FunctionCommand {

    private final ConfigManager cfg;
    private final LanguageAPI api;
    public CmdReload(LanguageAPI api, ConfigManager cfg) {
        super(api, "tl.cmd.reload", "reload", null, "tl.cmd.reload.desc", "tl.cmd.reload.usg", "tl.perm.cmd.reload", "tl.cmd.reload.permMsg");
        this.cfg = cfg;
        this.api = api;
    }

    @Override
    public List<String> complete(CommandSender commandSender, String label, List<String> fLabel, ArraySlice<String> args) {
        return Collections.emptyList();
    }

    @Override
    public boolean execute(CommandSender sender, String label, List<String> fLabel, ArraySlice<String> args) {
        LanguageHelper helper = getHelper(sender);
        sender.sendMessage(helper.trans("tl.cmd.reload.reloading_cfg"));
        cfg.reload();
        sender.sendMessage(helper.trans("tl.cmd.reload.reloading_lapi"));
        api.reload();
        sender.sendMessage(helper.trans("tl.cmd.reload.reload_finish"));
        return true;
    }
}
