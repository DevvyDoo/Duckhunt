package me.devvydoo.duckhunt.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class InfiniteTitleTask extends BukkitRunnable {

    private String title;
    private String subtitle;

    public InfiniteTitleTask(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers())
            player.sendTitle(title, subtitle, 0, 20, 0);
    }
}
