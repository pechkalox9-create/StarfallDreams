package dev.starfall.explosions.client.screen;
import dev.starfall.explosions.client.fx.CosmicEffects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class ControllerScreen extends Screen {
    public ControllerScreen() { super(Component.literal("STARFALL // ORBITAL CONSOLE")); }
    @Override protected void init() {
        int left = (width - 250) / 2, top = height / 2 - 66;
        addPreset(left, top, "01  NOVA PULSE", CosmicEffects.Preset.NOVA);
        addPreset(left, top + 27, "02  AURORA VEIL", CosmicEffects.Preset.AURORA);
        addPreset(left, top + 54, "03  STARFALL", CosmicEffects.Preset.STARFALL);
        addPreset(left, top + 81, "04  VOID RIPPLE", CosmicEffects.Preset.VOID_RIPPLE);
        addRenderableWidget(Button.builder(Component.literal("DISCONNECT"), button -> onClose())
            .bounds(left, top + 116, 250, 20).build());
    }
    private void addPreset(int x, int y, String label, CosmicEffects.Preset preset) {
        addRenderableWidget(Button.builder(Component.literal(label), button -> CosmicEffects.trigger(preset))
            .bounds(x, y, 250, 20).build());
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xD9081020);
        int left = width / 2 - 140, top = height / 2 - 91;
        graphics.fill(left, top, left + 280, top + 164, 0xEE101B32);
        graphics.fill(left, top, left + 280, top + 2, 0xFF48D8FF);
        graphics.drawCenteredString(font, title, width / 2, top + 12, 0xFFB9F3FF);
        graphics.drawCenteredString(font, Component.literal("VISUAL SIMULATION // BLOCK DAMAGE: OFF"),
            width / 2, top + 31, 0xFF829BB7);
        super.render(graphics, mouseX, mouseY, delta);
    }
    @Override public boolean isPauseScreen() { return false; }
}
