package ru.starfalldreams.vasya;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Session MVP. Persistent world-backed storage will replace this map in the next iteration. */
public final class VasyaProgress {
    private static final Map<UUID, Data> DATA = new ConcurrentHashMap<>();
    private static Data data(ServerPlayerEntity p) { return DATA.computeIfAbsent(p.getUuid(), id -> new Data()); }
    public static void addXp(ServerPlayerEntity p, int amount) { data(p).xp += amount; }
    public static int getXp(ServerPlayerEntity p) { return data(p).xp; }
    public static int level(ServerPlayerEntity p, String skill) { return data(p).levels.getOrDefault(skill, 0); }
    public static boolean upgrade(ServerPlayerEntity p, String skill) {
        if (!skill.matches("gathering|mining|foraging|cooking|defense")) return false;
        Data d = data(p);
        int cost = 5 + d.levels.getOrDefault(skill, 0) * 5;
        if (d.xp < cost) return false;
        d.xp -= cost;
        d.levels.merge(skill, 1, Integer::sum);
        return true;
    }
    private static final class Data {
        int xp;
        final Map<String,Integer> levels = new ConcurrentHashMap<>();
    }
}