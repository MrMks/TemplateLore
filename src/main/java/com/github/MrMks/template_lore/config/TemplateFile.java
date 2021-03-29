package com.github.MrMks.template_lore.config;

import org.bukkit.Material;

import java.util.Collections;
import java.util.List;

public class TemplateFile {
    private final String material;
    private final String damage;
    private final String name;
    private final String locName;
    private final List<String> lore;
    public TemplateFile(String material, String damage, String name, String locName, List<String> lore){
        this.material = material != null ? material : "";
        this.damage = damage != null ? damage : "0";
        this.name = name != null ? name : "";

        this.lore = lore != null ? lore : Collections.emptyList();
        this.locName = locName;
    }

    public String getMaterial(){
        return material;
    }

    public String getDamage() {
        return damage;
    }

    public String getName() {
        return name;
    }

    public String getLocName() {
        return locName;
    }

    public List<String> getLore() {
        return lore;
    }
}
