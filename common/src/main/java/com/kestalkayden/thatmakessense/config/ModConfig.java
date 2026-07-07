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
    }
}
