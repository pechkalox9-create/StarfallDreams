package dev.pasta.visual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionResult;
import net.minecraft.client.multiplayer.ClientLevel;
import org.lwjgl.glfw.GLFW;

public final class PastaVisualClient implements ClientModInitializer {
    private static KeyMapping openMenu;

    @Override
    public void onInitializeClient() {
        VisualSettings.load();
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.pasta_visual.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.pasta_visual"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.consumeClick()) {
                client.setScreen(new PastaVisualScreen(client.screen));
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> VisualSettings.save());

        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof TitleScreen) {
                screen.addRenderableWidget(Button.builder(
                        Component.literal("✦  PASTA VISUAL"),
                        button -> client.setScreen(new PastaVisualScreen(screen))
                ).bounds(width / 2 - 100, height - 48, 200, 20).build());
                ScreenEvents.afterRender(screen).register((renderedScreen, graphics, mouseX, mouseY, tickDelta) -> {
                    if (!VisualSettings.menuBranding) return;
                    graphics.fill(12, 12, 194, 66, 0xC9101420);
                    graphics.fill(12, 12, 15, 66, 0xFFFF8A45);
                    graphics.drawString(Minecraft.getInstance().font, "PASTA VISUAL", 25, 23, 0xFFFFB477, false);
                    graphics.drawString(Minecraft.getInstance().font, "VISUAL CLIENT  •  1.21.11", 25, 39, 0xFFD0D6E4, false);
                    graphics.drawString(Minecraft.getInstance().font, "RIGHT SHIFT  /  MENU", 25, 52, 0xFF8E9AAF, false);
                });
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() || !(entity instanceof LivingEntity target)) return InteractionResult.PASS;
            if (VisualSettings.hitParticles) {
                ClientLevel level = (ClientLevel) world;
                Vec3 pos = target.position().add(0, target.getBbHeight() * 0.55, 0);
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI * 2.0 * i / 8.0;
                    level.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z,
                            Math.cos(a) * 0.055, 0.025 + (i % 3) * 0.018, Math.sin(a) * 0.055);
                }
            }
            if (VisualSettings.criticalBurst && !player.onGround()
                    && player.fallDistance > 0.0F && !player.isInWater()) {
                ClientLevel level = (ClientLevel) world;
                Vec3 pos = target.position().add(0, target.getBbHeight() * 0.65, 0);
                for (int i = 0; i < 14; i++) {
                    double a = Math.PI * 2.0 * i / 14.0;
                    level.addParticle(ParticleTypes.CRIT, pos.x, pos.y, pos.z,
                            Math.cos(a) * 0.10, 0.04, Math.sin(a) * 0.10);
                }
            }
            return InteractionResult.PASS;
        });
    }
}
