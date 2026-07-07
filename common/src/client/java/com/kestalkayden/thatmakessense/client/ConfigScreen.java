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

            // ---- Copper Chest Rows ----
            addHeader("thatmakessense.config.section.copperChest");
            addOption(OptionInstance.createBoolean(
                    "thatmakessense.config.copperChest.enabled",
                    OptionInstance.cachedConstantTooltip(
                            Component.translatable("thatmakessense.config.copperChest.enabled.tooltip")),
                    cfg.copperChest.enabled,
                    val -> ModConfig.get().copperChest.enabled = val));
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
