package dev.pasta.visual;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PastaVisualScreen extends Screen {
    private final Screen parent;

    public PastaVisualScreen(Screen parent) {
        super(Component.literal("Pasta Visual"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 150;
        int top = this.height / 2 - 104;
        int gap = 25;
        addRenderableWidget(toggle("Hit particles", () -> VisualSettings.hitParticles,
                value -> VisualSettings.hitParticles = value, left, top + gap));
        addRenderableWidget(toggle("Critical burst", () -> VisualSettings.criticalBurst,
                value -> VisualSettings.criticalBurst = value, left, top + gap * 2));
        addRenderableWidget(toggle("Main menu branding", () -> VisualSettings.menuBranding,
                value -> VisualSettings.menuBranding = value, left, top + gap * 3));
        addRenderableWidget(toggle("Compact menu", () -> VisualSettings.compactMenu,
                value -> VisualSettings.compactMenu = value, left, top + gap * 4));
        addRenderableWidget(Button.builder(Component.literal("Save settings"), b -> VisualSettings.save())
                .bounds(left, top + gap * 6, 145, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
                .bounds(left + 155, top + gap * 6, 145, 20).build());
    }

    private Button toggle(String label, java.util.function.Supplier<Boolean> getter,
                          java.util.function.Consumer<Boolean> setter, int x, int y) {
        return Button.builder(toggleText(label, getter.get()), button -> {
            setter.accept(!getter.get());
            button.setMessage(toggleText(label, getter.get()));
            VisualSettings.save();
        }).bounds(x, y, 300, 20).build();
    }

    private Component toggleText(String label, boolean enabled) {
        return Component.literal(label + "   " + (enabled ? "§aON" : "§cOFF"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int panelWidth = VisualSettings.compactMenu ? 330 : 380;
        int panelHeight = 250;
        int x = width / 2 - panelWidth / 2;
        int y = height / 2 - panelHeight / 2;
        graphics.fill(x - 1, y - 1, x + panelWidth + 1, y + panelHeight + 1, 0xFFFF8A45);
        graphics.fill(x, y, x + panelWidth, y + panelHeight, 0xF0111520);
        graphics.fill(x, y, x + panelWidth, y + 2, 0xFFFF8A45);
        graphics.drawString(font, "PASTA VISUAL", x + 16, y + 14, 0xFFFFB477, false);
        graphics.drawString(font, "VISUAL SETTINGS  /  FABRIC 1.21.11", x + 16, y + 29, 0xFF9AA8BC, false);
        graphics.drawString(font, "COMBAT & INTERFACE", x + 16, y + 56, 0xFFE9EDF5, false);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(font, "Changes are client-side. Press Right Shift to reopen.", x + 16, y + panelHeight - 18, 0xFF7F8DA3, false);
    }

    @Override
    public void onClose() {
        VisualSettings.save();
        minecraft.setScreen(parent);
    }
}
