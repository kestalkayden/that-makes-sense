package com.kestalkayden.thatmakessense;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kestalkayden.thatmakessense.config.ModConfig;
import com.kestalkayden.thatmakessense.feature.CopperChestMenus;
import com.kestalkayden.thatmakessense.feature.DoubleDoors;
import com.kestalkayden.thatmakessense.feature.NoBerryDamage;
import com.kestalkayden.thatmakessense.feature.RightClickHarvest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

public class ThatMakesSenseFabric implements ModInitializer {

    public static final String MOD_ID = "thatmakessense";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing That Makes Sense (Fabric)");
        ModConfig.load(FabricLoader.getInstance().getConfigDir());

        // No Crop Trample is applied by FarmlandBlockMixin, Copper Chest Rows by ChestBlockEntity/
        // ChestBlock mixins, and the enchantment / silk-touch / blackstone tweaks by their mixins.
        // Those all consult ModConfig at runtime.

        // Copper Chest Rows (double): register the shared 8-row menu type. The screen is bound
        // client-side in ThatMakesSenseFabricClient; the server builds the menu in ChestBlockMixin.
        MenuType<ChestMenu> menu = new MenuType<>(CopperChestMenus::createClientMenu, FeatureFlags.VANILLA_SET);
        CopperChestMenus.doubleMenu = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath(MOD_ID, "copper_double"),
            menu);

        // Block right-clicks: try harvesting a mature crop first (consumes the click), otherwise mirror
        // double doors (never consumes - vanilla still toggles the clicked door).
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (RightClickHarvest.tryHarvest(world, hitResult.getBlockPos(), player, hand)) {
                return InteractionResult.SUCCESS;
            }
            DoubleDoors.onDoorUse(world, hitResult.getBlockPos(), player, hand);
            return InteractionResult.PASS;
        });

        // No Berry Damage: returning false disallows the incoming damage.
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(
            (entity, source, amount) -> !NoBerryDamage.shouldCancel(source, entity));
    }
}
