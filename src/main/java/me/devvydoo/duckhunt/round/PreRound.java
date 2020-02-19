package me.devvydoo.duckhunt.round;

import me.devvydoo.duckhunt.Duckhunt;
import me.devvydoo.duckhunt.tasks.TitleTimerTask;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PreRound implements Round {

    private Duckhunt plugin;
    private TitleTimerTask titleTimerTask;

    public PreRound(Duckhunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public RoundType getRoundType() {
        return RoundType.PREGAME;
    }

    @Override
    public void startRound() {
        for (Player p : plugin.getGame().getActivePlayers()) {
            p.sendTitle(ChatColor.GREEN + "Starting game...", "", 10, 12 * 20, 40);
            p.setAllowFlight(false);
        }
        titleTimerTask = new TitleTimerTask(plugin.getGame(), System.currentTimeMillis() + 7 * 1000);
        titleTimerTask.runTaskTimer(plugin, 1, 20);
    }

    @Override
    public void endRound() {

        for (Player p : plugin.getGame().getActivePlayers()){
            if (p.getAllowFlight())
                p.setFlying(false);
            p.setAllowFlight(false);
            p.setInvulnerable(false);
        }

        titleTimerTask.cancel();
    }

    @Override
    public RoundType getNextRoundType() {
        return RoundType.ACTION;
    }
}
