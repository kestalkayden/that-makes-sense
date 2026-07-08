package com.kestalkayden.thatmakessense.client;

import java.util.Collections;
import java.util.List;

import com.kestalkayden.thatmakessense.config.ModConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Hand-built vanilla config screen for That Makes Sense - single-column, full-width rows grouped
 * under centred per-feature headers. No Cloth Config (which routinely lags new Minecraft releases):
 * this is a plain {@link Screen} + {@link ContainerObjectSelectionList} that works on any 26.2 build.
 *
 * <p>Lives in {@code :common/src/client} and is compiled into both loaders. On Fabric it lands in the
 * {@code client} source set and is opened from {@code ModMenuIntegration}; on NeoForge it lands in
 * {@code main} and is opened from the {@code IConfigScreenFactory} registered behind a
 * {@code Dist.CLIENT} guard, so a dedicated server never links a client class.
 *
 * <p>To add a feature: add a {@code addHeader(...)} + one or more {@code addOption(...)} calls in
 * {@link ConfigList#populateEntries()} and the matching keys to the lang file.
 */
public class ConfigScreen extends Screen {

    private static final Component TITLE = Component.translatable("thatmakessense.config.title");

    private final Screen parent;
    private ConfigList list;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    public ConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        layout.addTitleHeader(TITLE, font);
        list = layout.addToContents(new ConfigList(minecraft, width));
        layout.addToFooter(
                Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                      .width(200)
                      .build());
        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        if (list != null) {
            list.updateSize(width, layout);
        }
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    /**
     * Persist on close - Done, Escape, or any other navigation. The option listeners in
     * {@link ConfigList} have already written their values into the live {@link ModConfig}; this
     * flushes them to disk.
     */
    @Override
    public void removed() {
        ModConfig.save();
    }

    // =========================================================================
    // Custom scrolling list
    // =========================================================================

    static final class ConfigList extends ContainerObjectSelectionList<ConfigList.AbstractEntry> {

        private static final int ROW_WIDTH = 310;
        private static final int OPTION_ROW_HEIGHT = 25;
        private static final int HEADER_ROW_HEIGHT = 18;

        ConfigList(Minecraft minecraft, int screenWidth) {
            super(minecraft, screenWidth, 0, 0, OPTION_ROW_HEIGHT);
            this.centerListVertically = false;
            populateEntries();
        }

        @Override
        public int getRowWidth() {
            return ROW_WIDTH;
        }

        private void populateEntries() {
            ModConfig cfg = ModConfig.get();

            // ---- No Crop Trample ----
            addHeader("thatmakessense.config.section.noCropTrample");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.noCropTrample.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.noCropTrample.enabled.tooltip")),
                    cfg.noCropTrample.enabled,
                    val -> ModConfig.get().noCropTrample.enabled = val));

            // ---- Silk Touch -> Budding Amethyst ----
            addHeader("thatmakessense.config.section.silkTouchBudding");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.silkTouchBudding.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.silkTouchBudding.enabled.tooltip")),
                    cfg.silkTouchBudding.enabled,
                    val -> ModConfig.get().silkTouchBudding.enabled = val));

            // ---- Enchantments ----
            addHeader("thatmakessense.config.section.enchantments");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.enchantments.infinityMending",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.enchantments.infinityMending.tooltip")),
                    cfg.enchantmentTweaks.infinityMending,
                    val -> ModConfig.get().enchantmentTweaks.infinityMending = val));
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.enchantments.stackDamage",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.enchantments.stackDamage.tooltip")),
                    cfg.enchantmentTweaks.stackDamage,
                    val -> ModConfig.get().enchantmentTweaks.stackDamage = val));

            // ---- Cobblestone -> Blackstone ----
            addHeader("thatmakessense.config.section.blackstoneRecipe");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.blackstoneRecipe.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.blackstoneRecipe.enabled.tooltip")),
                    cfg.blackstoneRecipe.enabled,
                    val -> ModConfig.get().blackstoneRecipe.enabled = val));

            // ---- Double Doors ----
            addHeader("thatmakessense.config.section.doubleDoors");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.doubleDoors.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.doubleDoors.enabled.tooltip")),
                    cfg.doubleDoors.enabled,
                    val -> ModConfig.get().doubleDoors.enabled = val));

            // ---- No Berry Damage ----
            addHeader("thatmakessense.config.section.noBerryDamage");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.noBerryDamage.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.noBerryDamage.enabled.tooltip")),
                    cfg.noBerryDamage.enabled,
                    val -> ModConfig.get().noBerryDamage.enabled = val));

            // ---- Right-Click Harvest ----
            addHeader("thatmakessense.config.section.rightClickHarvest");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.rightClickHarvest.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.rightClickHarvest.enabled.tooltip")),
                    cfg.rightClickHarvest.enabled,
                    val -> ModConfig.get().rightClickHarvest.enabled = val));

            // ---- No Creeper Block Damage ----
            addHeader("thatmakessense.config.section.noCreeperBlockDamage");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.noCreeperBlockDamage.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.noCreeperBlockDamage.enabled.tooltip")),
                    cfg.noCreeperBlockDamage.enabled,
                    val -> ModConfig.get().noCreeperBlockDamage.enabled = val));

            // ---- No Enderman Griefing ----
            addHeader("thatmakessense.config.section.noEndermanGriefing");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.noEndermanGriefing.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.noEndermanGriefing.enabled.tooltip")),
                    cfg.noEndermanGriefing.enabled,
                    val -> ModConfig.get().noEndermanGriefing.enabled = val));

            // ---- Totem from Inventory ----
            addHeader("thatmakessense.config.section.totemFromInventory");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.totemFromInventory.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.totemFromInventory.enabled.tooltip")),
                    cfg.totemFromInventory.enabled,
                    val -> ModConfig.get().totemFromInventory.enabled = val));

            // ---- Disable Phantoms ----
            addHeader("thatmakessense.config.section.disablePhantoms");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.disablePhantoms.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.disablePhantoms.enabled.tooltip")),
                    cfg.disablePhantoms.enabled,
                    val -> ModConfig.get().disablePhantoms.enabled = val));

            // ---- No Pet Teleport Damage ----
            addHeader("thatmakessense.config.section.noPetTeleportDamage");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.noPetTeleportDamage.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.noPetTeleportDamage.enabled.tooltip")),
                    cfg.noPetTeleportDamage.enabled,
                    val -> ModConfig.get().noPetTeleportDamage.enabled = val));

            // ---- No Ender Pearl Damage ----
            addHeader("thatmakessense.config.section.noEnderPearlDamage");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.noEnderPearlDamage.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.noEnderPearlDamage.enabled.tooltip")),
                    cfg.noEnderPearlDamage.enabled,
                    val -> ModConfig.get().noEnderPearlDamage.enabled = val));

            // ---- Rotten Flesh -> Leather ----
            addHeader("thatmakessense.config.section.rottenFleshLeather");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.rottenFleshLeather.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.rottenFleshLeather.enabled.tooltip")),
                    cfg.rottenFleshLeather.enabled,
                    val -> ModConfig.get().rottenFleshLeather.enabled = val));

            // ---- Zombie Jerky ----
            addHeader("thatmakessense.config.section.zombieJerky");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.zombieJerky.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.zombieJerky.enabled.tooltip")),
                    cfg.zombieJerky.enabled,
                    val -> ModConfig.get().zombieJerky.enabled = val));

            // ---- Stackable Totems ----
            addHeader("thatmakessense.config.section.stackableTotems");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.stackableTotems.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.stackableTotems.enabled.tooltip")),
                    cfg.stackableTotems.enabled,
                    val -> ModConfig.get().stackableTotems.enabled = val));

            // ---- Hide Armor ----
            addHeader("thatmakessense.config.section.hideArmor");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.hideArmor.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.hideArmor.enabled.tooltip")),
                    cfg.hideArmor.enabled,
                    val -> ModConfig.get().hideArmor.enabled = val));

            // ---- No Ghast Fireball Damage ----
            addHeader("thatmakessense.config.section.noGhastFireballDamage");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.noGhastFireballDamage.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.noGhastFireballDamage.enabled.tooltip")),
                    cfg.noGhastFireballDamage.enabled,
                    val -> ModConfig.get().noGhastFireballDamage.enabled = val));

            // ---- Cobweb Shears ----
            addHeader("thatmakessense.config.section.cobwebShears");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.cobwebShears.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.cobwebShears.enabled.tooltip")),
                    cfg.cobwebShears.enabled,
                    val -> ModConfig.get().cobwebShears.enabled = val));

            // ---- Villager Stock ----
            addHeader("thatmakessense.config.section.villagerStock");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.villagerStock.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.villagerStock.enabled.tooltip")),
                    cfg.villagerStock.enabled,
                    val -> ModConfig.get().villagerStock.enabled = val));

            // ---- Bonemeal Extras ----
            addHeader("thatmakessense.config.section.bonemealExtras");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.bonemealExtras.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.bonemealExtras.enabled.tooltip")),
                    cfg.bonemealExtras.enabled,
                    val -> ModConfig.get().bonemealExtras.enabled = val));

            // ---- Stable Villager Prices ----
            addHeader("thatmakessense.config.section.stableVillagerPrices");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.stableVillagerPrices.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.stableVillagerPrices.enabled.tooltip")),
                    cfg.stableVillagerPrices.enabled,
                    val -> ModConfig.get().stableVillagerPrices.enabled = val));

            // ---- Longer Leads ----
            addHeader("thatmakessense.config.section.longerLeads");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.longerLeads.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.longerLeads.enabled.tooltip")),
                    cfg.longerLeads.enabled,
                    val -> ModConfig.get().longerLeads.enabled = val));

            // ---- Infinity on All Arrows ----
            addHeader("thatmakessense.config.section.infinityAllArrows");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.infinityAllArrows.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.infinityAllArrows.enabled.tooltip")),
                    cfg.infinityAllArrows.enabled,
                    val -> ModConfig.get().infinityAllArrows.enabled = val));

            // ---- Heal Parrots with Seeds ----
            addHeader("thatmakessense.config.section.healParrots");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.healParrots.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.healParrots.enabled.tooltip")),
                    cfg.healParrots.enabled,
                    val -> ModConfig.get().healParrots.enabled = val));

            // ---- Pet Damage Immunity ----
            addHeader("thatmakessense.config.section.petProtection");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.petProtection.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.petProtection.enabled.tooltip")),
                    cfg.petProtection.enabled,
                    val -> ModConfig.get().petProtection.enabled = val));

            // ---- No Pet Fall Damage ----
            addHeader("thatmakessense.config.section.noPetFallDamage");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.noPetFallDamage.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.noPetFallDamage.enabled.tooltip")),
                    cfg.noPetFallDamage.enabled,
                    val -> ModConfig.get().noPetFallDamage.enabled = val));

            // ---- No Villager Witch Conversion ----
            addHeader("thatmakessense.config.section.noVillagerWitch");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.noVillagerWitch.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.noVillagerWitch.enabled.tooltip")),
                    cfg.noVillagerWitch.enabled,
                    val -> ModConfig.get().noVillagerWitch.enabled = val));
        }

        private void addHeader(String langKey) {
            addEntry(new HeaderEntry(Component.translatable(langKey), minecraft), HEADER_ROW_HEIGHT);
        }

        private void addOption(OptionInstance<?> option) {
            addEntry(new OptionEntry(option, minecraft), OPTION_ROW_HEIGHT);
        }

        // =====================================================================
        // Entry types
        // =====================================================================

        abstract static class AbstractEntry extends ContainerObjectSelectionList.Entry<AbstractEntry> {
        }

        static final class HeaderEntry extends AbstractEntry {

            private final StringWidget widget;

            HeaderEntry(Component title, Minecraft minecraft) {
                this.widget = new StringWidget(ROW_WIDTH, 9, title, minecraft.font);
            }

            @Override
            public void extractContent(
                    GuiGraphicsExtractor graphics,
                    int mouseX, int mouseY,
                    boolean hovered,
                    float a) {
                int textWidth = this.widget.getWidth();
                int centreX = this.getContentXMiddle() - textWidth / 2;
                int centreY = this.getContentY() + (this.getContentHeight() - 9) / 2;
                this.widget.setPosition(centreX, centreY);
                this.widget.extractRenderState(graphics, mouseX, mouseY, a);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.emptyList();
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of(widget);
            }
        }

        static final class OptionEntry extends AbstractEntry {

            private final AbstractWidget widget;

            OptionEntry(OptionInstance<?> option, Minecraft minecraft) {
                this.widget = option.createButton(minecraft.options, 0, 0, ROW_WIDTH);
            }

            @Override
            public void extractContent(
                    GuiGraphicsExtractor graphics,
                    int mouseX, int mouseY,
                    boolean hovered,
                    float a) {
                int widgetX = this.getContentXMiddle() - this.widget.getWidth() / 2;
                this.widget.setPosition(widgetX, this.getContentY());
                this.widget.extractRenderState(graphics, mouseX, mouseY, a);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of(widget);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of(widget);
            }
        }
    }
}
