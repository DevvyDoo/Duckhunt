package me.devvydoo.duckhunt.tasks;

import me.devvydoo.duckhunt.game.DuckhuntGame;
import org.bukkit.scheduler.BukkitRunnable;

public class TimerTask extends BukkitRunnable {

    private DuckhuntGame game;

    private long start;
    private long end;
    private double currentSecond;
    private int totalSeconds;

    public TimerTask(DuckhuntGame game, long end) {
        this.game = game;
        this.start = System.currentTimeMillis();
        this.end = end;
        this.totalSeconds = (int) ((this.end - this.start) / 1000);
    }

    private void updateCurrentSecond(){
        currentSecond = (System.currentTimeMillis() - start) / 1000.;
    }

    public DuckhuntGame getGame() {
        return game;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public double getCurrentSecond() {
        return currentSecond;
    }

    public int getTotalSeconds() {
        return totalSeconds;
    }

    @Override
    public void run() {

        if (System.currentTimeMillis() > end){
            game.nextRound();
            this.cancel();
            return;
        }

        updateCurrentSecond();
    }
}
