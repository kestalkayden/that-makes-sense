package com.kestalkayden.thatmakessense;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kestalkayden.thatmakessense.client.ThatMakesSenseNeoForgeClient;
import com.kestalkayden.thatmakessense.config.ModConfig;
import com.kestalkayden.thatmakessense.feature.CopperChestMenus;
import com.kestalkayden.thatmakessense.feature.DoubleDoors;
import com.kestalkayden.thatmakessense.feature.NoBerryDamage;
import com.kestalkayden.thatmakessense.feature.NoEnderPearlDamage;
import com.kestalkayden.thatmakessense.feature.PetProtection;
import com.kestalkayden.thatmakessense.feature.RightClickHarvest;
import com.kestalkayden.thatmakessense.feature.TmsItems;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ThatMakesSenseNeoForge.MOD_ID)
public class ThatMakesSenseNeoForge {

    public static final String MOD_ID = "thatmakessense";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);

    /** The shared 8-row menu type for enlarged double copper chests (Copper Chest Rows). */
    public static final DeferredHolder<MenuType<?>, MenuType<ChestMenu>> COPPER_DOUBLE = MENUS.register(
        "copper_double",
        () -> IMenuTypeExtension.create((syncId, inv, extraData) -> CopperChestMenus.createClientMenu(syncId, inv)));

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);

    /** Zombie Jerky item (output of the Rotten Flesh -> Smoker recipe). */
    public static final DeferredHolder<Item, Item> ZOMBIE_JERKY = ITEMS.register(
        TmsItems.ZOMBIE_JERKY_PATH,
        () -> new Item(new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, TmsItems.ZOMBIE_JERKY_PATH)))
            .food(TmsItems.zombieJerkyFood())));

    public ThatMakesSenseNeoForge(ModContainer container, IEventBus modBus) {
        LOGGER.info("Initializing That Makes Sense (NeoForge)");
        ModConfig.load(FMLPaths.CONFIGDIR.get());

        // No Crop Trample: cancel the vanilla trample so farmland keeps its crops.
        NeoForge.EVENT_BUS.addListener(ThatMakesSenseNeoForge::onFarmlandTrample);
        // Double Doors: mirror a door right-click onto its neighbour.
        NeoForge.EVENT_BUS.addListener(ThatMakesSenseNeoForge::onRightClickBlock);
        // No Berry Damage + No Ender Pearl Damage: cancel the incoming damage.
        NeoForge.EVENT_BUS.addListener(ThatMakesSenseNeoForge::onLivingIncomingDamage);

        // Copper Chest Rows: register the shared 8-row menu type used by double copper chests, then
        // hand the registered type to the loader-agnostic holder once the registry is populated.
        MENUS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener((FMLCommonSetupEvent event) -> {
            CopperChestMenus.doubleMenu = COPPER_DOUBLE.get();
            TmsItems.zombieJerky = ZOMBIE_JERKY.get();
        });
        modBus.addListener(ThatMakesSenseNeoForge::onBuildCreativeTab);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            // Config screen + menu-screen wiring live in a client-only class reached solely through
            // this guarded invokestatic, so a dedicated server never links a Screen-referencing class.
            ThatMakesSenseNeoForgeClient.register(modBus, container);
        }
    }

    private static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (ModConfig.get().shouldPreventTrampling(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (RightClickHarvest.tryHarvest(event.getLevel(), event.getPos(), event.getEntity(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }
        DoubleDoors.onDoorUse(event.getLevel(), event.getPos(), event.getEntity(), event.getHand());
    }

    private static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (NoBerryDamage.shouldCancel(event.getSource(), event.getEntity())
                || NoEnderPearlDamage.shouldCancel(event.getSource())
                || PetProtection.shouldCancel(event.getSource(), event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ZOMBIE_JERKY.get());
        }
    }
}
