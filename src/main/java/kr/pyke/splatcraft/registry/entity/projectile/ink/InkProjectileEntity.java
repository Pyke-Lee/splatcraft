package kr.pyke.splatcraft.registry.entity.projectile.ink;

import kr.pyke.splatcraft.data.WeaponData;
import kr.pyke.splatcraft.util.InkHelper;
import kr.pyke.splatcraft.registry.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jspecify.annotations.NonNull;

public class InkProjectileEntity extends ThrowableProjectile {
    private byte teamID;
    private int splashRadius;
    private float damage;
    private int maxLife;
    private double gravity;
    private int life = 0;

    public InkProjectileEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
        applyDefaults();
    }

    public InkProjectileEntity(Level level, LivingEntity owner, byte teamId) {
        super(ModEntities.INK_PROJECTILE, level);
        applyDefaults();
        this.setOwner(owner);
        this.teamID = teamId;
        this.setPos(owner.getX(), owner.getEyeY(), owner.getZ());
    }

    private void applyDefaults() {
        WeaponData defaults = WeaponData.DEFAULT;
        this.teamID = 0;
        this.splashRadius = defaults.splashRadius();
        this.damage = defaults.damage();
        this.maxLife = defaults.maxProjectileLife();
        this.gravity = defaults.gravity();
    }

    public void setTeamID(byte teamID) {
        this.teamID = teamID;
    }

    public void setSplashRadius(int splashRadius) {
        this.splashRadius = splashRadius;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setMaxLife(int maxLife) {
        this.maxLife = maxLife;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    @Override protected double getDefaultGravity() { return gravity; }

    @Override
    public void tick() {
        super.tick();

        life++;
        if (life >= maxLife) {
            if (!level().isClientSide()) { applyInkSplash(blockPosition()); }
            discard();
            return;
        }

        if (level().isClientSide()) {
            level().addParticle(ParticleTypes.FALLING_WATER, getX(), getY(), getZ(), 0, 0, 0);
        }
    }

    @Override
    protected void onHitBlock(@NonNull BlockHitResult result) {
        super.onHitBlock(result);

        if (!level().isClientSide()) {
            InkHelper.tryApplyInk(level(), result.getBlockPos(), result.getDirection(), teamID);
            applyInkSplash(result.getBlockPos());
        }
        discard();
    }

    @Override
    protected void onHitEntity(@NonNull EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            result.getEntity().hurtServer(serverLevel, damageSources().thrown(this, getOwner()), damage);
            applyInkSplash(result.getEntity().blockPosition());
        }
        discard();
    }

    private void applyInkSplash(BlockPos center) {
        InkHelper.applySplash(level(), center, splashRadius, teamID);
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);

        WeaponData defaults = WeaponData.DEFAULT;
        teamID = input.getByteOr("TeamID", (byte) 0);
        splashRadius = input.getIntOr("SplashRadius", defaults.splashRadius());
        damage = input.getFloatOr("Damage", defaults.damage());
        maxLife = input.getIntOr("MaxLife", defaults.maxProjectileLife());
        gravity = input.getDoubleOr("Gravity", defaults.gravity());
        life = input.getIntOr("Life", 0);
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);

        output.putByte("TeamID", teamID);
        output.putInt("SplashRadius", splashRadius);
        output.putFloat("Damage", damage);
        output.putInt("MaxLife", maxLife);
        output.putDouble("Gravity", gravity);
        output.putInt("Life", life);
    }

    @Override protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) { }
}