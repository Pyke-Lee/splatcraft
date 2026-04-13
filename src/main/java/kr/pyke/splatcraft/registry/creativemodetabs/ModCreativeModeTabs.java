package kr.pyke.splatcraft.registry.creativemodetabs;

import kr.pyke.splatcraft.SplatCraft;
import kr.pyke.splatcraft.registry.item.ModItems;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTabs {
    public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(SplatCraft.MOD_ID, "splatcraft_creative_tab"));

    private ModCreativeModeTabs() { }

    public static final CreativeModeTab DEFAULT_CREATIVE_TAB = FabricCreativeModeTab.builder()
        .icon(() -> new ItemStack(ModItems.FIELD_MARKER))
        .title(Component.translatable("itemGroup.splatcraft.creative_tab"))
        .displayItems((params, output) -> {
            output.accept(ModItems.FIELD_MARKER);
        })
        .build();

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_KEY, DEFAULT_CREATIVE_TAB);
    }
}