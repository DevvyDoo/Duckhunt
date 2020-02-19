package me.devvydoo.duckhunt.round;


import me.devvydoo.duckhunt.Duckhunt;
import me.devvydoo.duckhunt.tasks.InfiniteTitleTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * This round is active when the server is idle, that is waiting on an admin to start the game
 */
public class WaitingRound implements Round {

    private Duckhunt plugin;
    private InfiniteTitleTask infiniteTitleTask;

    public WaitingRound(Duckhunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public RoundType getRoundType() {
        return RoundType.WAITING;
    }

    @Override
    public void startRound() {

        for (Player p : Bukkit.getOnlinePlayers()){
            p.setExp(0);
            p.setLevel(0);
        }

        infiniteTitleTask = new InfiniteTitleTask(ChatColor.GOLD + "Waiting...", ChatColor.GRAY + "An admin needs to start the game");
        infiniteTitleTask.runTaskTimer(plugin, 220, 1);
    }

    @Override
    public void endRound() {
        if (infiniteTitleTask != null)
            infiniteTitleTask.cancel();
    }

    @Override
    public RoundType getNextRoundType() {
        return RoundType.PREGAME;
    }
}
