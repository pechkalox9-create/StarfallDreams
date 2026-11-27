package ru.starfalldreams.vasya;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Observation-based learning prototype.
 * Progress is currently session-scoped; world persistence is a separate next step.
 */
public final class VasyaProgress {
    private static final Map<UUID, Data> DATA = new ConcurrentHashMap<>();
    private static final String[] SKILLS = {"gathering", "mining", "foraging", "cooking", "defense"};

    private static Data data(ServerPlayerEntity player) {
        return DATA.computeIfAbsent(player.getUuid(), id -> new Data());
    }

    /** Called only when the server observes a meaningful player action. */
    public static boolean observe(ServerPlayerEntity player, String skill, String detail) {
        if (!isSkill(skill)) return false;
        Data d = data(player);
        int count = d.observations.merge(skill, 1, Integer::sum);
        d.lastObservation.put(skill, detail);
        d.xp += 1;

        int level = d.levels.getOrDefault(skill, 0);
        int threshold = 4 + level * 6;
        if (count >= threshold) {
            d.observations.put(skill, 0);
            d.levels.put(skill, level + 1);
            return true;
        }
        return false;
    }

    public static int getXp(ServerPlayerEntity player) { return data(player).xp; }

    public static int level(ServerPlayerEntity player, String skill) {
        return data(player).levels.getOrDefault(skill, 0);
    }

    public static boolean upgrade(ServerPlayerEntity player, String skill) {
        if (!isSkill(skill)) return false;
        Data d = data(player);
        int cost = 5 + d.levels.getOrDefault(skill, 0) * 5;
        if (d.xp < cost) return false;
        d.xp -= cost;
        d.levels.merge(skill, 1, Integer::sum);
        return true;
    }

    public static String status(ServerPlayerEntity player) {
        Data d = data(player);
        return "Опыт наблюдений: " + d.xp
            + ". Навыки: сбор " + level(player, "gathering")
            + ", добыча " + level(player, "mining")
            + ", поиск еды " + level(player, "foraging")
            + ", готовка " + level(player, "cooking")
            + ", осторожность " + level(player, "defense") + ".";
    }

    public static String reply(ServerPlayerEntity player, String question) {
        Data d = data(player);
        if (question.contains("чему") || question.contains("науч") || question.contains("умеешь")) {
            return "Я наблюдаю за тобой! " + status(player);
        }
        if (question.contains("еда") || question.contains("есть")) {
            return d.lastObservation.containsKey("foraging")
                ? "Я заметил, как ты добываешь " + d.lastObservation.get("foraging") + ". Учусь находить еду!"
                : "Пока я не видел, как ты ищешь еду. Покажи мне, где её искать.";
        }
        if (question.contains("ресурс") || question.contains("добы")) {
            return d.lastObservation.containsKey("mining")
                ? "Я запомнил, как ты добывал " + d.lastObservation.get("mining") + ". Продолжай, я учусь!"
                : "Я пока наблюдаю за добычей. Покажи мне разные блоки.";
        }
        if (question.contains("привет")) return "Мяу-мяу! Я внимательно наблюдаю и учусь у тебя.";
        return "Мр-р! Я учусь, наблюдая за твоими действиями. Спроси, чему я уже научился.";
    }

    private static boolean isSkill(String skill) {
        for (String known : SKILLS) if (known.equals(skill)) return true;
        return false;
    }

    private static final class Data {
        int xp;
        final Map<String, Integer> levels = new ConcurrentHashMap<>();
        final Map<String, Integer> observations = new ConcurrentHashMap<>();
        final Map<String, String> lastObservation = new ConcurrentHashMap<>();
    }

    private VasyaProgress() {}
}
