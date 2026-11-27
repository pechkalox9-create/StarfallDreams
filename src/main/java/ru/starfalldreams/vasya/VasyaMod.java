package ru.starfalldreams.vasya;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.registry.Registries;

import static net.minecraft.server.command.CommandManager.*;

public class VasyaMod implements ModInitializer {
    public static final String MOD_ID = "vasya";

    @Override
    public void onInitialize() {
        // Вася учится на реально наблюдаемых действиях, а не на каждом тике игрока.
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) return;
            String block = Registries.BLOCK.getId(state.getBlock()).getPath();
            String skill = classifyBrokenBlock(block);
            if (skill != null) notifyLearning(serverPlayer,
                VasyaProgress.observe(serverPlayer, skill, humanName(block)), skill);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            String block = Registries.BLOCK.getId(world.getBlockState(hit.getBlockPos()).getBlock()).getPath();
            String skill = classifyUsedBlock(block);
            if (skill != null) notifyLearning(serverPlayer,
                VasyaProgress.observe(serverPlayer, skill, humanName(block)), skill);
            return ActionResult.PASS;
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
                    player.sendMessage(Text.literal("§aВася появился! Он будет наблюдать за твоими действиями и учиться."), false);
                    return 1;
                })
                .then(literal("status").executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                    player.sendMessage(Text.literal("§6Вася: §f" + VasyaProgress.status(player)), false);
                    return 1;
                }))
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
                        player.sendMessage(Text.literal("§6Вася: §f" + VasyaProgress.reply(player, q)), false);
                        return 1;
                    })))
            )
        );
    }

    private static void notifyLearning(ServerPlayerEntity player, boolean learned, String skill) {
        if (learned) player.sendMessage(Text.literal("§eВася чему-то научился! Навык: " + skill), true);
    }

    private static String classifyBrokenBlock(String id) {
        if (id.contains("ore") || id.equals("stone") || id.equals("deepslate")
            || id.contains("netherrack") || id.contains("granite") || id.contains("diorite")
            || id.contains("andesite")) return "mining";
        if (id.contains("log") || id.contains("wood") || id.contains("planks")
            || id.contains("leaves") || id.contains("wool") || id.contains("cobweb")) return "gathering";
        if (id.contains("wheat") || id.contains("carrot") || id.contains("potato")
            || id.contains("beetroot") || id.contains("melon") || id.contains("pumpkin")
            || id.contains("berry") || id.contains("mushroom")) return "foraging";
        return null;
    }

    private static String classifyUsedBlock(String id) {
        if (id.contains("furnace") || id.contains("smoker") || id.contains("campfire")) return "cooking";
        if (id.contains("crafting_table") || id.contains("stonecutter") || id.contains("smithing_table")) return "gathering";
        if (id.contains("chest") || id.contains("barrel") || id.contains("shulker_box")) return "gathering";
        return null;
    }

    private static String humanName(String id) {
        return id.replace('_', ' ');
    }
}
