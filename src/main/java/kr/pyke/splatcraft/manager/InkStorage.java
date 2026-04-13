package kr.pyke.splatcraft.manager;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kr.pyke.splatcraft.SplatCraft;
import kr.pyke.splatcraft.data.ChunkInkData;
import kr.pyke.splatcraft.data.InkEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InkStorage {
    private static final Map<Level, InkStorage> INSTANCES = new ConcurrentHashMap<>();
    private final Map<ChunkPos, ChunkInkData> chunks = new ConcurrentHashMap<>();

    private InkStorage() { }

    public static InkStorage get(Level level) { return INSTANCES.computeIfAbsent(level, k -> new InkStorage()); }

    public static void remove(Level level) {
        InkStorage storage = INSTANCES.remove(level);
        if (storage != null) { storage.chunks.clear(); }
    }

    public ChunkInkData getChunkData(ChunkPos chunkPos) { return chunks.get(chunkPos); }

    public ChunkInkData getOrCreateChunkData(ChunkPos chunkPos) { return chunks.computeIfAbsent(chunkPos, k -> new ChunkInkData()); }

    public boolean hasInk(BlockPos pos) {
        ChunkInkData data = chunks.get(ChunkPos.containing(pos));

        return data != null && data.hasInk(pos);
    }

    public boolean hasInkOnFace(BlockPos pos, Direction face) {
        ChunkInkData data = chunks.get(ChunkPos.containing(pos));

        return data != null && data.hasInkOnFace(pos, face);
    }

    public byte getTeamAt(BlockPos pos) {
        ChunkInkData data = chunks.get(ChunkPos.containing(pos));

        return data != null ? data.getTeamAt(pos) : 0;
    }

    public InkEntry getEntry(BlockPos pos) {
        ChunkInkData data = chunks.get(ChunkPos.containing(pos));

        return data != null ? data.getEntry(pos) : null;
    }

    public boolean applyInk(BlockPos pos, Direction face, byte teamID, byte pattern) {
        ChunkPos chunkPos = ChunkPos.containing(pos);
        ChunkInkData data = getOrCreateChunkData(chunkPos);

        return data.applyInk(pos, face, teamID, pattern);
    }

    public boolean removeInk(BlockPos pos, Direction face) {
        ChunkPos chunkPos = ChunkPos.containing(pos);
        ChunkInkData data = chunks.get(chunkPos);
        if (data == null) { return false; }

        boolean result = data.removeInk(pos, face);
        cleanupEmptyChunk(chunkPos, data);
        return result;
    }

    public boolean removeAllInk(BlockPos pos) {
        ChunkPos chunkPos = ChunkPos.containing(pos);
        ChunkInkData data = chunks.get(chunkPos);
        if (data == null) { return false; }

        boolean result = data.removeAllInk(pos);
        cleanupEmptyChunk(chunkPos, data);
        return result;
    }

    public void clearAll() {
        chunks.clear();
    }

    public int clearArea(BlockPos min, BlockPos max) {
        int removed = 0;
        int minCX = min.getX() >> 4;
        int maxCX = max.getX() >> 4;
        int minCZ = min.getZ() >> 4;
        int maxCZ = max.getZ() >> 4;

        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                ChunkPos chunkPos = new ChunkPos(cx, cz);
                ChunkInkData data = chunks.get(chunkPos);
                if (data == null) { continue; }

                List<BlockPos> toRemove = new ArrayList<>();
                for (BlockPos pos : data.getEntries().keySet()) {
                    if (isWithinBounds(pos, min, max)) { toRemove.add(pos); }
                }
                for (BlockPos pos : toRemove) {
                    if (data.removeAllInk(pos)) { removed++; }
                }
                cleanupEmptyChunk(chunkPos, data);
            }
        }

        return removed;
    }

    public int clearTeam(byte teamID) {
        int removed = 0;
        List<ChunkPos> emptyChunks = new ArrayList<>();

        for (var entry : chunks.entrySet()) {
            removed += entry.getValue().clearTeam(teamID);
            if (entry.getValue().isEmpty()) {
                emptyChunks.add(entry.getKey());
            }
        }

        for (ChunkPos pos : emptyChunks) { chunks.remove(pos); }
        return removed;
    }

    public List<Map.Entry<ChunkPos, ChunkInkData>> collectDirtyChunks() {
        List<Map.Entry<ChunkPos, ChunkInkData>> dirtyList = new ArrayList<>();
        for (var entry : chunks.entrySet()) {
            if (entry.getValue().isDirty()) { dirtyList.add(entry); }
        }

        return dirtyList;
    }

    public boolean hasDirtyChunks() {
        for (ChunkInkData data : chunks.values()) {
            if (data.isDirty()) { return true; }
        }

        return false;
    }

    public Map<Byte, Integer> countFacesInArea(BlockPos min, BlockPos max) {
        Map<Byte, Integer> counts = new HashMap<>();
        int minCX = min.getX() >> 4;
        int maxCX = max.getX() >> 4;
        int minCZ = min.getZ() >> 4;
        int maxCZ = max.getZ() >> 4;

        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                ChunkInkData data = chunks.get(new ChunkPos(cx, cz));
                if (data == null) { continue; }

                for (var entry : data.getEntries().entrySet()) {
                    if (!isWithinBounds(entry.getKey(), min, max)) { continue; }

                    InkEntry inkEntry = entry.getValue();
                    counts.merge(inkEntry.getTeamID(), inkEntry.faceCount(), Integer::sum);
                }
            }
        }

        return counts;
    }

    public Map<ChunkPos, ChunkInkData> getAllChunks() { return Collections.unmodifiableMap(chunks); }

    private void cleanupEmptyChunk(ChunkPos chunkPos, ChunkInkData data) {
        if (data.isEmpty()) {
            chunks.remove(chunkPos);
        }
    }

    private static boolean isWithinBounds(BlockPos pos, BlockPos min, BlockPos max) { return pos.getX() >= min.getX() && pos.getX() <= max.getX() && pos.getY() >= min.getY() && pos.getY() <= max.getY() && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ(); }

    public static void loadFromSavedData(ServerLevel level) {
        InkSavedData savedData = level.getDataStorage().computeIfAbsent(InkSavedData.TYPE);
        InkStorage storage = get(level);
        storage.chunks.clear();

        for (InkRecord record : savedData.getRecords()) {
            ChunkPos chunkPos = ChunkPos.containing(record.pos);
            ChunkInkData chunkData = storage.getOrCreateChunkData(chunkPos);

            for (Direction dir : Direction.values()) {
                if ((record.faceMask & (1 << dir.ordinal())) != 0) {
                    chunkData.applyInk(record.pos, dir, record.teamID, record.pattern);
                }
            }
            chunkData.clearDirty();
        }
    }

    public static void markSavedDataDirty(ServerLevel level) {
        InkSavedData savedData = level.getDataStorage().computeIfAbsent(InkSavedData.TYPE);
        InkStorage storage = get(level);

        List<InkRecord> records = new ArrayList<>();
        for (var chunkEntry : storage.chunks.entrySet()) {
            for (var blockEntry : chunkEntry.getValue().getEntries().entrySet()) {
                InkEntry ink = blockEntry.getValue();
                records.add(new InkRecord(blockEntry.getKey(), ink.getFaceMask(), ink.getTeamID(), ink.getPattern()));
            }
        }

        savedData.setRecords(records);
        savedData.setDirty();
    }

    private record InkRecord(BlockPos pos, byte faceMask, byte teamID, byte pattern) {
        static final Codec<InkRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(InkRecord::pos),
            Codec.BYTE.fieldOf("face_mask").forGetter(InkRecord::faceMask),
            Codec.BYTE.fieldOf("team_id").forGetter(InkRecord::teamID),
            Codec.BYTE.fieldOf("pattern").forGetter(InkRecord::pattern)
        ).apply(instance, InkRecord::new));
    }

    public static class InkSavedData extends SavedData {
        private static final Identifier FILE_NAME = Identifier.fromNamespaceAndPath(SplatCraft.MOD_ID, "splatcraft_ink");

        public static final Codec<InkSavedData> CODEC = InkRecord.CODEC.listOf()
            .xmap(
                list -> {
                    InkSavedData data = new InkSavedData();
                    data.records = new ArrayList<>(list);

                    return data;
                },
                data -> data.records
            );

        public static final SavedDataType<InkSavedData> TYPE = new SavedDataType<>(FILE_NAME, InkSavedData::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

        private List<InkRecord> records = new ArrayList<>();

        public InkSavedData() { }

        List<InkRecord> getRecords() { return records; }

        void setRecords(List<InkRecord> records) {
            this.records = records;
        }
    }
}