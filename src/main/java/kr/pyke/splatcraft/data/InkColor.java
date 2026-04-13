package kr.pyke.splatcraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record InkColor(byte id, int rgb, String name) {
    public static final InkColor NONE = new InkColor((byte) 0, 0x000000, "none");

    public static final Codec<InkColor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BYTE.fieldOf("id").forGetter(InkColor::id),
        Codec.INT.fieldOf("rgb").forGetter(InkColor::rgb),
        Codec.STRING.fieldOf("name").forGetter(InkColor::name)
    ).apply(instance, InkColor::new));

    public static final StreamCodec<FriendlyByteBuf, InkColor> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BYTE, InkColor::id,
        ByteBufCodecs.INT, InkColor::rgb,
        ByteBufCodecs.STRING_UTF8, InkColor::name,
        InkColor::new
    );

    public int argb() { return 0xFF000000 | rgb; }

    public float redF() { return ((rgb >> 16) & 0xFF) / 255.f; }

    public float greenF() { return ((rgb >> 8) & 0xFF) / 255.f; }

    public float blueF() { return (rgb & 0xFF) / 255.f; }
}
