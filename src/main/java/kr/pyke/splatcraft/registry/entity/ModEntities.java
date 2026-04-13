package kr.pyke.splatcraft.registry.entity;

import kr.pyke.splatcraft.SplatCraft;
import kr.pyke.splatcraft.registry.entity.projectile.ink.InkProjectileEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static final EntityType<InkProjectileEntity> INK_PROJECTILE = register("ink_projectile", EntityType.Builder.<InkProjectileEntity>of(InkProjectileEntity::new, MobCategory.MISC).noLootTable().sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10));

    private ModEntities() { }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(SplatCraft.MOD_ID, name));

        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void register() {
        SplatCraft.LOGGER.info("Registering Mod Entities for " + SplatCraft.MOD_ID);
    }
}
