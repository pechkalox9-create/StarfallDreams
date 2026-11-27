package ru.starfalldreams.vasya;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class VasyaDialogScreen extends Screen {
    public VasyaDialogScreen() { super(Text.literal("Разговор с Васей")); }
    @Override protected void init() {
        int x = width / 2 - 100, y = height / 2 - 45;
        addDrawableChild(ButtonWidget.builder(Text.literal("Как дела, Вася?"), b -> ask("привет"))
            .dimensions(x, y, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Как добывать ресурсы?"), b -> ask("как добывать ресурсы"))
            .dimensions(x, y + 25, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Где найти еду?"), b -> ask("где найти еду"))
            .dimensions(x, y + 50, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), b -> close())
            .dimensions(x, y + 80, 200, 20).build());
    }
    private void ask(String q) {
        if (client != null && client.getNetworkHandler() != null) {
            client.getNetworkHandler().sendChatCommand("vasya ask " + q);
        }
        close();
    }
    @Override public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, "Вася: Мяу! Чему сегодня учимся?", width / 2, height / 2 - 75, 0xFFFFFF);
        super.render(ctx, mouseX, mouseY, delta);
    }
    @Override public boolean shouldPause() { return false; }
}