package kr.pyke.splatcraft.client.render;

import kr.pyke.splatcraft.registry.entity.projectile.ink.InkProjectileEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class InkProjectileRenderer extends EntityRenderer<InkProjectileEntity, EntityRenderState> {
    public InkProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override public @NonNull EntityRenderState createRenderState() { return new EntityRenderState(); }
}