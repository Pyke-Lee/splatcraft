package kr.pyke.splatcraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class InkEntry {
    private byte faceMask;
    private byte teamID;
    private byte pattern;

    public static final Codec<InkEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BYTE.fieldOf("face_mask").forGetter(InkEntry::getFaceMask),
        Codec.BYTE.fieldOf("team_id").forGetter(InkEntry::getTeamID),
        Codec.BYTE.fieldOf("pattern").forGetter(InkEntry::getPattern)
    ).apply(instance, InkEntry::new));

    public static final StreamCodec<FriendlyByteBuf, InkEntry> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BYTE, InkEntry::getFaceMask,
        ByteBufCodecs.BYTE, InkEntry::getTeamID,
        ByteBufCodecs.BYTE, InkEntry::getPattern,
        InkEntry::new
    );

    public InkEntry(byte faceMask, byte teamID, byte pattern) {
        this.faceMask = faceMask;
        this.teamID = teamID;
        this.pattern = pattern;
    }

    public InkEntry(byte teamID, byte pattern) {
        this((byte) 0, teamID, pattern);
    }

    public boolean hasFace(Direction direction) { return (faceMask & (1 << direction.ordinal())) != 0; }

    public void addFace(Direction direction) {
        faceMask |= (byte) (1 << direction.ordinal());
    }

    public void removeFace(Direction direction) {
        faceMask &= (byte) ~(1 << direction.ordinal());
    }

    public boolean isEmpty() { return faceMask == 0; }

    public int faceCount() { return Integer.bitCount(faceMask & 0x3F); }

    public byte getFaceMask() { return faceMask; }

    public byte getTeamID() { return teamID; }

    public void setTeamID(byte teamID) {
        this.teamID = teamID;
    }

    public byte getPattern() { return pattern; }

    public void setPattern(byte pattern) {
        this.pattern = pattern;
    }

    public InkEntry copy() { return new InkEntry(faceMask, teamID, pattern); }
}

