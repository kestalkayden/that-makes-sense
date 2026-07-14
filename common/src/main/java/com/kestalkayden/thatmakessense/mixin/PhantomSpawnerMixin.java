package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.PhantomSpawner;

/**
 * Disable Phantoms. {@link PhantomSpawner} is the sole insomnia spawn path; cancelling its tick stops
 * phantoms from ever spawning from lack of sleep. Command / spawn-egg phantoms are unaffected (they
 * bypass the spawner). Server-side, so a server's setting is authoritative.
 *
 * <p>1.21.1's {@code CustomSpawner.tick} returns an {@code int} (the count of entities spawned this
 * tick), not {@code void} - the injector needs a {@link CallbackInfoReturnable} to match, and cancels
 * by reporting zero spawns rather than a plain {@code ci.cancel()}.
 */
@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void thatmakessense$disablePhantoms(
            ServerLevel level, boolean spawnEnemies, boolean spawnFriendlies, CallbackInfoReturnable<Integer> cir) {
        if (ModConfig.get().disablePhantoms.enabled) {
            cir.setReturnValue(0);
        }
    }
}
