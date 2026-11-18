package dev.kocmoc.client;

import net.minecraft.client.Minecraft;

public final class Module {
    public enum Type {
        COMBAT, MOVEMENT, RENDER, PLAYER, UTILITY
    }

    private final String name;
    private final Type type;
    private boolean enabled;
    private double previousGamma = -1.0;

    public Module(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String name() {
        return name;
    }

    public Type type() {
        return type;
    }

    public boolean enabled() {
        return enabled;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;
        Minecraft client = Minecraft.getInstance();

        if (name.equals("FullBright") && client.options != null) {
            if (enabled) {
                previousGamma = client.options.gamma().get();
                client.options.gamma().set(1.0);
            } else if (previousGamma >= 0.0) {
                client.options.gamma().set(previousGamma);
                previousGamma = -1.0;
            }
        }
    }

    public void tick(Minecraft client) {
        if (!enabled || client.player == null) {
            return;
        }

        if (name.equals("Sprint") && client.player.input.hasForwardImpulse()) {
            client.player.setSprinting(true);
        }

        if (name.equals("FullBright") && client.options.gamma().get() < 1.0) {
            client.options.gamma().set(1.0);
        }
    }
}
