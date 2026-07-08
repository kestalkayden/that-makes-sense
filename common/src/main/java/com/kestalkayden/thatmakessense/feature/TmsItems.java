package com.kestalkayden.thatmakessense.feature;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/**
 * Holder for That Makes Sense's own items. Registration is loader-specific (Fabric
 * {@code Registry.register}, NeoForge {@code DeferredRegister}), but the item id and its food
 * properties are shared here so both loaders build the same item and the furnace gate can identify it.
 */
public final class TmsItems {

    public static final String ZOMBIE_JERKY_PATH = "zombie_jerky";

    /** The registered Zombie Jerky item, assigned by each loader during mod init. */
    public static Item zombieJerky;

    private TmsItems() {}

    /** Zombie Jerky food: a small, always-edible snack (rotten flesh, cured). */
    public static FoodProperties zombieJerkyFood() {
        return new FoodProperties.Builder()
            .nutrition(1)
            .saturationModifier(0.5F)
            .alwaysEdible()
            .build();
    }
}
