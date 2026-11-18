package dev.pasta.visual;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PastaVisualScreen extends Screen {
    private final Screen parent;
    private final String page;
    private static final String[] PAGES = {"Home", "Combat", "HUD", "Interface", "Browser", "Settings"};

    public PastaVisualScreen(Screen parent) { this(parent, "Home"); }

    private PastaVisualScreen(Screen parent, String page) {
        super(Component.literal("Pasta Visual"));
        this.parent = parent;
        this.page = page;
    }

    @Override
    protected void init() {
        int panelX = width / 2 - 225;
        int panelY = height / 2 - 155;
        int navX = panelX + 12;
        int contentX = panelX + 142;
        int contentY = panelY + 74;

        for (int i = 0; i < PAGES.length; i++) {
            String target = PAGES[i];
            addRenderableWidget(Button.builder(Component.literal((page.equals(target) ? "» " : "") + target),
                    b -> minecraft.setScreen(new PastaVisualScreen(parent, target)))
                    .bounds(navX, panelY + 62 + i * 31, 116, 25).build());
        }

        EditBox search = new EditBox(font, contentX, panelY + 39, 290, 20, Component.literal("Search modules"));
        search.setHint(Component.literal("Search modules..."));
        addRenderableWidget(search);

        if (page.equals("Combat")) {
            addToggle("Hit particles", () -> VisualSettings.hitParticles, v -> VisualSettings.hitParticles = v, contentX, contentY);
            addToggle("Critical burst", () -> VisualSettings.criticalBurst, v -> VisualSettings.criticalBurst = v, contentX, contentY + 31);
        } else if (page.equals("HUD")) {
            addToggle("HUD watermark", () -> VisualSettings.showHud, v -> VisualSettings.showHud = v, contentX, contentY);
            addToggle("Coordinates", () -> VisualSettings.showCoordinates, v -> VisualSettings.showCoordinates = v, contentX, contentY + 31);
            addToggle("FPS counter", () -> VisualSettings.showFps, v -> VisualSettings.showFps = v, contentX, contentY + 62);
            addToggle("Floating quick panel", () -> VisualSettings.showFloatingPanel, v -> VisualSettings.showFloatingPanel = v, contentX, contentY + 93);
        } else if (page.equals("Interface")) {
            addToggle("Main menu branding", () -> VisualSettings.menuBranding, v -> VisualSettings.menuBranding = v, contentX, contentY);
            addToggle("Compact layout", () -> VisualSettings.compactMenu, v -> VisualSettings.compactMenu = v, contentX, contentY + 31);
        } else if (page.equals("Settings")) {
            addRenderableWidget(Button.builder(Component.literal("Save configuration"), b -> VisualSettings.save())
                    .bounds(contentX, contentY, 140, 22).build());
            addRenderableWidget(Button.builder(Component.literal("Reset visual settings"), b -> {
                VisualSettings.reset();
                minecraft.setScreen(new PastaVisualScreen(parent, "Settings"));
            }).bounds(contentX + 148, contentY, 142, 22).build());
        } else if (page.equals("Browser")) {
            addRenderableWidget(Button.builder(Component.literal("Browser compatibility info"), b -> {})
                    .bounds(contentX, contentY + 82, 210, 22).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(panelX + 322, panelY + 274, 110, 22).build());
    }

    private void addToggle(String label, java.util.function.Supplier<Boolean> getter,
                           java.util.function.Consumer<Boolean> setter, int x, int y) {
        addRenderableWidget(Button.builder(toggleText(label, getter.get()), button -> {
            setter.accept(!getter.get());
            button.setMessage(toggleText(label, getter.get()));
            VisualSettings.save();
        }).bounds(x, y, 290, 24).build());
    }

    private Component toggleText(String label, boolean enabled) {
        return Component.literal(label + "     " + (enabled ? "§aON" : "§7OFF"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Deliberately avoid Minecraft's blur background: some mobile OpenGL renderers
        // cannot safely apply the blur more than once per frame.
        graphics.fill(0, 0, width, height, 0xA6000000);
        int x = width / 2 - 225;
        int y = height / 2 - 155;
        graphics.fill(x - 1, y - 1, x + 451, y + 311, 0xFFFF9857);
        graphics.fill(x, y, x + 449, y + 309, 0xF00D111B);
        graphics.fill(x, y, x + 449, y + 3, 0xFFFF9857);
        graphics.fill(x + 132, y + 52, x + 133, y + 264, 0xFF30394A);
        graphics.drawString(font, "PASTA VISUAL", x + 14, y + 13, 0xFFFFB477, false);
        graphics.drawString(font, "CLIENT  /  1.21.11  /  v1.1.0", x + 14, y + 28, 0xFF9CA9BD, false);
        graphics.drawString(font, page.toUpperCase(), x + 144, y + 14, 0xFFF2F4FA, false);
        graphics.drawString(font, "Find a module or choose a category", x + 144, y + 27, 0xFF8F9CB1, false);

        if (page.equals("Home")) {
            graphics.drawString(font, "Welcome back.", x + 145, y + 91, 0xFFFFB477, false);
            graphics.drawString(font, "Your visuals. Your world. Your style.", x + 145, y + 109, 0xFFD5DCE8, false);
            graphics.drawString(font, "Choose a category on the left to configure modules.", x + 145, y + 139, 0xFF9CA9BD, false);
            graphics.drawString(font, "Quick panel: toggle it in the HUD tab, then keep", x + 145, y + 169, 0xFF9CA9BD, false);
            graphics.drawString(font, "its compact status visible while you explore.", x + 145, y + 183, 0xFF9CA9BD, false);
        } else if (page.equals("Browser")) {
            graphics.drawString(font, "IN-GAME BROWSER", x + 145, y + 91, 0xFFFFB477, false);
            graphics.drawString(font, "A real Chromium view needs an optional browser engine", x + 145, y + 113, 0xFFD5DCE8, false);
            graphics.drawString(font, "such as MCEF/Rinku. It is not bundled in this build:", x + 145, y + 129, 0xFFD5DCE8, false);
            graphics.drawString(font, "native Chromium binaries are not safe to assume on", x + 145, y + 145, 0xFF9CA9BD, false);
            graphics.drawString(font, "Android launchers. This page is reserved for the", x + 145, y + 161, 0xFF9CA9BD, false);
            graphics.drawString(font, "optional provider integration.", x + 145, y + 177, 0xFF9CA9BD, false);
        } else if (page.equals("Settings")) {
            graphics.drawString(font, "Configuration is stored locally.", x + 145, y + 116, 0xFFD5DCE8, false);
            graphics.drawString(font, "Changes are saved automatically when toggled.", x + 145, y + 134, 0xFF9CA9BD, false);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(font, "RIGHT SHIFT  •  OPEN MENU", x + 14, y + 286, 0xFF8492A8, false);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        VisualSettings.save();
        minecraft.setScreen(parent);
    }
}
