package dev.kocmoc.client;

import java.util.List;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class XxKocmocClient implements ClientModInitializer {
    private static final List<Module> MODULES = List.of(
            new Module("Sprint", Module.Type.MOVEMENT),
            new Module("FullBright", Module.Type.RENDER),
            new Module("Watermark", Module.Type.RENDER),
            new Module("Coordinates", Module.Type.RENDER),
            new Module("FPS", Module.Type.RENDER),
            new Module("ActiveModules", Module.Type.RENDER),
            new Module("Crosshair", Module.Type.RENDER)
    );

    private boolean rightShiftWasDown;

    public static List<Module> modules() {
        return MODULES;
    }

    public static boolean isEnabled(String name) {
        return MODULES.stream().anyMatch(module -> module.name().equals(name) && module.enabled());
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("xxkocmocx", "hud"),
                (graphics, deltaTracker) -> HudRenderer.render(Minecraft.getInstance(), graphics));
    }

    private void onEndTick(Minecraft client) {
        if (client.getWindow() == null) {
            return;
        }

        boolean rightShiftDown = GLFW.glfwGetKey(client.getWindow().handle(), GLFW.GLFW_KEY_RIGHT_SHIFT)
                == GLFW.GLFW_PRESS;
        if (rightShiftDown && !rightShiftWasDown && client.screen == null) {
            client.setScreen(new ClickGuiScreen());
        }
        rightShiftWasDown = rightShiftDown;

        for (Module module : MODULES) {
            module.tick(client);
        }
    }
}
