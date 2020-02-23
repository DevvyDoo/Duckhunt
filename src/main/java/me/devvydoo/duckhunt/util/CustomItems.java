package me.devvydoo.duckhunt.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum CustomItems {

    ADMIN_FEATHER(Material.FEATHER, ChatColor.RED + "Duckhunt Admin", "dhadmin", "", ChatColor.GRAY + "Right click to open game settings!");

    public final Material material;
    public final String name;
    public final String key;
    public final String[] lore;

    CustomItems(Material material, String name, String key, String... lore){
        this.material = material;
        this.name = name;
        this.key = key;
        this.lore = lore;
    }

}


