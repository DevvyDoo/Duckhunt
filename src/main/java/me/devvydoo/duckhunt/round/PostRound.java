package me.devvydoo.duckhunt.round;

import me.devvydoo.duckhunt.Duckhunt;
import me.devvydoo.duckhunt.tasks.ExpTimerTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PostRound implements Round {

    private Duckhunt plugin;
    private ExpTimerTask expTimerTask;

    public PostRound(Duckhunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public RoundType getRoundType() {
        return null;
    }

    /**
     * This is called immediately after the game ends, so this should report who won, etc
     */
    @Override
    public void startRound() {
        expTimerTask = new ExpTimerTask(plugin.getGame(), System.currentTimeMillis() + 7 * 1000, false);
        expTimerTask.runTaskTimer(plugin, 1, 1);
    }

    /**
     * This is called once the post round intermission ends, so right before all players are sent to the lobby.
     */
    @Override
    public void endRound() {

        expTimerTask.cancel();

        for (Player p : plugin.getGame().getActivePlayers()){

            p.setExp(0);
            p.setLevel(0);

            p.getInventory().clear();
            p.teleport(plugin.getGame().getLobbySpawn());

            for (Player oP : Bukkit.getOnlinePlayers()){
                if (p != oP)
                    p.showPlayer(plugin, oP);
            }

        }

    }

    @Override
    public RoundType getNextRoundType() {
        return RoundType.PREGAME;
    }
}
