package me.devvydoo.duckhunt.util;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public final class GameItems {

    public GameItems(){
        throw new IllegalStateException();
    }

    public static ItemStack getItemWithMeta(Material material, int amount, String displayName, boolean unbreakable, boolean hideAttributes, String... lore){

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setUnbreakable(unbreakable);
        if (hideAttributes)
            meta.addItemFlags(ItemFlag.values());
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;

    }

    public static ItemStack getRunnerPotion(PotionEffectType type, String name, int time, int amplifier, boolean wantSplash){
        ItemStack pot;
        if (wantSplash)
            pot = new ItemStack(Material.SPLASH_POTION);
        else
            pot = new ItemStack(Material.POTION);
        PotionMeta potionMeta = (PotionMeta) pot.getItemMeta();
        if (type.equals(PotionEffectType.SPEED))
            potionMeta.setColor(Color.AQUA);
        PotionEffect heal = new PotionEffect(type, time, amplifier);
        potionMeta.addCustomEffect(heal, true);
        potionMeta.setDisplayName(name);
        pot.setItemMeta(potionMeta);
        return pot;
    }

    public static Collection<ItemStack> getRunnerKit(){

        ArrayList<ItemStack> items = new ArrayList<>();

        ItemStack sword = getItemWithMeta(Material.DIAMOND_SWORD, 1, ChatColor.AQUA + "Runner Sword", true, true, "", ChatColor.LIGHT_PURPLE + "- Sharpness X");
        sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 10);
        items.add(sword);
        items.add(GameItems.getRunnerPotion(PotionEffectType.SPEED, ChatColor.AQUA + "Speed Potion", 10 * 20, 1, false));
        items.add(GameItems.getRunnerPotion(PotionEffectType.HEAL, ChatColor.RED + "Heal Potion", 1, 2, true));
        items.add(getItemWithMeta(Material.GOLDEN_APPLE, 1, ChatColor.AQUA + "Runner Apple", false, true));
        items.add(getItemWithMeta(Material.COOKED_BEEF, 3, ChatColor.AQUA + "Runner Steak", false, true));
        return items;
    }

    public static Collection<ItemStack> getHunterKit(){
        ArrayList<ItemStack> items = new ArrayList<>();

        ItemStack bow = getItemWithMeta(Material.BOW, 1, ChatColor.RED + ChatColor.BOLD.toString() + "Hunter Bow", true, true, "", ChatColor.LIGHT_PURPLE + "- Power II", ChatColor.LIGHT_PURPLE + "- Infinity");
        bow.addEnchantment(Enchantment.ARROW_DAMAGE, 2);
        bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
        items.add(bow);
        items.add(getItemWithMeta(Material.ARROW, 1, ChatColor.RED + "Hunter Arrow", false, true));
        items.add(getItemWithMeta(Material.COOKED_BEEF, 5, ChatColor.RED + "Hunter Steak", false, true));
        return items;
    }



}
