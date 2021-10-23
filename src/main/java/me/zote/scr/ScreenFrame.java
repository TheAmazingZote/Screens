package me.zote.scr;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.entity.ArmorStand;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class ScreenFrame {

    private final List<String> lines = Lists.newLinkedList();

    public ScreenFrame(BufferedImage image) {
        scan(image);
    }

    public void display(Screen screen) {
        List<ArmorStand> components = screen.components();

        for (int i = 0; i < components.size(); i++) {
            String line = lines.get(i);
            ((CraftArmorStand) components.get(i)).getHandle().setCustomName(CraftChatMessage.fromStringOrNull(line));
        }

    }

    private void scan(BufferedImage image) {
        image = resize(image);

        int row = 0;
        int column = 0;
        int boundX = image.getWidth();
        int boundY = image.getHeight();
        try {
            for (int y = 0; y < boundY; y++) {
                row = y;
                TextComponent.Builder name = Component.text();
                for (int x = 0; x < boundX; x++) {
                    Component pixel = Component.text('\u2588').color(color(image.getRGB(x, y)));
                    name.append(pixel);
                    column = x;
                }
                String legacy = Screen.asLegacy(name.build());
                lines.add(legacy);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.printf("Coords x=%d y=%d", column, row);
        }
    }

    private BufferedImage resize(BufferedImage inputImage) {
        BufferedImage outputImage = new BufferedImage(Screen.width, Screen.height, inputImage.getType());

        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, Screen.width, Screen.height, null);
        g2d.dispose();

        return outputImage;
    }

    private TextColor color(int rgb) {
        Color color = new Color(rgb, true);
        if (color.getAlpha() == 0)
            return NamedTextColor.BLACK;
        return TextColor.color(color.getRed(), color.getGreen(), color.getBlue());
    }

}
