package dev.kocmoc.client;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class ClickGuiScreen extends Screen {
    private static final int PANEL_WIDTH = 520;
    private static final int PANEL_HEIGHT = 310;
    private static final int SIDEBAR_WIDTH = 126;
    private Module.Type selectedCategory = Module.Type.RENDER;

    public ClickGuiScreen() {
        super(Component.literal("XxKOCMOCxX"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Draw a dim overlay directly. Screen.renderBackground applies blur, which
        // may already have been applied by Minecraft earlier in this frame.
        graphics.fill(0, 0, this.width, this.height, 0x99000000);

        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        int right = left + PANEL_WIDTH;
        int bottom = top + PANEL_HEIGHT;

        graphics.fill(left - 1, top - 1, right + 1, bottom + 1, 0xFF9B75FF);
        graphics.fill(left, top, right, bottom, 0xF014151E);
        graphics.fill(left, top, right, top + 48, 0xFF1B1C28);
        graphics.fill(left, top + 47, right, top + 48, 0xFF343044);
        graphics.fill(left, top + 48, left + SIDEBAR_WIDTH, bottom, 0xFF191A24);

        graphics.drawString(font, "XxKOCMOCxX", left + 16, top + 10, 0xFFFFFFFF, true);
        graphics.drawString(font, "CLIENT  /  1.21.11", left + 16, top + 26, 0xFFAAA6B8, false);
        graphics.drawString(font, "RIGHT SHIFT  •  CLOSE", right - 143, top + 19, 0xFFAAA6B8, false);

        int categoryY = top + 65;
        for (Module.Type category : Module.Type.values()) {
            int y = categoryY;
            boolean selected = category == selectedCategory;
            if (selected) {
                graphics.fill(left + 9, y - 5, left + SIDEBAR_WIDTH - 9, y + 17, 0xFF30283F);
                graphics.fill(left + 9, y - 5, left + 12, y + 17, 0xFF9B75FF);
            }
            graphics.drawString(font, category.name(), left + 20, y + 2,
                    selected ? 0xFFFFFFFF : 0xFFAAA6B8, selected);
            categoryY += 31;
        }

        int contentLeft = left + SIDEBAR_WIDTH + 18;
        graphics.drawString(font, selectedCategory.name() + " MODULES", contentLeft, top + 64,
                0xFFB99FFF, true);
        graphics.drawString(font, "Toggle a module to enable or disable it.", contentLeft, top + 78,
                0xFF8E8A9B, false);

        List<Module> modules = XxKocmocClient.modules().stream()
                .filter(module -> module.type() == selectedCategory)
                .toList();
        int rowY = top + 105;
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int column = i % 2;
            int row = i / 2;
            int x = contentLeft + column * 184;
            int y = rowY + row * 54;
            int cardWidth = 170;
            boolean hovered = mouseX >= x && mouseX <= x + cardWidth
                    && mouseY >= y && mouseY <= y + 42;
            graphics.fill(x, y, x + cardWidth, y + 42, hovered ? 0xFF292A39 : 0xFF22232F);
            graphics.fill(x, y, x + 2, y + 42, module.enabled() ? 0xFF9B75FF : 0xFF4A4856);
            graphics.drawString(font, module.name(), x + 11, y + 9, 0xFFF2F0FA, true);
            String state = module.enabled() ? "ON" : "OFF";
            int stateColor = module.enabled() ? 0xFFB99FFF : 0xFF858191;
            graphics.drawString(font, state, x + 11, y + 24, stateColor, false);
            int toggleLeft = x + cardWidth - 34;
            graphics.fill(toggleLeft, y + 14, toggleLeft + 23, y + 27,
                    module.enabled() ? 0xFF7655B5 : 0xFF484653);
            graphics.fill(module.enabled() ? toggleLeft + 12 : toggleLeft + 2,
                    y + 16, module.enabled() ? toggleLeft + 21 : toggleLeft + 11,
                    y + 25, 0xFFFFFFFF);
        }

        graphics.drawString(font, "KOCMOC  •  CLIENT-SIDE MODULES", left + 16, bottom - 17,
                0xFF777384, false);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        double mouseX = event.x();
        double mouseY = event.y();
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        int categoryY = top + 65;
        for (Module.Type category : Module.Type.values()) {
            if (mouseX >= left + 9 && mouseX <= left + SIDEBAR_WIDTH - 9
                    && mouseY >= categoryY - 5 && mouseY <= categoryY + 17) {
                selectedCategory = category;
                return true;
            }
            categoryY += 31;
        }

        int contentLeft = left + SIDEBAR_WIDTH + 18;
        int rowY = top + 105;
        List<Module> modules = XxKocmocClient.modules().stream()
                .filter(module -> module.type() == selectedCategory)
                .toList();
        for (int i = 0; i < modules.size(); i++) {
            int x = contentLeft + (i % 2) * 184;
            int y = rowY + (i / 2) * 54;
            if (mouseX >= x && mouseX <= x + 170 && mouseY >= y && mouseY <= y + 42) {
                modules.get(i).toggle();
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
