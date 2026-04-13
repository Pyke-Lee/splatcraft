package kr.pyke.splatcraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class Field {
    private final String id;
    private final BlockPos min;
    private final BlockPos max;
    private boolean active;

    public static final Codec<Field> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("id").forGetter(Field::getID),
        BlockPos.CODEC.fieldOf("min").forGetter(Field::getMin),
        BlockPos.CODEC.fieldOf("max").forGetter(Field::getMax),
        Codec.BOOL.fieldOf("active").forGetter(Field::isActive)
    ).apply(instance, Field::new));

    public static final StreamCodec<FriendlyByteBuf, Field> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, Field::getID,
        BlockPos.STREAM_CODEC, Field::getMin,
        BlockPos.STREAM_CODEC, Field::getMax,
        ByteBufCodecs.BOOL, Field::isActive,
        Field::new
    );

    private Field(String id, BlockPos min, BlockPos max, boolean active) {
        this.id = id;
        this.min = min;
        this.max = max;
        this.active = active;
    }

    public static Field create(String id, BlockPos corner1, BlockPos corner2) {
        BlockPos min = new BlockPos(
            Math.min(corner1.getX(), corner2.getX()),
            Math.min(corner1.getY(), corner2.getY()),
            Math.min(corner1.getZ(), corner2.getZ())
        );
        BlockPos max = new BlockPos(
            Math.max(corner1.getX(), corner2.getX()),
            Math.max(corner1.getY(), corner2.getY()),
            Math.max(corner1.getZ(), corner2.getZ())
        );

        return new Field(id, min, max, false);
    }

    public boolean contains(BlockPos pos) { return pos.getX() >= min.getX() && pos.getX() <= max.getX() && pos.getY() >= min.getY() && pos.getY() <= max.getY() && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ(); }

    public String getID() { return id; }

    public BlockPos getMin() { return min; }

    public BlockPos getMax() { return max; }

    public boolean isActive() { return active; }

    public void setActive(boolean active) {
        this.active = active;
    }

    public BlockPos center() {
        return new BlockPos(
            (min.getX() + max.getX()) / 2,
            (min.getY() + max.getY()) / 2,
            (min.getZ() + max.getZ()) / 2
        );
    }
}
