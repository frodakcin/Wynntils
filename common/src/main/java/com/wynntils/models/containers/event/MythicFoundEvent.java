/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class MythicFoundEvent extends Event {
    public enum MythicSource {
        LOOT_CHEST,
        LOOTRUN_REWARD_CHEST,
        RAID_REWARD_CHEST
    }

    private final ItemStack mythicBoxItem;
    private final MythicSource mythicSource;

    public MythicFoundEvent(ItemStack mythicBoxItem, MythicSource mythicSource) {
        this.mythicBoxItem = mythicBoxItem;
        this.mythicSource = mythicSource;
    }

    public ItemStack getMythicBoxItem() {
        return mythicBoxItem;
    }

    public boolean isFromLootChest() {
        return mythicSource == MythicSource.LOOT_CHEST;
    }

    public boolean isLootrunEndReward() {
        return mythicSource == MythicSource.LOOTRUN_REWARD_CHEST;
    }

    public boolean isRaidEndReward() {
        return mythicSource == MythicSource.RAID_REWARD_CHEST;
    }
}
