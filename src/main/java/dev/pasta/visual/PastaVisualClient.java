package dev.pasta.visual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionResult;
import net.minecraft.client.multiplayer.ClientLevel;
import org.lwjgl.glfw.GLFW;

public final class PastaVisualClient implements ClientModInitializer {
    private static KeyMapping openMenu;
    private static KeyMapping toggleFloating;
    private static KeyMapping toggleHitFx;
    private static KeyMapping toggleCritFx;

    @Override
    public void onInitializeClient() {
        VisualSettings.load();
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("pasta_visual", "controls"));
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.pasta_visual.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        toggleFloating = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.pasta_visual.floating", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, category));
        toggleHitFx = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.pasta_visual.hitfx", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, category));
        toggleCritFx = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.pasta_visual.critfx", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.consumeClick()) client.setScreen(new PastaVisualScreen(client.screen));
            while (toggleFloating.consumeClick()) {
                VisualSettings.showFloatingPanel = !VisualSettings.showFloatingPanel;
                VisualSettings.save();
            }
            while (toggleHitFx.consumeClick()) {
                VisualSettings.hitParticles = !VisualSettings.hitParticles;
                VisualSettings.save();
            }
            while (toggleCritFx.consumeClick()) {
                VisualSettings.criticalBurst = !VisualSettings.criticalBurst;
                VisualSettings.save();
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> VisualSettings.save());

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("pasta_visual", "watermark"), (graphics, deltaTracker) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;

            if (VisualSettings.showHud) {
                int height = VisualSettings.showCoordinates ? 39 : 27;
                graphics.fill(8, 8, 142, height, 0xB8101420);
                graphics.fill(8, 8, 10, height, 0xFFFF9857);
                graphics.drawString(client.font, "PASTA VISUAL", 16, 13, 0xFFFFB477, false);
                if (VisualSettings.showCoordinates) {
                    var pos = client.player.blockPosition();
                    graphics.drawString(client.font, "XYZ  " + pos.getX() + "  " + pos.getY() + "  " + pos.getZ(),
                            16, 26, 0xFFD0D6E4, false);
                }
                if (VisualSettings.showFps) {
                    graphics.drawString(client.font, "FPS  " + client.getFps(), 16, height + 3, 0xFF9EACC0, false);
                }
            }

            if (VisualSettings.showFloatingPanel) {
                int x = 8;
                int y = client.getWindow().getGuiScaledHeight() - 54;
                graphics.fill(x, y, x + 178, y + 46, 0xD90D111B);
                graphics.fill(x, y, x + 178, y + 2, 0xFFFF9857);
                graphics.drawString(client.font, "PASTA QUICK PANEL", x + 8, y + 8, 0xFFFFB477, false);
                graphics.drawString(client.font, "Hit FX J: " + state(VisualSettings.hitParticles)
                        + "   Crit K: " + state(VisualSettings.criticalBurst), x + 8, y + 22, 0xFFD5DCE8, false);
                graphics.drawString(client.font, "Right Shift: GUI   H: hide panel", x + 8, y + 34, 0xFF8997AC, false);
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof TitleScreen) {
                ScreenEvents.afterRender(screen).register((renderedScreen, graphics, mouseX, mouseY, tickDelta) -> {
                    if (!VisualSettings.menuBranding) return;
                    graphics.fill(12, 12, 204, 68, 0xC9101420);
                    graphics.fill(12, 12, 15, 68, 0xFFFF9857);
                    graphics.drawString(Minecraft.getInstance().font, "PASTA VISUAL", 25, 22, 0xFFFFB477, false);
                    graphics.drawString(Minecraft.getInstance().font, "VISUAL CLIENT  •  1.1.0", 25, 38, 0xFFD0D6E4, false);
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

    private static String state(boolean enabled) { return enabled ? "ON" : "OFF"; }
}
