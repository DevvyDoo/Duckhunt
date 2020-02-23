package me.devvydoo.duckhunt.commands;

import com.sun.istack.internal.NotNull;
import me.devvydoo.duckhunt.game.DuckhuntGame;
import me.devvydoo.duckhunt.round.WaitingRound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

public class DuckhuntAdminGUI implements Listener, InventoryHolder {

    private DuckhuntGame game;
    private final Inventory inventory;

    private final int HUNTER_SPAWN_SLOT = 10;
    private final int DUCK_SPAWN_SLOT = 12;
    private final int SPECTATOR_SPAWN_SLOT = 28;
    private final int LOBBY_SPAWN_SLOT = 30;

    private final int START_GAME_SLOT = 43;

    public DuckhuntAdminGUI(DuckhuntGame game) {
        this.game = game;
        inventory = Bukkit.createInventory(this, 54, ChatColor.RED + "Duck Hunt (Admin)");
        initInterface();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void initInterface(){

        if (game.getHunterSpawn() != null)
            inventory.setItem(HUNTER_SPAWN_SLOT, createButton(Material.BOW, ChatColor.GREEN + "Set Hunter Spawn",
                    ChatColor.GRAY + "Indicates where the hunter will spawn", "",
                    ChatColor.GREEN + "The spawn is currently set to:",
                    ChatColor.DARK_GREEN + "X: " + ChatColor.YELLOW + game.getHunterSpawn().getBlockX(),
                    ChatColor.DARK_GREEN + "Y: " + ChatColor.YELLOW + game.getHunterSpawn().getBlockY(),
                    ChatColor.DARK_GREEN + "Z: " + ChatColor.YELLOW + game.getHunterSpawn().getBlockZ()));
        else
            inventory.setItem(HUNTER_SPAWN_SLOT, createButton(Material.BOW, ChatColor.RED + "Set Hunter Spawn",
                    ChatColor.GRAY + "Indicates where the hunter will spawn", "",
                    ChatColor.RED + "The spawn is currently not set!"));

        if (game.getDuckSpawn() != null)
            inventory.setItem(DUCK_SPAWN_SLOT, createButton(Material.FEATHER, ChatColor.GREEN + "Set Duck Spawn",
                    ChatColor.GRAY + "Indicates where the duck/runners will spawn", "",
                    ChatColor.GREEN + "The spawn is currently set to:",
                    ChatColor.DARK_GREEN + "X: " + ChatColor.YELLOW + game.getDuckSpawn().getBlockX(),
                    ChatColor.DARK_GREEN + "Y: " + ChatColor.YELLOW + game.getDuckSpawn().getBlockY(),
                    ChatColor.DARK_GREEN + "Z: " + ChatColor.YELLOW + game.getDuckSpawn().getBlockZ()));
        else
            inventory.setItem(DUCK_SPAWN_SLOT, createButton(Material.FEATHER, ChatColor.RED + "Set Duck Spawn",
                    ChatColor.GRAY + "Indicates where the ducks/runners will spawn", "",
                    ChatColor.RED + "The spawn is currently not set!"));

        if (game.getSpectatorSpawn() != null)
            inventory.setItem(SPECTATOR_SPAWN_SLOT, createButton(Material.GLASS, ChatColor.GREEN + "Set Spectate Spawn",
                    ChatColor.GRAY + "Indicates where the dead players will spawn", "",
                    ChatColor.GREEN + "The spawn is currently set to:",
                    ChatColor.DARK_GREEN + "X: " + ChatColor.YELLOW + game.getSpectatorSpawn().getBlockX(),
                    ChatColor.DARK_GREEN + "Y: " + ChatColor.YELLOW + game.getSpectatorSpawn().getBlockY(),
                    ChatColor.DARK_GREEN + "Z: " + ChatColor.YELLOW + game.getSpectatorSpawn().getBlockZ()));
        else
            inventory.setItem(SPECTATOR_SPAWN_SLOT, createButton(Material.GLASS, ChatColor.RED + "Set Spectate Spawn",
                    ChatColor.GRAY + "Indicates where the dead players will spawn", "",
                    ChatColor.RED + "The spawn is currently not set!"));


        if (game.getLobbySpawn() != null)
            inventory.setItem(LOBBY_SPAWN_SLOT, createButton(Material.DARK_OAK_DOOR, ChatColor.GREEN + "Set Lobby Spawn",
                    ChatColor.GRAY + "Indicates where the players spawn after the game", "",
                    ChatColor.GREEN + "The spawn is currently set to:",
                    ChatColor.DARK_GREEN + "X: " + ChatColor.YELLOW + game.getLobbySpawn().getBlockX(),
                    ChatColor.DARK_GREEN + "Y: " + ChatColor.YELLOW + game.getLobbySpawn().getBlockY(),
                    ChatColor.DARK_GREEN + "Z: " + ChatColor.YELLOW + game.getLobbySpawn().getBlockZ()));
        else
            inventory.setItem(LOBBY_SPAWN_SLOT, createButton(Material.DARK_OAK_DOOR, ChatColor.RED + "Set Lobby Spawn",
                    ChatColor.GRAY + "Indicates where the players spawn after the game", "",
                    ChatColor.RED + "The spawn is currently not set!"));

        if (game.getCurrentRound() instanceof WaitingRound && game.isGameReady())
            inventory.setItem(START_GAME_SLOT, createButton(Material.GREEN_WOOL, ChatColor.GREEN + ChatColor.BOLD.toString() + "Start Game!", ChatColor.GRAY + "Starts the game!"));
        else if (game.getCurrentRound() instanceof WaitingRound && !game.isGameReady())
            inventory.setItem(START_GAME_SLOT, createButton(Material.GRAY_WOOL, ChatColor.RED + ChatColor.BOLD.toString() + "Can't Start!", ChatColor.DARK_RED + "Either you don't have enough players, or!", ChatColor.DARK_RED + "There are missing spawn points!"));
        else
            inventory.setItem(START_GAME_SLOT, createButton(Material.RED_WOOL, ChatColor.RED + ChatColor.BOLD.toString() + "END GAME!!!", ChatColor.DARK_RED + "This will end the game and reset scoreboard progress!"));
    }

    private ItemStack createButton(Material material, String name, String... loreContents){
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(new ArrayList<>(Arrays.asList(loreContents)));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public void openInterface(Player player){
        initInterface();
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){

        if (event.getInventory().getHolder() != this)
            return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();

        switch (slot){

            case HUNTER_SPAWN_SLOT:
                game.setHunterSpawn(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Hunter Spawn set!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .7f, 2);
                initInterface();
                break;
            case DUCK_SPAWN_SLOT:
                game.setDuckSpawn(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Duck spawn set!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .7f, 2);
                initInterface();
                break;
            case SPECTATOR_SPAWN_SLOT:
                game.setSpectatorSpawn(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Spectate spawn set!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .7f, 2);
                initInterface();
                break;
            case LOBBY_SPAWN_SLOT:
                game.setLobbySpawn(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Lobby spawn set!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .7f, 2);
                initInterface();
                break;
            case START_GAME_SLOT:
                if (game.getCurrentRound() instanceof WaitingRound && game.isGameReady()) {
                    game.startGame();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .7f, 2);
                    player.sendMessage(ChatColor.GREEN + "Starting a new game!");
                }
                else if (game.getCurrentRound() instanceof WaitingRound && !game.isGameReady()){}
                else {
                    game.endGame();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .7f, 2);
                    player.sendMessage(ChatColor.RED + "Ending the current game!");
                }
                initInterface();
                break;

        }
    }
}
