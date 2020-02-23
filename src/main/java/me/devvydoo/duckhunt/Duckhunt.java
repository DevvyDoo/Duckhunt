package me.devvydoo.duckhunt;

import me.devvydoo.duckhunt.commands.DuckhuntCommand;
import me.devvydoo.duckhunt.game.DuckhuntGame;
import me.devvydoo.duckhunt.util.CustomItemManager;
import me.devvydoo.duckhunt.util.DuckhuntFile;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class Duckhunt extends JavaPlugin implements Listener {

    private DuckhuntGame game;
    private CustomItemManager customItemManager;

    private HashMap<World, DuckhuntFile> worldConfigMap = new HashMap<>();

    public DuckhuntGame getGame() {
        return game;
    }

    public CustomItemManager getCustomItemManager(){
        return customItemManager;
    }

    public void updateWorldLocations(World world, DuckhuntGame game){
        worldConfigMap.get(world).updateLocations(game);
    }

    private void setupServerSettings(){
        getServer().setSpawnRadius(0);
    }


    private void setupWorldConfigs(){

        String rootPath = getServer().getWorldContainer().getAbsolutePath();
        rootPath = rootPath.substring(0, rootPath.length() - 1);

        for (World w : getServer().getWorlds()) {
            if (w.getEnvironment().equals(World.Environment.NORMAL))
                worldConfigMap.put(w, new DuckhuntFile(w, rootPath + w.getName()));
        }

        // See if we should load from existing settings
        for (World w : getServer().getWorlds()) {
            if (w.getEnvironment().equals(World.Environment.NORMAL)){
                worldConfigMap.get(w).parseFile();
                if (worldConfigMap.get(w).shouldSaveToFile()){
                    DuckhuntFile config = worldConfigMap.get(w);
                    game = new DuckhuntGame(this, w, config.getHunterSpawn(), config.getRunnerSpawn(), config.getSpectatorSpawn(), config.getLobbySpawn(), config.getTimeSeconds());
                }
            }
        }

        if (game == null)
            game = new DuckhuntGame(this);

    }


    @Override
    public void onEnable() {

        setupWorldConfigs();
        setupServerSettings();

        customItemManager = new CustomItemManager(this);
        DuckhuntCommand duckhuntCommand = new DuckhuntCommand(this);

        getCommand("duckhunt").setExecutor(duckhuntCommand);

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(game, this);

    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event){
        String rootPath = getServer().getWorldContainer().getAbsolutePath();
        rootPath = rootPath.substring(0, rootPath.length() - 1);
        worldConfigMap.putIfAbsent(event.getWorld(), new DuckhuntFile(event.getWorld(), rootPath + event.getWorld().getName()));
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event){
        worldConfigMap.remove(event.getWorld());
    }
}
