package kr.pyke.splatcraft.registry.component;

import kr.pyke.splatcraft.SplatCraft;
import kr.pyke.splatcraft.registry.component.squid.ISquidComponent;
import kr.pyke.splatcraft.registry.component.squid.SquidComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class ModComponents implements EntityComponentInitializer {
    public static final ComponentKey<ISquidComponent> SQUID = ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.fromNamespaceAndPath(SplatCraft.MOD_ID, "squid"), ISquidComponent.class);

    @Override
    public void registerEntityComponentFactories(@NonNull EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(Player.class, SQUID).impl(SquidComponent.class).respawnStrategy(RespawnCopyStrategy.LOSSLESS_ONLY).end(SquidComponent::new);
    }
}
