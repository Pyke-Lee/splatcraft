package kr.pyke.splatcraft.network;

import kr.pyke.splatcraft.data.Field;
import kr.pyke.splatcraft.manager.FieldManager;
import kr.pyke.splatcraft.manager.InkStorage;
import kr.pyke.splatcraft.data.ChunkInkData;
import kr.pyke.splatcraft.network.payload.s2c.S2C_FieldSyncPayload;
import kr.pyke.splatcraft.network.payload.s2c.S2C_InkBatchUpdatePayload;
import kr.pyke.splatcraft.network.payload.s2c.S2C_InkClearPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.List;
import java.util.Map;

public class SCPacket {
    private SCPacket() { }

    public static void registerCodec() {
        // Client → Server

        // Server → Client
        PayloadTypeRegistry.clientboundPlay().register(S2C_InkBatchUpdatePayload.ID, S2C_InkBatchUpdatePayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2C_InkClearPayload.ID, S2C_InkClearPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2C_FieldSyncPayload.ID, S2C_FieldSyncPayload.STREAM_CODEC);
    }

    public static void registerServer() {

    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(S2C_InkBatchUpdatePayload.ID, S2C_InkBatchUpdatePayload::handle);
        ClientPlayNetworking.registerGlobalReceiver(S2C_InkClearPayload.ID, S2C_InkClearPayload::handle);
        ClientPlayNetworking.registerGlobalReceiver(S2C_FieldSyncPayload.ID, S2C_FieldSyncPayload::handle);
    }

    public static void broadcastDirtyInk(ServerLevel level) {
        InkStorage storage = InkStorage.get(level);
        List<Map.Entry<ChunkPos, ChunkInkData>> dirtyChunks = storage.collectDirtyChunks();
        if (dirtyChunks.isEmpty()) { return; }

        for (var entry : dirtyChunks) {
            ChunkPos chunkPos = entry.getKey();
            ChunkInkData data = entry.getValue();

            S2C_InkBatchUpdatePayload payload = new S2C_InkBatchUpdatePayload(chunkPos.pack(), data);

            for (ServerPlayer player : level.players()) {
                if (isPlayerNearChunk(player, chunkPos)) {
                    ServerPlayNetworking.send(player, payload);
                }
            }

            data.clearDirty();
        }
    }

    public static void sendFieldSync(ServerPlayer player) {
        FieldManager manager = FieldManager.get(player.level());
        S2C_FieldSyncPayload payload = new S2C_FieldSyncPayload(List.copyOf(manager.getAllFields()));
        ServerPlayNetworking.send(player, payload);
    }

    public static void broadcastFieldSync(ServerLevel level) {
        FieldManager manager = FieldManager.get(level);
        S2C_FieldSyncPayload payload = new S2C_FieldSyncPayload(List.copyOf(manager.getAllFields()));
        for (ServerPlayer player : level.players()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void broadcastInkClear(ServerLevel level, Field field) {
        S2C_InkClearPayload payload = new S2C_InkClearPayload(field.getMin(), field.getMax());
        for (ServerPlayer player : level.players()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void sendFullInkSnapshot(ServerPlayer player) {
        InkStorage storage = InkStorage.get(player.level());
        for (var entry : storage.getAllChunks().entrySet()) {
            if (!entry.getValue().isEmpty()) {
                S2C_InkBatchUpdatePayload payload = new S2C_InkBatchUpdatePayload(entry.getKey().pack(), entry.getValue());
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    private static boolean isPlayerNearChunk(ServerPlayer player, ChunkPos chunkPos) {
        int playerCX = player.getBlockX() >> 4;
        int playerCZ = player.getBlockZ() >> 4;
        int distance = Math.max(Math.abs(playerCX - chunkPos.x()), Math.abs(playerCZ - chunkPos.z()));

        return distance <= player.level().getServer().getPlayerList().getViewDistance();
    }
}