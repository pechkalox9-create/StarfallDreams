package ru.starfalldreams.vasya;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class VasyaClient implements ClientModInitializer {
    private static final KeyBinding.Category VASYA_CATEGORY =
        KeyBinding.Category.create(Identifier.of("vasya", "main"));
    private KeyBinding askKey;
    private KeyBinding skillsKey;

    @Override public void onInitializeClient() {
        askKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.vasya.ask", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, VASYA_CATEGORY));
        skillsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.vasya.skills", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, VASYA_CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (askKey.wasPressed()) client.setScreen(new VasyaDialogScreen());
            while (skillsKey.wasPressed()) client.setScreen(new VasyaSkillsScreen());
        });
    }
}