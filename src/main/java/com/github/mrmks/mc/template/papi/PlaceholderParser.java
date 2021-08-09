package com.github.mrmks.mc.template.papi;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;

public class PlaceholderParser implements IPAPIParser {
    public String parse(OfflinePlayer player, String str){
        return PlaceholderAPI.setPlaceholders(player, str);
    }
}
