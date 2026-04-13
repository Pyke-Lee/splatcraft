package kr.pyke.splatcraft.registry.item.watergun;

import kr.pyke.splatcraft.data.WeaponData;
import kr.pyke.splatcraft.manager.WeaponDataManager;
import kr.pyke.splatcraft.registry.entity.projectile.ink.InkProjectileEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class WaterGunItem extends Item {
    public WaterGunItem(Properties properties) {
        super(properties);
    }

    private WeaponData getWeaponData() {
        Identifier itemID = BuiltInRegistries.ITEM.getKey(this);

        return WeaponDataManager.get(itemID);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level instanceof ServerLevel serverLevel) {
            WeaponData data = getWeaponData();
            byte teamId = 1;

            InkProjectileEntity projectile = new InkProjectileEntity(level, player, teamId);
            projectile.setSplashRadius(data.splashRadius());
            projectile.setDamage(data.damage());
            projectile.setMaxLife(data.maxProjectileLife());

            Projectile.spawnProjectile(projectile, serverLevel, stack, p -> p.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.f, data.projectileSpeed(), data.projectileSpread()));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));

            player.getCooldowns().addCooldown(stack, data.cooldownTicks());
        }

        return InteractionResult.SUCCESS;
    }
}