package me.zote.scr;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class ScreenTask extends BukkitRunnable {

    private List<ScreenFrame> frames;
    private int currentFrame = 0;
    private final Screen screen;
    private boolean paused;

    public ScreenTask(Screen screen) {
        this.screen = screen;
    }

    @Override
    public void run() {
        if (screen == null) {
            cancel();
            return;
        }

        if (paused)
            return;

        if (frames == null || frames.isEmpty())
            return;

        if (currentFrame >= frames.size())
            currentFrame = 0;

        frames.get(currentFrame++).display(screen);
    }

    public void setFrames(List<ScreenFrame> frames) {
        this.paused = frames.isEmpty();
        this.currentFrame = 0;
        this.frames = frames;
    }

    public void pause(boolean b) {
        paused = b;
    }

}
