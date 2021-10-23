package me.zote.scr;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ScreenPlugin extends JavaPlugin implements Listener, CommandExecutor, TabExecutor {

    private final Map<String, List<ScreenFrame>> frames = Maps.newHashMap();
    private ScreenTask screenTask;
    private Screen screen;
    private Location loc;

    public void onEnable() {

        for (File file : getDataFolder().listFiles()) {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".gif")) {
                name = name.substring(0, name.lastIndexOf('.'));
                String finalName = name;
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return splitGif(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).thenAccept(list -> {
                    if (list != null)
                        frames.put(finalName, list);
                });
            }
        }

        PluginCommand command = getCommand("display");

        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }

        getServer().getPluginManager().registerEvents(this, this);

    }

    public void onDisable() {
        if (screen != null) {
            screen.turnOff();
        }

        if (screenTask != null && !screenTask.isCancelled()) {
            screenTask.cancel();
        }
    }

    private List<ScreenFrame> splitGif(File file) throws IOException {
        GifDecoder.GifImage decoder = GifDecoder.read(new FileInputStream(file));
        int size = decoder.getFrameCount();
        BufferedImage lastImage = decoder.getFrame(0);

        List<ScreenFrame> frames = Lists.newLinkedList();
        frames.add(new ScreenFrame((lastImage)));

        for (int i = 1; i < size; i++) {
            BufferedImage image = makeImageForIndex(decoder.getFrame(i), lastImage);
            frames.add(new ScreenFrame(image));
        }

        return frames;
    }

    private BufferedImage makeImageForIndex(BufferedImage next, BufferedImage lastImage) {
        BufferedImage newImage = new BufferedImage(next.getWidth(), next.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

        if (lastImage != null) {
            newImage.getGraphics().drawImage(lastImage, 0, 0, null);
        }
        newImage.getGraphics().drawImage(next, 0, 0, null);
        return newImage;
    }

    private Location getLoc(CommandSender sender) {
        if (sender instanceof Player)
            return ((Player) sender).getLocation();
        if (sender instanceof BlockCommandSender)
            return ((BlockCommandSender) sender).getBlock().getLocation();
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (screen == null) {

            if (loc == null)
                loc = getLoc(sender);

            screen = new Screen();
            screen.turnOn(loc);
            screenTask = new ScreenTask(screen);
            screenTask.runTaskTimer(this, 0, 2L);
        }

        if (args.length < 1) {
            screenTask.pause(true);
            screen.clear();
            return false;
        }

        String name = args[0];

        switch (name.toLowerCase()) {
            case "pause" -> screenTask.pause(true);
            case "stop", "off" -> {
                screenTask.cancel();
                screen.turnOff();
                screenTask = null;
                screen = null;
            }
            case "resume", "start" -> screenTask.pause(false);
            case "play" -> {
                if (args.length == 2) {
                    screenTask.setFrames(frames.getOrDefault(args[1], Lists.newArrayList()));
                } else {
                    sender.sendMessage("Usage: /display play <name>");
                }
            }
        }

        return true;
    }

    @Override
    public @Nullable
    List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String string, @NotNull String[] args) {
        int offset = args.length;

        if (offset == 1) {
            return Lists.newArrayList("start", "pause", "stop", "play");
        } else if (offset == 2) {
            String sub = args[0];
            if (sub.equalsIgnoreCase("play")) {
                return Lists.newArrayList(frames.keySet());
            }
        }

        return Lists.newArrayList();
    }
}
