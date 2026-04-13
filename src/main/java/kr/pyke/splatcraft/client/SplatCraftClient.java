package kr.pyke.splatcraft.client;

import kr.pyke.splatcraft.client.render.InkProjectileRenderer;
import kr.pyke.splatcraft.network.SCPacket;
import kr.pyke.splatcraft.registry.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class SplatCraftClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SCPacket.registerClient();

        EntityRenderers.register(ModEntities.INK_PROJECTILE, InkProjectileRenderer::new);
    }
}
