package com.kestalkayden.thatmakessense.feature;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * No Berry Damage: sweet berry bushes stop hurting players who walk through them (mobs still take it,
 * so the bushes remain a soft AI barrier). Loader-agnostic decision - each loader's damage hook
 * (Fabric {@code ServerLivingEntityEvents.ALLOW_DAMAGE}, NeoForge {@code LivingIncomingDamageEvent})
 * calls {@link #shouldCancel} and cancels when it returns true.
 */
public final class NoBerryDamage {

    private NoBerryDamage() {}

    /** True when this is sweet-berry-bush damage to a player and the feature is enabled. */
    public static boolean shouldCancel(DamageSource source, LivingEntity entity) {
        return ModConfig.get().noBerryDamage.enabled
            && entity instanceof Player
            && source.is(DamageTypes.SWEET_BERRY_BUSH);
    }
}
