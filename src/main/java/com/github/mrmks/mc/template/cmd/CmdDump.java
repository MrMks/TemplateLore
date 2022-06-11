package com.github.mrmks.mc.template.cmd;

import com.github.mrmks.mc.marcabone.cmd.FunctionCommand;
import com.github.mrmks.mc.marcabone.lang.LanguageAPI;
import com.github.mrmks.mc.marcabone.lang.LanguageHelper;
import com.github.mrmks.mc.marcabone.utils.ArraySlice;
import com.github.mrmks.mc.template.TemplateDumper;
import com.github.mrmks.mc.template.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CmdDump extends FunctionCommand {

    private final ConfigManager cfg;
    public CmdDump(LanguageAPI lapi, ConfigManager cfg) {
        super(lapi, "tl.cmd.dump", "dump", null,
                "tl.cmd.dump.desc",
                "tl.cmd.dump.usg",
                "tl.perm.cmd.dump",
                "tl.cmd.dump.permMsg");
        this.cfg = cfg;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, List<String> list, ArraySlice<String> arraySlice) {
        if (commandSender instanceof Player) {
            ItemStack stack = ((Player) commandSender).getInventory().getItemInMainHand();
            if (stack != null && stack.getType() != Material.AIR) {
                String name = arraySlice.size() > 0 ? arraySlice.first() : null;
                if (name == null) name = stack.getType().name().toLowerCase(Locale.ENGLISH) + "_" + System.currentTimeMillis();
                YamlConfiguration cfg = TemplateDumper.dumpItemStack(stack, name);
                try {
                    this.cfg.dumpTo(cfg, name);
                    LanguageHelper helper = getHelper(commandSender);
                    commandSender.sendMessage(helper.trans("tl.cmd.dump.success", "name", name));
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                LanguageHelper helper = getHelper(commandSender);
                commandSender.sendMessage(helper.trans("tl.cmd.dump.air_or_empty"));
                return true;
            }
        } else {
            LanguageHelper helper = getHelper(commandSender);
            commandSender.sendMessage(helper.trans("tl.cmd.dump.player_only"));
            return true;
        }
        return true;
    }

    @Override
    public List<String> complete(CommandSender commandSender, String s, List<String> list, ArraySlice<String> arraySlice) {
        return Collections.emptyList();
    }
}
