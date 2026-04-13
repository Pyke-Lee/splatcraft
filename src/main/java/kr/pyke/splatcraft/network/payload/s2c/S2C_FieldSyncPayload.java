package kr.pyke.splatcraft.network.payload.s2c;

import kr.pyke.splatcraft.SplatCraft;
import kr.pyke.splatcraft.data.Field;
import kr.pyke.splatcraft.manager.FieldManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record S2C_FieldSyncPayload(List<Field> fields) implements CustomPacketPayload {
    public static final Type<S2C_FieldSyncPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(SplatCraft.MOD_ID, "s2c_field_sync"));

    public static final StreamCodec<FriendlyByteBuf, S2C_FieldSyncPayload> STREAM_CODEC = StreamCodec.composite(
        Field.STREAM_CODEC.apply(ByteBufCodecs.list()), S2C_FieldSyncPayload::fields,
        S2C_FieldSyncPayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(S2C_FieldSyncPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            var level = context.client().level;
            if (level == null) { return; }

            FieldManager manager = FieldManager.get(level);
            manager.clear();
            for (Field field : payload.fields) { manager.addField(field); }
        });
    }
}