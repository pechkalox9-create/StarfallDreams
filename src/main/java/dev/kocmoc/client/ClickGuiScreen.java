package dev.kocmoc.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ClickGuiScreen extends Screen {
    private static final int PANEL_WIDTH = 220;

    public ClickGuiScreen() {
        super(Component.literal("XxKOCMOCxX"));
    }

    @Override
    protected void init() {
        int left = (width - PANEL_WIDTH) / 2;
        int top = height / 2 - 70;
        int row = 0;

        for (Module module : XxKocmocClient.modules()) {
            int y = top + 34 + row * 26;
            addRenderableWidget(Button.builder(moduleLabel(module), button -> {
                module.toggle();
                button.setMessage(moduleLabel(module));
            }).bounds(left + 10, y, PANEL_WIDTH - 20, 20).build());
            row++;
        }

        addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
                .bounds(left + 10, top + 42 + row * 26, PANEL_WIDTH - 20, 20)
                .build());
    }

    private static Component moduleLabel(Module module) {
        return Component.literal(module.name() + ": " + (module.enabled() ? "ON" : "OFF"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        int left = (width - PANEL_WIDTH) / 2;
        int top = height / 2 - 70;
        graphics.fill(left, top, left + PANEL_WIDTH, top + 48 + XxKocmocClient.modules().size() * 26,
                0xE91B1B27);
        graphics.drawCenteredString(font, "XxKOCMOCxX", width / 2, top + 10, 0xFF8B5CF6);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
