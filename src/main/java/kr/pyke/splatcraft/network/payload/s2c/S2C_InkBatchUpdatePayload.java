package kr.pyke.splatcraft.network.payload.s2c;

import kr.pyke.splatcraft.SplatCraft;
import kr.pyke.splatcraft.data.ChunkInkData;
import kr.pyke.splatcraft.manager.InkStorage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.NonNull;

public record S2C_InkBatchUpdatePayload(long chunkPosLong, ChunkInkData data) implements CustomPacketPayload {
    public static final Type<S2C_InkBatchUpdatePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(SplatCraft.MOD_ID, "s2c_ink_batch_update"));

    public static final StreamCodec<FriendlyByteBuf, S2C_InkBatchUpdatePayload> STREAM_CODEC = StreamCodec.composite(
        StreamCodec.of(FriendlyByteBuf::writeLong, FriendlyByteBuf::readLong), S2C_InkBatchUpdatePayload::chunkPosLong,
        ChunkInkData.STREAM_CODEC, S2C_InkBatchUpdatePayload::data,
        S2C_InkBatchUpdatePayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public ChunkPos chunkPos() { return ChunkPos.unpack(chunkPosLong); }

    public static void handle(S2C_InkBatchUpdatePayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            var level = context.client().level;
            if (level == null) { return; }

            InkStorage storage = InkStorage.get(level);
            ChunkPos chunkPos = payload.chunkPos();
            ChunkInkData existing = storage.getOrCreateChunkData(chunkPos);

            existing.clear();
            for (var entry : payload.data().getEntries().entrySet()) {
                var inkEntry = entry.getValue();
                for (Direction dir : Direction.values()) {
                    if (inkEntry.hasFace(dir)) {
                        existing.applyInk(entry.getKey(), dir, inkEntry.getTeamID(), inkEntry.getPattern());
                    }
                }
            }
            existing.markDirty();
        });
    }
}