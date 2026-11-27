package ru.starfalldreams.vasya;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.*;

public class VasyaMod implements ModInitializer {
    public static final String MOD_ID = "vasya";

    @Override public void onInitialize() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) return;
            VasyaProgress.addXp(serverPlayer, 1);
            if (VasyaProgress.getXp(serverPlayer) % 12 == 0) {
                serverPlayer.sendMessage(Text.literal("§eВася наблюдает за добычей и чему-то учится!"), true);
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("vasya")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                    CatEntity cat = EntityType.CAT.create(player.getWorld(), SpawnReason.TRIGGERED);
                    if (cat == null) return 0;
                    BlockPos p = player.getBlockPos().add(1, 0, 1);
                    cat.refreshPositionAndAngles(p, player.getYaw(), 0);
                    cat.setCustomName(Text.literal("Вася"));
                    cat.setCustomNameVisible(true);
                    cat.setTamed(true, true);
                    cat.setOwner(player);
                    player.getWorld().spawnEntity(cat);
                    player.sendMessage(Text.literal("§aВася появился! Познакомься с ним."), false);
                    return 1;
                })
                .then(literal("skill").then(argument("name", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                        String skill = StringArgumentType.getString(ctx, "name");
                        boolean upgraded = VasyaProgress.upgrade(player, skill);
                        player.sendMessage(Text.literal(upgraded
                            ? "§aНавык «" + skill + "» улучшен!"
                            : "§cНедостаточно опыта или неизвестный навык."), false);
                        return upgraded ? 1 : 0;
                    })))
                .then(literal("ask").then(argument("question", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                        String q = StringArgumentType.getString(ctx, "question").toLowerCase();
                        String answer = q.contains("еда") || q.contains("есть")
                            ? "Мяу! Я бы поискал рыбку или курочку."
                            : q.contains("ресурс") || q.contains("добы")
                            ? "Я пока учусь. Покажи мне, как ты добываешь блоки!"
                            : q.contains("привет") ? "Мяу-мяу! Рад тебя видеть!"
                            : "Мр-р? Я пока не всё понимаю, но обязательно научусь!";
                        player.sendMessage(Text.literal("§6Вася: §f" + answer), false);
                        return 1;
                    })))
            )
        );
    }
}