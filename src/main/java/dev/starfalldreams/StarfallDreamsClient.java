package dev.starfalldreams;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class StarfallDreamsClient implements ClientModInitializer {
    private static final RandomSource RANDOM = RandomSource.create();
    private static final List<Meteor> METEORS = new ArrayList<>();
    private static long ticks;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));
    }

    private static void tick(Minecraft client) {
        ClientLevel level = client.level;
        if (level == null || client.player == null || client.isPaused()) {
            return;
        }

        ticks++;
        long dayTime = level.getDayTime() % 24000L;
        boolean night = dayTime >= 12500L && dayTime <= 23500L;
        if (!night) {
            METEORS.clear();
            return;
        }

        // A meteor is a tiny animated flight path, not a static screen overlay.
        if (ticks % 95L == 0L && RANDOM.nextFloat() < 0.72f) {
            spawnMeteor(client);
        }

        Iterator<Meteor> iterator = METEORS.iterator();
        while (iterator.hasNext()) {
            Meteor meteor = iterator.next();
            meteor.age++;
            meteor.position = meteor.position.add(meteor.velocity);

            // Layered particles create a soft, shimmering tail with a warm core.
            level.addParticle(ParticleTypes.END_ROD,
                    meteor.position.x, meteor.position.y, meteor.position.z,
                    -meteor.velocity.x * 0.12, -meteor.velocity.y * 0.12, -meteor.velocity.z * 0.12);

            if (meteor.age % 2 == 0) {
                level.addParticle(meteor.accent,
                        meteor.position.x, meteor.position.y, meteor.position.z,
                        0.0, 0.015, 0.0);
            }

            if (meteor.age % 5 == 0) {
                level.addParticle(ParticleTypes.FIREWORK,
                        meteor.position.x, meteor.position.y, meteor.position.z,
                        0.0, 0.0, 0.0);
            }

            if (meteor.age > meteor.lifetime) {
                if (meteor.burst) {
                    for (int i = 0; i < 18; i++) {
                        double angle = RANDOM.nextDouble() * Math.PI * 2.0;
                        double speed = 0.025 + RANDOM.nextDouble() * 0.075;
                        level.addParticle(ParticleTypes.END_ROD,
                                meteor.position.x, meteor.position.y, meteor.position.z,
                                Math.cos(angle) * speed, (RANDOM.nextDouble() - 0.5) * speed,
                                Math.sin(angle) * speed);
                    }
                }
                iterator.remove();
            }
        }
    }

    private static void spawnMeteor(Minecraft client) {
        Vec3 player = client.player.position();
        double angle = RANDOM.nextDouble() * Math.PI * 2.0;
        double distance = 18.0 + RANDOM.nextDouble() * 30.0;
        double x = player.x + Math.cos(angle) * distance;
        double z = player.z + Math.sin(angle) * distance;
        double y = player.y + 17.0 + RANDOM.nextDouble() * 24.0;

        // Each flight gets its own speed, direction, palette and lifetime.
        Vec3 velocity = new Vec3(
                (RANDOM.nextDouble() - 0.5) * 0.12,
                -(0.16 + RANDOM.nextDouble() * 0.13),
                (RANDOM.nextDouble() - 0.5) * 0.12
        );
        var accent = switch (RANDOM.nextInt(4)) {
            case 0 -> ParticleTypes.SOUL_FIRE_FLAME;
            case 1 -> ParticleTypes.WAX_ON;
            case 2 -> ParticleTypes.CHERRY_LEAVES;
            default -> ParticleTypes.END_ROD;
        };
        boolean rareBurst = RANDOM.nextFloat() < 0.075f;
        METEORS.add(new Meteor(new Vec3(x, y, z), velocity, accent,
                rareBurst ? 34 : 22 + RANDOM.nextInt(18), rareBurst));
    }

    private static final class Meteor {
        private Vec3 position;
        private final Vec3 velocity;
        private final net.minecraft.core.particles.SimpleParticleType accent;
        private final int lifetime;
        private final boolean burst;
        private int age;

        private Meteor(Vec3 position, Vec3 velocity,
                       net.minecraft.core.particles.SimpleParticleType accent,
                       int lifetime, boolean burst) {
            this.position = position;
            this.velocity = velocity;
            this.accent = accent;
            this.lifetime = lifetime;
            this.burst = burst;
        }
    }
}
