package kr.pyke.splatcraft.mixin.server;

import kr.pyke.splatcraft.handle.SquidHandler;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "tick", at = @At("RETURN"))
    public void tick(CallbackInfo ci) {
        SquidHandler.tickPlayer((ServerPlayer) (Object) this);
    }
}
