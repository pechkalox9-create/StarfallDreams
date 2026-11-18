package dev.kocmoc.client;

import java.util.Comparator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;

final class HudRenderer {
    private static final int ACCENT = 0xFF9B75FF;
    private static final int PANEL = 0xB9161722;
    private static final int TEXT = 0xFFF2F0FA;
    private static final int MUTED = 0xFFAAA6B8;

    private HudRenderer() {
    }

    static void render(Minecraft client, GuiGraphics graphics) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        if (XxKocmocClient.isEnabled("Watermark")) {
            graphics.fill(7, 7, 112, 27, PANEL);
            graphics.fill(7, 7, 10, 27, ACCENT);
            graphics.drawString(client.font, "XxKOCMOCxX", 16, 10, TEXT, true);
            graphics.drawString(client.font, "1.21.11  /  FABRIC", 16, 19, MUTED, false);
        }

        if (XxKocmocClient.isEnabled("Coordinates")) {
            String coordinates = String.format("XYZ  %.1f  %.1f  %.1f",
                    player.getX(), player.getY(), player.getZ());
            int y = client.getWindow().getGuiScaledHeight() - 14;
            graphics.fill(7, y - 3, 7 + client.font.width(coordinates) + 12, y + 9, PANEL);
            graphics.drawString(client.font, coordinates, 13, y, TEXT, true);
        }

        if (XxKocmocClient.isEnabled("FPS")) {
            String fps = "FPS  " + client.getFps();
            graphics.fill(7, 31, 7 + client.font.width(fps) + 12, 45, PANEL);
            graphics.drawString(client.font, fps, 13, 34, TEXT, true);
        }

        if (XxKocmocClient.isEnabled("ActiveModules")) {
            List<Module> enabled = XxKocmocClient.modules().stream()
                    .filter(Module::enabled)
                    .filter(module -> !module.name().equals("ActiveModules"))
                    .sorted(Comparator.comparingInt((Module module) -> client.font.width(module.name())).reversed())
                    .toList();
            int y = 8;
            for (Module module : enabled) {
                int width = client.font.width(module.name()) + 15;
                int left = client.getWindow().getGuiScaledWidth() - width - 7;
                graphics.fill(left, y, left + width, y + 14, PANEL);
                graphics.fill(left + width - 2, y, left + width, y + 14, ACCENT);
                graphics.drawString(client.font, module.name(), left + 5, y + 3, TEXT, true);
                y += 16;
            }
        }

        if (XxKocmocClient.isEnabled("Crosshair")) {
            int centerX = client.getWindow().getGuiScaledWidth() / 2;
            int centerY = client.getWindow().getGuiScaledHeight() / 2;
            graphics.fill(centerX - 7, centerY, centerX - 2, centerY + 1, ACCENT);
            graphics.fill(centerX + 2, centerY, centerX + 7, centerY + 1, ACCENT);
            graphics.fill(centerX, centerY - 7, centerX + 1, centerY - 2, ACCENT);
            graphics.fill(centerX, centerY + 2, centerX + 1, centerY + 7, ACCENT);
        }
    }
}
