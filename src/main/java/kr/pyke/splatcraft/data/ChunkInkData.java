package kr.pyke.splatcraft.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkInkData {
    private final Map<BlockPos, InkEntry> entries = new ConcurrentHashMap<>();
    private volatile boolean dirty = false;

    public static final StreamCodec<FriendlyByteBuf, ChunkInkData> STREAM_CODEC = StreamCodec.of(ChunkInkData::write, ChunkInkData::read);

    public InkEntry getEntry(BlockPos pos) { return entries.get(pos); }

    public boolean hasInk(BlockPos pos) { return entries.containsKey(pos); }

    public boolean hasInkOnFace(BlockPos pos, Direction face) {
        InkEntry entry = entries.get(pos);

        return entry != null && entry.hasFace(face);
    }

    public byte getTeamAt(BlockPos pos) {
        InkEntry entry = entries.get(pos);

        return entry != null ? entry.getTeamID() : 0;
    }

    public Map<BlockPos, InkEntry> getEntries() { return Collections.unmodifiableMap(entries); }

    public int size() { return entries.size(); }

    public boolean isEmpty() { return entries.isEmpty(); }

    public boolean applyInk(BlockPos pos, Direction face, byte teamID, byte pattern) {
        BlockPos immutable = pos.immutable();
        InkEntry entry = entries.get(immutable);

        if (entry == null) {
            entry = new InkEntry(teamID, pattern);
            entry.addFace(face);
            entries.put(immutable, entry);
            dirty = true;

            return true;
        }

        boolean changed = false;

        if (entry.getTeamID() != teamID) {
            entry.setTeamID(teamID);
            entry.setPattern(pattern);
            if (!entry.hasFace(face)) { entry.addFace(face); }
            changed = true;
        }
        else if (!entry.hasFace(face)) {
            entry.addFace(face);
            changed = true;
        }

        if (changed) { dirty = true; }
        return changed;
    }

    public boolean removeInk(BlockPos pos, Direction face) {
        InkEntry entry = entries.get(pos);
        if (entry == null || !entry.hasFace(face)) { return false; }

        entry.removeFace(face);
        if (entry.isEmpty()) { entries.remove(pos); }

        dirty = true;
        return true;
    }

    public boolean removeAllInk(BlockPos pos) {
        if (entries.remove(pos) != null) {
            dirty = true;
            return true;
        }

        return false;
    }

    public void clear() {
        if (!entries.isEmpty()) {
            entries.clear();
            dirty = true;
        }
    }

    public int clearTeam(byte teamID) {
        int removed = 0;
        var iterator = entries.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().getTeamID() == teamID) {
                iterator.remove();
                removed++;
            }
        }

        if (removed > 0) { dirty = true; }
        return removed;
    }

    public boolean isDirty() { return dirty; }

    public void markDirty() {
        dirty = true;
    }

    public void clearDirty() {
        dirty = false;
    }

    public int countFaces(byte teamID) {
        int count = 0;
        for (InkEntry entry : entries.values()) {
            if (entry.getTeamID() == teamID) {
                count += entry.faceCount();
            }
        }

        return count;
    }

    public int countTotalFaces() {
        int count = 0;
        for (InkEntry entry : entries.values()) {
            count += entry.faceCount();
        }

        return count;
    }

    private static void write(FriendlyByteBuf buf, ChunkInkData data) {
        buf.writeVarInt(data.entries.size());
        for (var entry : data.entries.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            InkEntry.STREAM_CODEC.encode(buf, entry.getValue());
        }
    }

    private static ChunkInkData read(FriendlyByteBuf buf) {
        ChunkInkData data = new ChunkInkData();
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            BlockPos pos = buf.readBlockPos();
            InkEntry entry = InkEntry.STREAM_CODEC.decode(buf);
            data.entries.put(pos, entry);
        }

        return data;
    }
}