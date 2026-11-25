package dev.starfall.explosions.item;
import dev.starfall.explosions.StarfallExplosions;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
public final class ModItems {
    public static final ResourceKey<Item> CONTROLLER_KEY = ResourceKey.create(Registries.ITEM,
        Identifier.fromNamespaceAndPath(StarfallExplosions.MOD_ID, "stellar_controller"));
    public static final Item STARFALL_CONTROLLER = Registry.register(BuiltInRegistries.ITEM, CONTROLLER_KEY,
        new StellarControllerItem(new Item.Properties().setId(CONTROLLER_KEY).stacksTo(1)));
    private ModItems() {}
    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
            .register(entries -> entries.accept(STARFALL_CONTROLLER));
    }
}
