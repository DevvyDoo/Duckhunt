package me.devvydoo.duckhunt.util;

import me.devvydoo.duckhunt.Duckhunt;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashMap;

public class CustomItemManager {

    private Duckhunt plugin;
    private HashMap<CustomItems, NamespacedKey> itemKeyMap;

    public CustomItemManager(Duckhunt plugin) {
        this.plugin = plugin;

        itemKeyMap = new HashMap<>();
        for (CustomItems type : CustomItems.values())
            itemKeyMap.put(type, new NamespacedKey(plugin, type.key));
    }

    public boolean isCustomItemType(ItemStack itemStack, CustomItems type){
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        return container.has(itemKeyMap.get(type), PersistentDataType.INTEGER);
    }

    public ItemStack getCustomItemOfType(CustomItems type){
        ItemStack item = new ItemStack(type.material);

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        meta.setDisplayName(type.name);  // Set display name of item
        meta.setLore(Arrays.asList(type.lore));  // Set description of item
        container.set(itemKeyMap.get(type), PersistentDataType.INTEGER, 1);  // Put a permanant flag on the item
        meta.addItemFlags(ItemFlag.values()); // Hide some text on the item
        item.setItemMeta(meta);
        return item;
    }

}
