package me.zote.scr;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Screen {

    private final List<ArmorStand> components = Lists.newLinkedList();
    public static final int width = 128;
    public static final int height = 72;

    public void turnOn(Location loc) {
        double maxY = 0.2 * height;
        Location spawn = loc.add(0, maxY, 0);

        for (int y = 0; y < height; y++) {
            spawn.getWorld().spawn(spawn, ArmorStand.class, as -> {
                as.setGravity(false);
                as.setVisible(false);
                as.setCustomNameVisible(true);
                as.setCustomName(asLegacy(Component.text(" ")));
                components.add(as);
            });
            spawn.subtract(0, 0.2, 0);
        }

        clear();
    }

    public void turnOff() {
        components.forEach(Entity::remove);
        components.clear();
    }

    public void clear() {
        for (ArmorStand as : components) {
            TextComponent.Builder name = Component.text();
            for (int i = 0; i < width; i++) {
                Component pixel = Component.text('\u2588').color(NamedTextColor.BLACK);
                name.append(pixel);
            }
            String legacy = Screen.asLegacy(name.build());
            as.setCustomName(legacy);
        }
    }

    public List<ArmorStand> components() {
        return components;
    }

    public static String asLegacy(Component component) {
        return LegacyComponentSerializer
                .builder()
                .character('\u00A7')
                .useUnusualXRepeatedCharacterHexFormat()
                .hexColors()
                .build()
                .serialize(component);
    }

}
