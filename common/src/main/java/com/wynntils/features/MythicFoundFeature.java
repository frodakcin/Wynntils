/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.containers.event.MythicFoundEvent;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.items.game.AspectItem;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.InsulatorItem;
import com.wynntils.models.items.items.game.SimulatorItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.utils.mc.McUtils;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public class MythicFoundFeature extends Feature {
    private static final ResourceLocation MYTHIC_FOUND_CLASSIC_ID =
            ResourceLocation.fromNamespaceAndPath("wynntils", "misc.mythic-found-classic");
    private static final ResourceLocation MYTHIC_FOUND_MODERN_ID =
            ResourceLocation.fromNamespaceAndPath("wynntils", "misc.mythic-found-modern");

    @Persisted
    public final Config<Boolean> playSound = new Config<>(true);

    @Persisted
    private final Config<MythicSound> chestSound = new Config<>(MythicSound.MODERN);

    @Persisted
    private final Config<MythicSound> lootrunSound = new Config<>(MythicSound.MODERN);

    @Persisted
    private final Config<MythicSound> raidSound = new Config<>(MythicSound.MODERN);

    @Persisted
    public final Config<Boolean> showDryStreakMessage = new Config<>(true);

    @Persisted
    public final Config<Boolean> showAspectDryStreakMessage = new Config<>(true);

    @Persisted
    public final Config<Boolean> showTomeDryStreakMessage = new Config<>(false);

    @SubscribeEvent
    public void onMythicFound(MythicFoundEvent event) {
        ItemStack itemStack = event.getMythicBoxItem();

        if (playSound.get() || showDryStreakMessage.get()) {
            // Normal loot chest reward
            if (event.getMythicSource() == MythicFoundEvent.MythicSource.LOOT_CHEST) {
                Optional<GearBoxItem> gearBoxItem = Models.Item.asWynnItem(itemStack, GearBoxItem.class);
                if (gearBoxItem.isPresent()) {
                    if (gearBoxItem.get().getGearType() != GearType.MASTERY_TOME) {
                        if (playSound.get()) {
                            McUtils.playSoundAmbient(chestSound.get().getSoundEvent());
                        }

                        if (!showDryStreakMessage.get()) return;
                        sendNormalDryStreakMessage(StyledText.fromComponent(
                                event.getMythicBoxItem().getHoverName()));
                    }
                    return;
                }
            }

            // Lootrun rewards
            if (event.getMythicSource() == MythicFoundEvent.MythicSource.LOOTRUN_REWARD_CHEST) {
                boolean validLootrunMythic = false;
                Optional<GearItem> gearItem = Models.Item.asWynnItem(itemStack, GearItem.class);
                if (gearItem.isPresent()) {
                    validLootrunMythic = true;
                }

                Optional<InsulatorItem> insulatorItem = Models.Item.asWynnItem(itemStack, InsulatorItem.class);
                if (insulatorItem.isPresent()) {
                    validLootrunMythic = true;
                }

                Optional<SimulatorItem> simulatorItem = Models.Item.asWynnItem(itemStack, SimulatorItem.class);
                if (simulatorItem.isPresent()) {
                    validLootrunMythic = true;
                }

                if (validLootrunMythic) {
                    if (playSound.get()) {
                        McUtils.playSoundAmbient(lootrunSound.get().getSoundEvent());
                    }

                    if (!showDryStreakMessage.get()) return;
                    sendLootrunDryStreakMessage(
                            StyledText.fromComponent(event.getMythicBoxItem().getHoverName()));
                }
            }
        }

        // Raid rewards
        if (event.getMythicSource() == MythicFoundEvent.MythicSource.RAID_REWARD_CHEST) {
            if (showAspectDryStreakMessage.get() || playSound.get()) {
                Optional<AspectItem> aspectItem = Models.Item.asWynnItem(itemStack, AspectItem.class);
                if (aspectItem.isPresent()) {
                    if (playSound.get()) {
                        McUtils.playSoundAmbient(raidSound.get().getSoundEvent());
                    }
                    if (showAspectDryStreakMessage.get()) {
                        sendAspectDryStreakMessage(StyledText.fromComponent(
                                event.getMythicBoxItem().getHoverName()));
                    }
                    return;
                }
            }

            if (showTomeDryStreakMessage.get() || playSound.get()) {
                Optional<TomeItem> tomeItem = Models.Item.asWynnItem(itemStack, TomeItem.class);
                if (tomeItem.isPresent()) {
                    if (playSound.get()) {
                        McUtils.playSoundAmbient(raidSound.get().getSoundEvent());
                    }
                    if (showTomeDryStreakMessage.get()) {
                        sendTomeDryStreakMessage(StyledText.fromComponent(
                                event.getMythicBoxItem().getHoverName()));
                    }
                    return;
                }
            }
        }
    }

    private void sendLootrunDryStreakMessage(StyledText itemName) {
        McUtils.sendMessageToClient(Component.literal("Dry streak broken! Found an ")
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(itemName.getComponent())
                .append(Component.literal(" after ")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                        .append(Component.literal(Models.Lootrun.dryPulls.get() + " pulls")
                                .withStyle(ChatFormatting.GOLD)))
                .append(Component.literal(" without a mythic.").withStyle(ChatFormatting.LIGHT_PURPLE)));
    }

    private static void sendNormalDryStreakMessage(StyledText itemName) {
        McUtils.sendMessageToClient(Component.literal("Dry streak broken! Found an ")
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(itemName.getComponent())
                .append(Component.literal(" in chest ")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                        .append(Component.literal("#" + Models.LootChest.getOpenedChestCount())
                                .withStyle(ChatFormatting.GOLD)))
                .append(Component.literal(" after ")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                        .append(Component.literal(String.valueOf(Models.LootChest.getDryCount()))
                                .withStyle(ChatFormatting.DARK_PURPLE)))
                .append(Component.literal(" dry chests and "))
                .append(Component.literal(String.valueOf(Models.LootChest.getDryBoxes()))
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.literal(" dry boxes.")));
    }

    private static void sendAspectDryStreakMessage(StyledText itemName) {
        McUtils.sendMessageToClient(Component.literal("Dry streak broken! Found ")
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(itemName.getComponent())
                .append(Component.literal(" after ")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                        .append(Component.literal(
                                        String.valueOf(Models.Raid.getNumRaidsWithoutMythicAspect()) + " raids")
                                .withStyle(ChatFormatting.GOLD)))
                .append(Component.literal(" and ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(
                                String.valueOf(Models.Raid.getNumAspectPullsWithoutMythicAspect()) + " aspect pulls")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" without a mythic.").withStyle(ChatFormatting.LIGHT_PURPLE)));
    }

    private static void sendTomeDryStreakMessage(StyledText itemName) {
        McUtils.sendMessageToClient(Component.literal("Dry streak broken! Found ")
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(itemName.getComponent())
                .append(Component.literal(" after ")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                        .append(Component.literal(String.valueOf(Models.Raid.getNumRaidsWithoutMythicTome()) + " raids")
                                .withStyle(ChatFormatting.GOLD)))
                .append(Component.literal(" and ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(
                                String.valueOf(Models.Raid.getNumRewardPullsWithoutMythicTome()) + " reward pulls")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" without a mythic."))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    private enum MythicSound {
        MODERN(SoundEvent.createVariableRangeEvent(MYTHIC_FOUND_MODERN_ID)),
        CLASSIC(SoundEvent.createVariableRangeEvent(MYTHIC_FOUND_CLASSIC_ID));

        private final SoundEvent soundEvent;

        MythicSound(SoundEvent soundEvent) {
            this.soundEvent = soundEvent;
        }

        public SoundEvent getSoundEvent() {
            return soundEvent;
        }
    }
}
