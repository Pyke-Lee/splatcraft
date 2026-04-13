package kr.pyke.splatcraft.registry.item;

import kr.pyke.splatcraft.SplatCraft;
import kr.pyke.splatcraft.registry.item.fieldmarker.FieldMarkerItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class ModItems {
    public static final Item FIELD_MARKER = registerFactory("marker/field", properties -> new FieldMarkerItem(properties.stacksTo(1)));

    private ModItems() { }

    private static Item registerFactory(String name, Function<Item.Properties, Item> factory) {
        ResourceKey<Item> resourceKey = key(name);
        Item.Properties properties = new Item.Properties().setId(resourceKey);

        return Registry.register(BuiltInRegistries.ITEM, resourceKey, factory.apply(properties));
    }

    private static ResourceKey<Item> key(String name) { return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SplatCraft.MOD_ID, name)); }

    public static void register() {
        SplatCraft.LOGGER.info("Registering Mod Items for " + SplatCraft.MOD_ID);
    }
}
