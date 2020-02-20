package me.devvydoo.duckhunt.round;

import me.devvydoo.duckhunt.Duckhunt;
import me.devvydoo.duckhunt.tasks.ExpTimerTask;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionRound implements Round {

    private Duckhunt plugin;
    private ExpTimerTask expTimerTask;

    public ActionRound(Duckhunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public RoundType getRoundType() {
        return RoundType.ACTION;
    }



    @Override
    public void startRound() {

        plugin.getGame().randomizePlayers();

        expTimerTask = new ExpTimerTask(plugin.getGame(), System.currentTimeMillis() + 180 * 1000, true);
        expTimerTask.runTaskTimer(plugin, 1, 1);

        // Disable fall damage protection after a second
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getGame().setCancelFallDamage(false);
            }
        }.runTaskLater(plugin, 20);
    }

    @Override
    public void endRound() {
        expTimerTask.cancel();

        // All runners are dead
        if (plugin.getGame().getDucks().size() == 0)
            plugin.getGame().announceHunterWon("All runners have died!");

        // The clock ended, and there are still runners alive
        else if (expTimerTask.getEnd() <= System.currentTimeMillis())
            plugin.getGame().announceHunterWon("Time is up!");

        // We must be calling this from somewhere else... hmmm
        else
            plugin.getGame().announceRunnersWon("The hunter was defeated!");

        plugin.getGame().setCancelFallDamage(true);

    }

    @Override
    public RoundType getNextRoundType() {
        return RoundType.POST;
    }
}
