package me.devvydoo.duckhunt;

import me.devvydoo.duckhunt.commands.DuckhuntCommand;
import me.devvydoo.duckhunt.game.DuckhuntGame;
import me.devvydoo.duckhunt.util.DuckhuntFile;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class Duckhunt extends JavaPlugin implements Listener {

    private DuckhuntGame game;

    private String configFileName = "duckhunt.options";
    private HashMap<World, DuckhuntFile> worldConfigMap = new HashMap<>();

    public DuckhuntGame getGame() {
        return game;
    }

    public void updateWorldLocations(World world, DuckhuntGame game){
        worldConfigMap.get(world).updateLocations(game);
    }

    @Override
    public void onEnable() {

        getServer().setSpawnRadius(0);

        getServer().getPluginManager().registerEvents(this, this);

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

        getServer().getPluginManager().registerEvents(game, this);
        getCommand("duckhunt").setExecutor(new DuckhuntCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
