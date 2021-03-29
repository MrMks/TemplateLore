package com.github.MrMks.template_lore.cmd;

import com.github.MrMks.dev_tools_b.cmd.ICmdFunc;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CmdTempInfo implements ICmdFunc {
    @Override
    public List<String> onTabComplete(CommandSender sender, String label, List<String> args) {
        return null;
    }

    @Override
    public boolean onExecute(CommandSender sender, String label, List<String> args) {
        
        return false;
    }
}
