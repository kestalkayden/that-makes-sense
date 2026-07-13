package com.kestalkayden.thatmakessense.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.world.entity.Entity;

/**
 * Loader-agnostic config for That Makes Sense, stored as pretty-printed JSON at
 * {@code config/thatmakessense.json}.
 *
 * <p>This class lives in {@code :common} and is compiled into both loader jars via {@code srcDir}.
 * It touches no Fabric/NeoForge API - each loader passes its own config directory into
 * {@link #load(Path)} at startup, and {@link #load(Path)} remembers the resolved path so the
 * in-game screen can call the no-arg {@link #save()} on close.
 *
 * <p>The schema is one nested object per feature so new quality-of-life toggles slot in without
 * disturbing existing user configs. Feature code asks its dedicated decision method (e.g.
 * {@link #shouldPreventTrampling(Entity)}) rather than reading raw fields, keeping per-feature
 * policy in one place.
 */
public final class ModConfig {

    // -------------------------------------------------------------------------
    // Schema - one nested category per feature
    // -------------------------------------------------------------------------

    public NoCropTrample noCropTrample = new NoCropTrample();

    /** No Crop Trample: farmland never reverts to dirt from entities landing on it. */
    public static final class NoCropTrample {
        public boolean enabled = true;
    }

    public SilkTouchBudding silkTouchBudding = new SilkTouchBudding();

    /** Silk Touch -> Budding Amethyst: a Silk Touch tool harvests budding amethyst blocks. */
    public static final class SilkTouchBudding {
        public boolean enabled = true;
    }

    public EnchantmentTweaks enchantmentTweaks = new EnchantmentTweaks();

    /** Lifts vanilla enchantment mutual-exclusivity for a couple of common combos. */
    public static final class EnchantmentTweaks {
        /** Allow Infinity and Mending together on a bow. */
        public boolean infinityMending = true;
        /** Allow the melee damage enchantments (Sharpness, Smite, ...) to coexist on one weapon. */
        public boolean stackDamage = true;
    }

    public BlackstoneRecipe blackstoneRecipe = new BlackstoneRecipe();

    /** Cobblestone -> Blackstone in a blast furnace. Gated server-side, so a server's setting wins. */
    public static final class BlackstoneRecipe {
        public boolean enabled = true;
    }

    public DoubleDoors doubleDoors = new DoubleDoors();

    /** Opening one door of a matching pair opens both. */
    public static final class DoubleDoors {
        public boolean enabled = true;
    }

    public NoBerryDamage noBerryDamage = new NoBerryDamage();

    /** Sweet berry bushes stop hurting players. */
    public static final class NoBerryDamage {
        public boolean enabled = true;
    }

    public RightClickHarvest rightClickHarvest = new RightClickHarvest();

    /** Right-clicking a mature crop harvests it and replants it in place. */
    public static final class RightClickHarvest {
        public boolean enabled = true;
    }

    public NoCreeperBlockDamage noCreeperBlockDamage = new NoCreeperBlockDamage();

    /** Creeper explosions hurt entities but leave terrain intact. */
    public static final class NoCreeperBlockDamage {
        public boolean enabled = true;
    }

    public NoEndermanGriefing noEndermanGriefing = new NoEndermanGriefing();

    /** Endermen no longer pick up or place blocks. */
    public static final class NoEndermanGriefing {
        public boolean enabled = true;
    }

    public TotemFromInventory totemFromInventory = new TotemFromInventory();

    /** A Totem of Undying works from anywhere in the inventory, not just a hand. */
    public static final class TotemFromInventory {
        public boolean enabled = true;
    }

    public DisablePhantoms disablePhantoms = new DisablePhantoms();

    /** Phantoms never spawn from insomnia. */
    public static final class DisablePhantoms {
        public boolean enabled = true;
    }

    public NoPetTeleportDamage noPetTeleportDamage = new NoPetTeleportDamage();

    /** Tamed pets take no fall damage when they teleport to their owner. */
    public static final class NoPetTeleportDamage {
        public boolean enabled = true;
    }

    public NoEnderPearlDamage noEnderPearlDamage = new NoEnderPearlDamage();

    /** Throwing an ender pearl no longer deals teleport damage. */
    public static final class NoEnderPearlDamage {
        public boolean enabled = true;
    }

    public RottenFleshLeather rottenFleshLeather = new RottenFleshLeather();

    /** Smelting rotten flesh in a furnace yields leather. */
    public static final class RottenFleshLeather {
        public boolean enabled = true;
    }

    public ZombieJerky zombieJerky = new ZombieJerky();

    /** Smoking rotten flesh yields Zombie Jerky (a small food item). */
    public static final class ZombieJerky {
        public boolean enabled = true;
    }

    public StackableTotems stackableTotems = new StackableTotems();

    /** Totems of Undying stack (up to a full stack) instead of being capped at one. */
    public static final class StackableTotems {
        public boolean enabled = true;
    }

    public HideArmor hideArmor = new HideArmor();

    /**
     * Hide your own armor from view while it stays fully equipped. Client-side cosmetic only.
     * {@code enabled} gates the inventory button; {@code hidden} is the live toggle the button flips
     * and is persisted so the preference survives relaunches.
     */
    public static final class HideArmor {
        public boolean enabled = true;
        public boolean hidden = false;
    }

    public NoGhastFireballDamage noGhastFireballDamage = new NoGhastFireballDamage();

    /** Ghast fireball explosions stop breaking terrain (they still hurt entities). */
    public static final class NoGhastFireballDamage {
        public boolean enabled = true;
    }

    public CobwebShears cobwebShears = new CobwebShears();

    /** Shears break cobweb instantly. */
    public static final class CobwebShears {
        public boolean enabled = true;
    }

    public VillagerStock villagerStock = new VillagerStock();

    /**
     * Villagers sell each trade {@code multiplier} times as often before it locks (they still restock).
     * {@code multiplier} is edit-in-file (defaults to 4x); the screen exposes only the on/off toggle.
     */
    public static final class VillagerStock {
        public boolean enabled = true;
        public int multiplier = 2;
    }

    public BonemealExtras bonemealExtras = new BonemealExtras();

    /** Bonemeal grows nether wart, cactus, and sugar cane. */
    public static final class BonemealExtras {
        public boolean enabled = true;
    }

    public StableVillagerPrices stableVillagerPrices = new StableVillagerPrices();

    /** Villager trade prices stop climbing from demand (heavy-use inflation); discounts still apply. */
    public static final class StableVillagerPrices {
        public boolean enabled = true;
    }

    public LongerLeads longerLeads = new LongerLeads();

    /** Leads break at {@code distance} blocks instead of vanilla's 12 (elastic pull still starts at 6). */
    public static final class LongerLeads {
        public boolean enabled = true;
        public double distance = 24.0;
    }

    public InfinityAllArrows infinityAllArrows = new InfinityAllArrows();

    /** Infinity works on every arrow type (tipped, spectral), not just the plain arrow. */
    public static final class InfinityAllArrows {
        public boolean enabled = true;
    }

    public HealParrots healParrots = new HealParrots();

    /** An injured tamed parrot heals when fed seeds (parrots otherwise have no heal path). */
    public static final class HealParrots {
        public boolean enabled = true;
    }

    public PetProtection petProtection = new PetProtection();

    /** Tamed pets take no damage from their owner or the owner's other tamed pets. */
    public static final class PetProtection {
        public boolean enabled = true;
    }

    public NoPetFallDamage noPetFallDamage = new NoPetFallDamage();

    /** Tamed pets (wolves, cats, parrots) take no fall damage. */
    public static final class NoPetFallDamage {
        public boolean enabled = true;
    }

    public NoVillagerWitch noVillagerWitch = new NoVillagerWitch();

    /** Villagers struck by lightning don't convert into witches. */
    public static final class NoVillagerWitch {
        public boolean enabled = true;
    }

    public ChestsUnderBlocks chestsUnderBlocks = new ChestsUnderBlocks();

    /** Chests open even with a solid block directly above them (the cat-on-chest block still applies). */
    public static final class ChestsUnderBlocks {
        public boolean enabled = true;
    }

    public FasterNetherPortal fasterNetherPortal = new FasterNetherPortal();

    /** Time (ticks) a player waits in a nether portal before travelling, capped down from vanilla ~80. */
    public static final class FasterNetherPortal {
        public boolean enabled = true;
        public int delayTicks = 0;
    }

    public NoAnvilCap noAnvilCap = new NoAnvilCap();

    /** Removes the anvil "Too Expensive!" level cap so pricey combines/repairs still produce a result. */
    public static final class NoAnvilCap {
        public boolean enabled = true;
    }

    public MegaTrees megaTrees = new MegaTrees();

    /** 2x2 saplings grow a giant tree for species vanilla doesn't cover (birch, oak, cherry). */
    public static final class MegaTrees {
        public boolean enabled = true;
    }

    // -------------------------------------------------------------------------
    // Feature decision points
    // -------------------------------------------------------------------------

    /**
     * Single decision point for No Crop Trample. Later revisions can extend this with per-entity
     * toggles (player / mob / projectile) by inspecting {@code entity} without touching callers.
     */
    public boolean shouldPreventTrampling(Entity entity) {
        return noCropTrample.enabled;
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "thatmakessense.json";

    /** Resolved config file, set by {@link #load(Path)}; null until the mod initializes. */
    private static Path path;

    /** The live singleton. Mutate fields directly, then call {@link #save()}. */
    private static ModConfig instance = new ModConfig();

    public static ModConfig get() {
        return instance;
    }

    /**
     * Load (or create) {@code <configDir>/thatmakessense.json}. A missing or unreadable file falls
     * back to defaults, which are then written so the file is self-documenting on first run. Always
     * leaves {@link #instance} non-null with every nested object present.
     */
    public static ModConfig load(Path configDir) {
        path = configDir.resolve(FILE_NAME);
        ModConfig cfg = new ModConfig();
        if (Files.exists(path)) {
            try (Reader r = Files.newBufferedReader(path)) {
                ModConfig loaded = GSON.fromJson(r, ModConfig.class);
                if (loaded != null) {
                    cfg = loaded;
                }
            } catch (IOException | RuntimeException e) {
                // Keep defaults; the broken file is overwritten by the save() below.
            }
        }
        cfg.fillMissing();
        instance = cfg;
        save();
        return instance;
    }

    /** Persist the live instance to disk. No-op before {@link #load(Path)} has run. */
    public static void save() {
        if (path == null) {
            return;
        }
        try {
            Files.createDirectories(path.getParent());
            try (Writer w = Files.newBufferedWriter(path)) {
                GSON.toJson(instance, w);
            }
        } catch (IOException e) {
            // Non-fatal: the in-memory state is already correct.
        }
    }

    /** Replace any null nested object (e.g. a partial or older JSON file) with its default. */
    private void fillMissing() {
        if (noCropTrample == null) {
            noCropTrample = new NoCropTrample();
        }
        if (silkTouchBudding == null) {
            silkTouchBudding = new SilkTouchBudding();
        }
        if (enchantmentTweaks == null) {
            enchantmentTweaks = new EnchantmentTweaks();
        }
        if (blackstoneRecipe == null) {
            blackstoneRecipe = new BlackstoneRecipe();
        }
        if (doubleDoors == null) {
            doubleDoors = new DoubleDoors();
        }
        if (noBerryDamage == null) {
            noBerryDamage = new NoBerryDamage();
        }
        if (rightClickHarvest == null) {
            rightClickHarvest = new RightClickHarvest();
        }
        if (noCreeperBlockDamage == null) {
            noCreeperBlockDamage = new NoCreeperBlockDamage();
        }
        if (noEndermanGriefing == null) {
            noEndermanGriefing = new NoEndermanGriefing();
        }
        if (totemFromInventory == null) {
            totemFromInventory = new TotemFromInventory();
        }
        if (disablePhantoms == null) {
            disablePhantoms = new DisablePhantoms();
        }
        if (noPetTeleportDamage == null) {
            noPetTeleportDamage = new NoPetTeleportDamage();
        }
        if (noEnderPearlDamage == null) {
            noEnderPearlDamage = new NoEnderPearlDamage();
        }
        if (rottenFleshLeather == null) {
            rottenFleshLeather = new RottenFleshLeather();
        }
        if (zombieJerky == null) {
            zombieJerky = new ZombieJerky();
        }
        if (stackableTotems == null) {
            stackableTotems = new StackableTotems();
        }
        if (hideArmor == null) {
            hideArmor = new HideArmor();
        }
        if (noGhastFireballDamage == null) {
            noGhastFireballDamage = new NoGhastFireballDamage();
        }
        if (cobwebShears == null) {
            cobwebShears = new CobwebShears();
        }
        if (villagerStock == null) {
            villagerStock = new VillagerStock();
        }
        if (bonemealExtras == null) {
            bonemealExtras = new BonemealExtras();
        }
        if (stableVillagerPrices == null) {
            stableVillagerPrices = new StableVillagerPrices();
        }
        if (longerLeads == null) {
            longerLeads = new LongerLeads();
        }
        if (infinityAllArrows == null) {
            infinityAllArrows = new InfinityAllArrows();
        }
        if (healParrots == null) {
            healParrots = new HealParrots();
        }
        if (petProtection == null) {
            petProtection = new PetProtection();
        }
        if (noPetFallDamage == null) {
            noPetFallDamage = new NoPetFallDamage();
        }
        if (noVillagerWitch == null) {
            noVillagerWitch = new NoVillagerWitch();
        }
        if (chestsUnderBlocks == null) {
            chestsUnderBlocks = new ChestsUnderBlocks();
        }
        if (fasterNetherPortal == null) {
            fasterNetherPortal = new FasterNetherPortal();
        }
        if (noAnvilCap == null) {
            noAnvilCap = new NoAnvilCap();
        }
        if (megaTrees == null) {
            megaTrees = new MegaTrees();
        }
    }
}
