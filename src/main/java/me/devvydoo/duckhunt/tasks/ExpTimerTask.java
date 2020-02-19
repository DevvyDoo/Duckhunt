package me.devvydoo.duckhunt.tasks;

import me.devvydoo.duckhunt.game.DuckhuntGame;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ExpTimerTask extends TimerTask {

    private boolean wantLowTimeAlert;

    public ExpTimerTask(DuckhuntGame game, long end, boolean wantLowTimeAlert) {
        super(game, end);
        this.wantLowTimeAlert = wantLowTimeAlert;
    }

    @Override
    public void run() {
        super.run();
        if ( getCurrentSecond() / getTotalSeconds() > 0 && getCurrentSecond() / getTotalSeconds() < 1) {
            for (Player p : getGame().getActivePlayers()) {
                p.setExp((float) (1 - (getCurrentSecond() / getTotalSeconds())));
                int secLeft = getTotalSeconds() - (int)getCurrentSecond();
                int oldLevel = p.getLevel();
                p.setLevel(secLeft);
                if (wantLowTimeAlert && secLeft < 11 && oldLevel != p.getLevel() ){
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .5f, .75f);
                    p.sendTitle(ChatColor.RED.toString() + secLeft, "", 0, 30, 0);
                }
            }
        }
    }
}
