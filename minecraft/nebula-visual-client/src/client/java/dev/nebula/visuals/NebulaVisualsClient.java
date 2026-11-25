package dev.nebula.visuals;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class NebulaVisualsClient implements ClientModInitializer {
    private static boolean rightShiftWasDown;
    private static boolean f8WasDown;

    @Override
    public void onInitializeClient() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("nebula_visuals", "main_hud"),
                NebulaVisualsClient::renderHud
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            long window = client.getWindow().getWindow();
            boolean rightShift = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            boolean f8 = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_F8) == GLFW.GLFW_PRESS;

            if (((rightShift && !rightShiftWasDown) || (f8 && !f8WasDown)) && client.screen == null) {
                client.setScreen(new NebulaMenuScreen());
            }
            rightShiftWasDown = rightShift;
            f8WasDown = f8;
        });
    }

    private static void renderHud(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        int x = 8;
        int y = 8;
        int rows = 1;
        if (VisualSettings.showFps) rows++;
        if (VisualSettings.showCoordinates) rows++;
        if (VisualSettings.showClock) rows++;
        int height = 12 + rows * 12;

        if (VisualSettings.showWatermark) {
            graphics.fill(x, y, x + 142, y + height, 0xB5101424);
            graphics.fill(x, y, x + 2, y + height, 0xFF9B7BFF);
            graphics.text(client.font, "NEBULA VISUALS", x + 9, y + 5, 0xFFE9E2FF, true);
        }

        int lineY = y + 19;
        if (VisualSettings.showFps) {
            graphics.text(client.font, "FPS  " + client.getFps(), x + 9, lineY, 0xFFD6D0E8, true);
            lineY += 12;
        }
        if (VisualSettings.showCoordinates) {
            var pos = client.player.blockPosition();
            graphics.text(client.font, "XYZ  " + pos.getX() + " / " + pos.getY() + " / " + pos.getZ(),
                    x + 9, lineY, 0xFFD6D0E8, true);
            lineY += 12;
        }
        if (VisualSettings.showClock) {
            graphics.text(client.font, "TIME  " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                    x + 9, lineY, 0xFFD6D0E8, true);
        }
    }

    private static final class VisualSettings {
        private static boolean showWatermark = true;
        private static boolean showFps = true;
        private static boolean showCoordinates = false;
        private static boolean showClock = false;
    }

    private static final class NebulaMenuScreen extends Screen {
        private NebulaMenuScreen() {
            super(Component.literal("Nebula Visuals"));
        }

        @Override
        protected void init() {
            int left = this.width / 2 - 145;
            int top = this.height / 2 - 76;
            int gap = 8;
            int buttonWidth = 136;

            addRenderableWidget(toggleButton("Watermark", () -> VisualSettings.showWatermark,
                    value -> VisualSettings.showWatermark = value, left, top, buttonWidth));
            addRenderableWidget(toggleButton("FPS counter", () -> VisualSettings.showFps,
                    value -> VisualSettings.showFps = value, left + buttonWidth + gap, top, buttonWidth));
            addRenderableWidget(toggleButton("Coordinates", () -> VisualSettings.showCoordinates,
                    value -> VisualSettings.showCoordinates = value, left, top + 32, buttonWidth));
            addRenderableWidget(toggleButton("Clock", () -> VisualSettings.showClock,
                    value -> VisualSettings.showClock = value, left + buttonWidth + gap, top + 32, buttonWidth));

            addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
                    .bounds(this.width / 2 - 68, top + 86, 136, 20).build());
        }

        private Button toggleButton(String label, java.util.function.BooleanSupplier getter,
                                    java.util.function.Consumer<Boolean> setter, int x, int y, int width) {
            Button button = Button.builder(toggleLabel(label, getter.getAsBoolean()), ignored -> {
                boolean next = !getter.getAsBoolean();
                setter.accept(next);
                ignored.setMessage(toggleLabel(label, next));
            }).bounds(x, y, width, 22).build();
            return button;
        }

        private Component toggleLabel(String label, boolean enabled) {
            return Component.literal(label + ": " + (enabled ? "ON" : "OFF"));
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            super.extractRenderState(graphics, mouseX, mouseY, partialTick);
            int panelWidth = 318;
            int panelHeight = 188;
            int left = this.width / 2 - panelWidth / 2;
            int top = this.height / 2 - panelHeight / 2;
            graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xE8101424);
            graphics.fill(left, top, left + panelWidth, top + 2, 0xFF9B7BFF);
            graphics.text(this.font, "NEBULA VISUALS", left + 16, top + 14, 0xFFF0E9FF, true);
            graphics.text(this.font, "CLIENT HUD  /  VISUAL SETTINGS", left + 16, top + 29, 0xFFAAA2C3, true);
            graphics.text(this.font, "Toggle clean on-screen information widgets.", left + 16, top + 60,
                    0xFFD6D0E8, true);
            graphics.text(this.font, "Open: RIGHT SHIFT or F8", left + 16, top + 151, 0xFFAAA2C3, true);
            graphics.text(this.font, "Build " + Util.getMillis() / 1000L, left + panelWidth - 82,
                    top + 14, 0xFFAAA2C3, true);
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
