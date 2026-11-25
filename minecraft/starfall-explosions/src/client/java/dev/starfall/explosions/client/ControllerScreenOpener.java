package dev.starfall.explosions.client;
import dev.starfall.explosions.client.screen.ControllerScreen;
import net.minecraft.client.Minecraft;
public final class ControllerScreenOpener {
    private ControllerScreenOpener() {}
    public static void open() { Minecraft.getInstance().setScreen(new ControllerScreen()); }
}
