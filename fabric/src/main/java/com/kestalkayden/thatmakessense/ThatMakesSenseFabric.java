package com.kestalkayden.thatmakessense;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kestalkayden.thatmakessense.config.ModConfig;
import com.kestalkayden.thatmakessense.feature.DoubleDoors;
import com.kestalkayden.thatmakessense.feature.NoBerryDamage;
import com.kestalkayden.thatmakessense.feature.NoPetFallDamage;
import com.kestalkayden.thatmakessense.feature.PetProtection;
import com.kestalkayden.thatmakessense.feature.RightClickHarvest;
import com.kestalkayden.thatmakessense.feature.TmsItems;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ThatMakesSenseFabric implements ModInitializer {

    public static final String MOD_ID = "thatmakessense";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing That Makes Sense (Fabric)");
        ModConfig.load(FabricLoader.getInstance().getConfigDir());

        // No Crop Trample is applied by FarmBlockMixin, and the enchantment / silk-touch / blackstone
        // tweaks by their mixins. Those all consult ModConfig at runtime.

        // Zombie Jerky item (output of the Rotten Flesh -> Smoker recipe).
        // 1.21.8 requires the Item.Properties to carry its own id (via setId) before construction -
        // the Item constructor now eagerly derives its description id, rather than that being resolved
        // lazily from the registry key Registry.register binds it under afterwards.
        ResourceLocation jerkyId = ResourceLocation.fromNamespaceAndPath(MOD_ID, TmsItems.ZOMBIE_JERKY_PATH);
        ResourceKey<Item> jerkyKey = ResourceKey.create(Registries.ITEM, jerkyId);
        TmsItems.zombieJerky = Registry.register(BuiltInRegistries.ITEM, jerkyKey,
            new Item(new Item.Properties().food(TmsItems.zombieJerkyFood()).setId(jerkyKey)));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS)
            .register(entries -> entries.accept(new ItemStack(TmsItems.zombieJerky)));

        // Block right-clicks: try harvesting a mature crop first (consumes the click), otherwise mirror
        // double doors (never consumes - vanilla still toggles the clicked door).
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (RightClickHarvest.tryHarvest(world, hitResult.getBlockPos(), player, hand)) {
                return InteractionResult.SUCCESS;
            }
            DoubleDoors.onDoorUse(world, hitResult.getBlockPos(), player, hand);
            return InteractionResult.PASS;
        });

        // No Berry Damage: returning false disallows the incoming damage. No Ender Pearl Damage is
        // handled directly by ThrownEnderpearlMixin (there is no dedicated damage type to key off here).
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) ->
            !(NoBerryDamage.shouldCancel(source, entity)
              || PetProtection.shouldCancel(source, entity) || NoPetFallDamage.shouldCancel(source, entity)));
    }
}
