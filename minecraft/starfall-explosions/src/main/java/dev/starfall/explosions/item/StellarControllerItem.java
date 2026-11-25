package dev.starfall.explosions.item;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
public final class StellarControllerItem extends Item {
    public StellarControllerItem(Properties properties) { super(properties); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            try {
                Class.forName("dev.starfall.explosions.client.ControllerScreenOpener").getMethod("open").invoke(null);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Could not open Starfall controller screen", exception);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
