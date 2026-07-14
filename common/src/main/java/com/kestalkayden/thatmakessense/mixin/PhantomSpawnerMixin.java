package com.kestalkayden.thatmakessense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.PhantomSpawner;

/**
 * Disable Phantoms. {@link PhantomSpawner} is the sole insomnia spawn path; cancelling its tick stops
 * phantoms from ever spawning from lack of sleep. Command / spawn-egg phantoms are unaffected (they
 * bypass the spawner). Server-side, so a server's setting is authoritative.
 *
 * <p>1.21.1's {@code CustomSpawner.tick} returns an {@code int} (the count of entities spawned this
 * tick); 1.21.8 changed the return type to {@code void} while keeping the same three parameters
 * ({@code ServerLevel, spawnEnemies, spawnFriendlies}) - so the injector now takes a plain
 * {@link CallbackInfo} and cancels with {@code ci.cancel()} instead of reporting zero spawns.
 */
@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void thatmakessense$disablePhantoms(
            ServerLevel level, boolean spawnEnemies, boolean spawnFriendlies, CallbackInfo ci) {
        if (ModConfig.get().disablePhantoms.enabled) {
            ci.cancel();
        }
    }
}
