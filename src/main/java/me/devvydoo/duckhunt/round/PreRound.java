package me.devvydoo.duckhunt.round;

import me.devvydoo.duckhunt.Duckhunt;
import me.devvydoo.duckhunt.tasks.TitleTimerTask;
import me.devvydoo.duckhunt.util.CustomItems;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Item;
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

            p.getInventory().clear();

            if (p.isOp())
                p.getInventory().addItem(plugin.getCustomItemManager().getCustomItemOfType(CustomItems.ADMIN_FEATHER));

            p.sendTitle(ChatColor.GREEN + "Starting game...", "", 10, 12 * 20, 40);
            if (p.getGameMode().equals(GameMode.ADVENTURE) || p.getGameMode().equals(GameMode.SURVIVAL))
                p.setAllowFlight(false);
            p.teleport(plugin.getGame().getLobbySpawn());
        }
        titleTimerTask = new TitleTimerTask(plugin.getGame(), System.currentTimeMillis() + 15 * 1000);
        titleTimerTask.runTaskTimer(plugin, 1, 20);

        for (Item drop : plugin.getGame().getWorld().getEntitiesByClass(Item.class))
            drop.remove();
    }

    @Override
    public void endRound() {

        for (Player p : plugin.getGame().getActivePlayers()){
            if (p.getAllowFlight())
                p.setFlying(false);
            p.setAllowFlight(false);
        }

        titleTimerTask.cancel();
    }

    @Override
    public RoundType getNextRoundType() {
        return RoundType.ACTION;
    }
}
