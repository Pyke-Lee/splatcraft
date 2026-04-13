package kr.pyke.splatcraft.network.payload.s2c;

import kr.pyke.splatcraft.SplatCraft;
import kr.pyke.splatcraft.manager.InkStorage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record S2C_InkClearPayload(BlockPos min, BlockPos max) implements CustomPacketPayload {
    public static final Type<S2C_InkClearPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(SplatCraft.MOD_ID, "s2c_ink_clear"));

    public static final StreamCodec<FriendlyByteBuf, S2C_InkClearPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, S2C_InkClearPayload::min,
        BlockPos.STREAM_CODEC, S2C_InkClearPayload::max,
        S2C_InkClearPayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(S2C_InkClearPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            var level = context.client().level;
            if (level == null) { return; }

            InkStorage.get(level).clearArea(payload.min, payload.max);
        });
    }
}