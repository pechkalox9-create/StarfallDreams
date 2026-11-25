package dev.starfall.explosions;
import dev.starfall.explosions.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.item.ItemStack;
public final class StarfallExplosions implements ModInitializer {
    public static final String MOD_ID = "starfall_explosions";
    @Override public void onInitialize() {
        ModItems.initialize();
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.player;
            if (!player.getInventory().contains(new ItemStack(ModItems.STARFALL_CONTROLLER)))
                player.getInventory().add(new ItemStack(ModItems.STARFALL_CONTROLLER));
        });
    }
}
