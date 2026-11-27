package ru.starfalldreams.vasya;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class VasyaSkillsScreen extends Screen {
    public VasyaSkillsScreen() { super(Text.literal("Навыки Васи")); }

    @Override protected void init() {
        int x = width / 2 - 100, y = height / 2 - 55;
        String[] skills = {"gathering", "mining", "foraging", "cooking", "defense"};
        String[] labels = {"Сбор ресурсов", "Добыча", "Поиск еды", "Приготовление", "Осторожность"};
        for (int i = 0; i < skills.length; i++) {
            final String skill = skills[i];
            addDrawableChild(ButtonWidget.builder(Text.literal("Улучшить: " + labels[i]), b -> {
                if (client != null && client.getNetworkHandler() != null)
                    client.getNetworkHandler().sendChatCommand("vasya skill " + skill);
            }).dimensions(x, y + i * 22, 200, 20).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Показать прогресс"), b -> {
            if (client != null && client.getNetworkHandler() != null)
                client.getNetworkHandler().sendChatCommand("vasya status");
        }).dimensions(x, y + 112, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), b -> close())
            .dimensions(x, y + 136, 200, 20).build());
    }

    @Override public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, "Навыки растут от наблюдений и практики", width / 2, height / 2 - 80, 0xFFFFFF);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override public boolean shouldPause() { return false; }
}
