/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.lootrunpaths;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.WynntilsListScreen;
import com.wynntils.screens.base.widgets.BackButton;
import com.wynntils.screens.base.widgets.PageSelectorButton;
import com.wynntils.screens.base.widgets.ReloadButton;
import com.wynntils.screens.lootrunpaths.widgets.LootrunPathButton;
import com.wynntils.screens.wynntilsmenu.WynntilsMenuScreen;
import com.wynntils.services.lootrunpaths.LootrunPathInstance;
import com.wynntils.services.lootrunpaths.event.LootrunPathCacheRefreshEvent;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.TaskUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

public final class WynntilsLootrunPathsScreen extends WynntilsListScreen<LootrunPathInstance, LootrunPathButton> {
    private WynntilsLootrunPathsScreen() {
        super(Component.translatable("screens.wynntils.lootruns.name"));

        WynntilsMod.registerEventListener(this);
    }

    public static Screen create() {
        return new WynntilsLootrunPathsScreen();
    }

    @Override
    public void onClose() {
        WynntilsMod.unregisterEventListener(this);
        super.onClose();
    }

    @SubscribeEvent
    public void onLootrunCacheRefresh(LootrunPathCacheRefreshEvent event) {
        reloadElements();
    }

    @Override
    protected void doInit() {
        super.doInit();

        TaskUtils.runAsync(Services.LootrunPaths::refreshLootrunCache);

        this.addRenderableWidget(new BackButton(
                (int) ((Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 16) / 2f + offsetX),
                65 + offsetY,
                Texture.BACK_ARROW_OFFSET.width() / 2,
                Texture.BACK_ARROW_OFFSET.height(),
                WynntilsMenuScreen.create()));

        this.addRenderableWidget(new ReloadButton(
                Texture.CONTENT_BOOK_BACKGROUND.width() - 21 + offsetX,
                11 + offsetY,
                (int) (Texture.RELOAD_ICON_OFFSET.width() / 2f / 1.7f),
                (int) (Texture.RELOAD_ICON_OFFSET.height() / 1.7f),
                "lootrun",
                () -> TaskUtils.runAsync(Services.LootrunPaths::refreshLootrunCache)));

        this.addRenderableWidget(new PageSelectorButton(
                (int) (Texture.CONTENT_BOOK_BACKGROUND.width() / 2f
                        + 50
                        - Texture.FORWARD_ARROW_OFFSET.width() / 2f
                        + offsetX),
                Texture.CONTENT_BOOK_BACKGROUND.height() - 25 + offsetY,
                Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.FORWARD_ARROW_OFFSET.height(),
                false,
                this));
        this.addRenderableWidget(new PageSelectorButton(
                Texture.CONTENT_BOOK_BACKGROUND.width() - 50 + offsetX,
                Texture.CONTENT_BOOK_BACKGROUND.height() - 25 + offsetY,
                Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.FORWARD_ARROW_OFFSET.height(),
                true,
                this));
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (hovered instanceof LootrunPathButton lootrunPathButton) {
            List<Component> tooltipLines;

            LootrunPathInstance currentLootrun = Services.LootrunPaths.getCurrentLootrun();
            if (currentLootrun != null
                    && Objects.equals(lootrunPathButton.getLootrun().name(), currentLootrun.name())) {
                tooltipLines = List.of(
                        Component.literal(lootrunPathButton.getLootrun().name()).withStyle(ChatFormatting.BOLD),
                        Component.translatable("screens.wynntils.lootruns.lootrunButton.loaded")
                                .withStyle(ChatFormatting.YELLOW),
                        Component.translatable("screens.wynntils.lootruns.lootrunButton.viewInFolder")
                                .withStyle(ChatFormatting.GOLD),
                        Component.translatable("screens.wynntils.lootruns.lootrunButton.openOnMap")
                                .withStyle(ChatFormatting.BLUE),
                        Component.translatable("screens.wynntils.lootruns.lootrunButton.unload")
                                .withStyle(ChatFormatting.GREEN));
            } else {
                tooltipLines = List.of(
                        Component.literal(lootrunPathButton.getLootrun().name()).withStyle(ChatFormatting.BOLD),
                        Component.translatable("screens.wynntils.lootruns.lootrunButton.load")
                                .withStyle(ChatFormatting.GREEN),
                        Component.translatable("screens.wynntils.lootruns.lootrunButton.viewInFolder")
                                .withStyle(ChatFormatting.GOLD),
                        Component.translatable("screens.wynntils.lootruns.lootrunButton.openOnMap")
                                .withStyle(ChatFormatting.BLUE),
                        Component.translatable("screens.wynntils.lootruns.lootrunButton.remove")
                                .withStyle(ChatFormatting.RED));
            }

            guiGraphics.renderComponentTooltip(FontRenderer.getInstance().getFont(), tooltipLines, mouseX, mouseY);
            return;
        }

        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        renderBackgroundTexture(poseStack);

        renderTitle(poseStack, I18n.get("screens.wynntils.lootruns.name"));

        renderVersion(poseStack);

        renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

        if (elements.isEmpty()) {
            renderNoElementsHelper(poseStack, I18n.get("screens.wynntils.lootruns.noLootruns"));
        }

        renderDescription(poseStack);

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderDescription(PoseStack poseStack) {
        LootrunPathInstance currentLootrun = Services.LootrunPaths.getCurrentLootrun();
        if (currentLootrun != null) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(currentLootrun.name()),
                            20 + offsetX,
                            80 + offsetY,
                            CommonColors.BLACK,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.NONE,
                            1.4f);

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.lootruns.chests") + ": "
                                    + currentLootrun.chests().size()),
                            20 + offsetX,
                            99 + offsetY,
                            CommonColors.BLACK,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.NONE);

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.lootruns.notes") + ": "
                                    + currentLootrun.notes().size()),
                            20 + offsetX,
                            109 + offsetY,
                            CommonColors.BLACK,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.NONE);

            Position start = currentLootrun.path().points().getFirst();
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.lootruns.start") + ": "
                                    + String.format("[%d, %d, %d]", (int) start.x(), (int) start.y(), (int) start.z())),
                            20 + offsetX,
                            119 + offsetY,
                            CommonColors.BLACK,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.NONE);

            Position end = currentLootrun.path().points().getLast();
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.lootruns.end") + ": "
                                    + String.format("[%d, %d, %d]", (int) end.x(), (int) end.y(), (int) end.z())),
                            20 + offsetX,
                            129 + offsetY,
                            CommonColors.BLACK,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.NONE);
        } else {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.lootruns.description1")),
                            20 + offsetX,
                            Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 10 + offsetX,
                            80 + offsetY,
                            Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 30,
                            CommonColors.BLACK,
                            HorizontalAlignment.LEFT,
                            TextShadow.NONE);

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.lootruns.description2")),
                            20 + offsetX,
                            Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 10 + offsetX,
                            155 + offsetY,
                            Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 30,
                            CommonColors.BLACK,
                            HorizontalAlignment.LEFT,
                            TextShadow.NONE);
        }
    }

    @Override
    protected LootrunPathButton getButtonFromElement(int i) {
        int offset = i % getElementsPerPage();
        return new LootrunPathButton(
                (int) (Texture.CONTENT_BOOK_BACKGROUND.width() / 2f + 15 + offsetX),
                offset * 13 + 25 + offsetY,
                Texture.CONTENT_BOOK_BACKGROUND.width() / 2 - 37,
                9,
                elements.get(i),
                this);
    }

    @Override
    protected void reloadElementsList(String searchTerm) {
        elements.addAll(Services.LootrunPaths.getLootruns().stream()
                .filter(lootrunInstance -> StringUtils.partialMatch(lootrunInstance.name(), searchTerm))
                .toList());
    }
}
