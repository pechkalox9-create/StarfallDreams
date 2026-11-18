package dev.pasta.visual;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class VisualSettings {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("pasta-visual.properties");
    public static boolean hitParticles = true;
    public static boolean criticalBurst = true;
    public static boolean menuBranding = true;
    public static boolean compactMenu = false;

    private VisualSettings() {}

    public static void load() {
        if (!Files.exists(FILE)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
            hitParticles = Boolean.parseBoolean(p.getProperty("hitParticles", "true"));
            criticalBurst = Boolean.parseBoolean(p.getProperty("criticalBurst", "true"));
            menuBranding = Boolean.parseBoolean(p.getProperty("menuBranding", "true"));
            compactMenu = Boolean.parseBoolean(p.getProperty("compactMenu", "false"));
        } catch (IOException ignored) {
        }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("hitParticles", Boolean.toString(hitParticles));
        p.setProperty("criticalBurst", Boolean.toString(criticalBurst));
        p.setProperty("menuBranding", Boolean.toString(menuBranding));
        p.setProperty("compactMenu", Boolean.toString(compactMenu));
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                p.store(out, "Pasta Visual settings");
            }
        } catch (IOException ignored) {
        }
    }
}
