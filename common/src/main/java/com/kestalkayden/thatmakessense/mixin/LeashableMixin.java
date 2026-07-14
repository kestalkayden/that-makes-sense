package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.world.entity.Leashable;

/**
 * Longer leads. 1.21.1 has no overridable {@code leashSnapDistance()} - the "too far" threshold is the
 * {@code LEASH_TOO_FAR_DIST} constant (10.0) inlined directly into the body of the static
 * {@link Leashable#tickLeash}, which compares the holder distance against it and drops the leash once
 * past it. We modify that inlined constant instead: raising it lets mobs wander further before the
 * lead snaps. The elastic pull-back distance ({@code LEASH_ELASTIC_DIST}, 6 blocks) is a separate
 * constant in the same method and is left untouched, so it's simply more rope. Mobs that override the
 * leash-distance behaviour entirely (e.g. happy ghasts, via {@code handleLeashAtDistance}) are
 * unaffected, matching the original intent. Leash logic runs server-side.
 */
@Mixin(Leashable.class)
public interface LeashableMixin {

    @ModifyConstant(method = "tickLeash", constant = @Constant(doubleValue = 10.0D))
    private static double thatmakessense$longerLeads(double original) {
        ModConfig.LongerLeads cfg = ModConfig.get().longerLeads;
        if (cfg.enabled) {
            // Never shorter than vanilla, so a stray small config value can't make leads worse.
            return Math.max(cfg.distance, 10.0);
        }
        return original;
    }
}
