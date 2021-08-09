package com.github.mrmks.mc.template.papi;

import org.bukkit.OfflinePlayer;

public interface IPAPIParser {
    String parse(OfflinePlayer player, String str);
}
