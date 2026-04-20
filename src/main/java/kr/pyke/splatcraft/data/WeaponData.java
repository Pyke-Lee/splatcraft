package kr.pyke.splatcraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WeaponData(float projectileSpeed, float projectileSpread, int splashRadius, float damage, int maxProjectileLife, int cooldownTicks, double gravity) {
    public static final WeaponData DEFAULT = new WeaponData(1.5f, 1.f, 2, 4.f, 40, 4, 0.06);

    public static final Codec<WeaponData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.FLOAT.fieldOf("projectile_speed").forGetter(WeaponData::projectileSpeed),
        Codec.FLOAT.fieldOf("projectile_spread").forGetter(WeaponData::projectileSpread),
        Codec.INT.fieldOf("splash_radius").forGetter(WeaponData::splashRadius),
        Codec.FLOAT.fieldOf("damage").forGetter(WeaponData::damage),
        Codec.INT.fieldOf("max_projectile_life").forGetter(WeaponData::maxProjectileLife),
        Codec.INT.fieldOf("cooldown_ticks").forGetter(WeaponData::cooldownTicks),
        Codec.DOUBLE.fieldOf("gravity").forGetter(WeaponData::gravity)
    ).apply(instance, WeaponData::new));
}