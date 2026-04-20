package kr.pyke.splatcraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TeamColorData(byte teamID, String colorHex, String name) {
    public static final Codec<TeamColorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BYTE.fieldOf("team_id").forGetter(TeamColorData::teamID),
        Codec.STRING.fieldOf("color").forGetter(TeamColorData::colorHex),
        Codec.STRING.fieldOf("name").forGetter(TeamColorData::name)
    ).apply(instance, TeamColorData::new));

    public int argb() { return (int) Long.parseLong(colorHex, 16); }
}