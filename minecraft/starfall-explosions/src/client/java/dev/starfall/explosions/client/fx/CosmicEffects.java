package dev.starfall.explosions.client.fx;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
public final class CosmicEffects {
    public enum Preset { NOVA, AURORA, STARFALL, VOID_RIPPLE }
    private static Preset active;
    private static int ticksLeft, age;
    private static final RandomSource RANDOM = RandomSource.create();
    private CosmicEffects() {}
    public static void trigger(Preset preset) { active = preset; ticksLeft = 50; age = 0; }
    public static void tick(Minecraft client) {
        if (active == null || ticksLeft-- <= 0 || client.level == null || client.player == null) { active = null; return; }
        Vec3 origin = client.player.position().add(0, 1.0, 0);
        switch (active) {
            case NOVA -> ring(client, origin, ParticleTypes.END_ROD, 1.2 + age * 0.12, 18);
            case AURORA -> curtain(client, origin);
            case STARFALL -> starfall(client, origin);
            case VOID_RIPPLE -> ring(client, origin, ParticleTypes.PORTAL, 0.8 + age * 0.08, 12);
        }
        age++;
    }
    private static void ring(Minecraft client, Vec3 center, ParticleOptions particle, double radius, int count) {
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2.0 * i / count + age * 0.035;
            double x = Math.cos(angle) * radius, z = Math.sin(angle) * radius;
            client.level.addParticle(particle, center.x + x, center.y, center.z + z,
                -x * 0.012, 0.015 + RANDOM.nextDouble() * 0.025, -z * 0.012);
        }
    }
    private static void curtain(Minecraft client, Vec3 center) {
        for (int i = 0; i < 8; i++) {
            double x = (RANDOM.nextDouble() - 0.5) * 8.0;
            double y = RANDOM.nextDouble() * 3.0 + Math.sin(age * 0.12 + i) * 0.4;
            client.level.addParticle(ParticleTypes.END_ROD, center.x + x, center.y + y, center.z - 3.0,
                0, 0.025 + RANDOM.nextDouble() * 0.025, 0);
        }
    }
    private static void starfall(Minecraft client, Vec3 center) {
        for (int i = 0; i < 5; i++) {
            double x = (RANDOM.nextDouble() - 0.5) * 12.0, z = (RANDOM.nextDouble() - 0.5) * 12.0;
            client.level.addParticle(ParticleTypes.FIREWORK, center.x + x, center.y + 8.0, center.z + z,
                -x * 0.015, -0.22 - RANDOM.nextDouble() * 0.12, -z * 0.015);
        }
    }
}
