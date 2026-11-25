package dev.starfall.explosions.client;
import dev.starfall.explosions.client.fx.CosmicEffects;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
public final class StarfallExplosionsClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(CosmicEffects::tick);
    }
}
