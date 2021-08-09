package com.github.mrmks.mc.template;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.List;

public class TemplateFile {
    // this can't be empty
    private String material;
    // this is allowed to be empty, 0 by default
    private String damage;
    private short damageI;
    private boolean damageF;
    // this is allowed to be empty, 1 by default
    private String size;
    private int sizeI;
    private boolean sizeF;

    // this is allowed to be empty
    private String name;
    private boolean nameF = false;
    private String locName;
    private boolean locNameF = false;
    private List<String> lore;
    private boolean loreF = false;
    private ConfigurationSection nbtSection;
    private boolean nbtF = false;

    private boolean valid = false;
    public TemplateFile(ConfigurationSection section) {
        if (section != null) {
            String tmp;
            if (section.contains("id")) {
                tmp = section.getString("id");
                if (!tmp.isEmpty()) {
                    this.material = tmp;
                    valid = true;
                }
            }
            if (section.contains("name")) {
                this.name = section.getString("name");
                this.nameF = true;
            }
            if (section.contains("locName")) {
                this.locName = section.getString("locName");
                this.locNameF = true;
            }
            if (section.contains("damage")) {
                Object obj = section.get("damage");
                if (obj instanceof String) {
                    damage = (String) obj;
                    damageF = false;
                }
                else {
                    damageF = true;
                    damageI = (obj instanceof Number) ? ((Number)obj).shortValue() : 0;
                }
            } else {
                damageF = true;
                damageI = 0;
            }
            if (section.contains("size")) {
                Object obj = section.get("size");
                if (obj instanceof String) {
                    this.size = (String) obj;
                    this.sizeF = false;
                }
                else {
                    this.sizeF = true;
                    this.sizeI = (obj instanceof Number) ? ((Number)obj).intValue() : 1;
                }
            } else {
                this.sizeF = true;
                this.sizeI = 1;
            }
            if (section.contains("lore")) {
                if (section.isList("lore")) this.lore = section.getStringList("lore");
                else this.lore = Collections.singletonList(section.getString("lore"));
                this.loreF = true;
            }
            if (section.isConfigurationSection("nbt")) {
                this.nbtSection = section.getConfigurationSection("nbt");
                this.nbtSection.set("display", null);
                this.nbtSection.set("id", null);
                this.nbtSection.set("Damage", null);
                this.nbtSection.set("Count", null);
                this.nbtF = true;
            }
        }
    }

    public boolean isValid() {
        return valid;
    }

    public String getMaterial(){
        return material;
    }

    public boolean isDirectDamage() {
        return damageF;
    }

    public short getDamageI() {
        return damageI;
    }

    public String getDamage() {
        return damage;
    }

    public boolean isDirectSize() {
        return sizeF;
    }

    public int getSizeI() {
        return sizeI;
    }

    public String getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public boolean isSetName() {
        return nameF;
    }

    public String getLocName() {
        return locName;
    }

    public boolean isSetLocName() {
        return locNameF;
    }

    public List<String> getLore() {
        return lore;
    }

    public boolean isSetLore() {
        return loreF;
    }

    public ConfigurationSection getNbtSection() {
        return nbtSection;
    }

    public boolean isSetNbt() {
        return nbtF;
    }
}
