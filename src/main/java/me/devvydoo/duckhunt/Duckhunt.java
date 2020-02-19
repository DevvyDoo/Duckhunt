package me.devvydoo.duckhunt;

import me.devvydoo.duckhunt.commands.DuckhuntCommand;
import me.devvydoo.duckhunt.game.DuckhuntGame;
import org.bukkit.plugin.java.JavaPlugin;

public final class Duckhunt extends JavaPlugin {

    private DuckhuntGame game;

    public DuckhuntGame getGame() {
        return game;
    }

    @Override
    public void onEnable() {

        getServer().setSpawnRadius(0);

        game = new DuckhuntGame(this);

        getServer().getPluginManager().registerEvents(game, this);

        getCommand("duckhunt").setExecutor(new DuckhuntCommand(this));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
