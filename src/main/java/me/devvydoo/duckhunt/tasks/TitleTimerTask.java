package me.devvydoo.duckhunt.tasks;

import me.devvydoo.duckhunt.game.DuckhuntGame;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class TitleTimerTask extends TimerTask {

    public TitleTimerTask(DuckhuntGame game, long end) {
        super(game, end);
    }

    @Override
    public void run() {
        super.run();
        if (getTotalSeconds() - (int) getCurrentSecond() > 1) {
            for (Player p : getGame().getActivePlayers()) {
                p.sendTitle(ChatColor.RED.toString() + (getTotalSeconds() - (int) getCurrentSecond() - 1), ChatColor.GRAY + "Game is starting...", 0, 60, 0);
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, .5f, .4f);
            }
        }
    }
}
