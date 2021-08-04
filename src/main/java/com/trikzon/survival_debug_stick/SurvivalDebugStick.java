/*
 * Copyright 2020 Trikzon
 *
 * Survival Debug Stick is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * https://www.gnu.org/licenses/
 */
package com.trikzon.survival_debug_stick;

import com.trikzon.survival_debug_stick.mixin.ServerInteractManagerMixin;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

import java.util.HashMap;

public class SurvivalDebugStick implements ModInitializer
{
    HashMap<PlayerEntity, Integer> counters = new HashMap<>();

    @Override
    public void onInitialize()
    {
        // The way this event gets called is really, really odd and worries me a bit
        // If anyone has a better way to slow down the left click action of the stick
        // PLEASE PR it :)
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            Item itemInHand = player.getStackInHand(hand).getItem();
            if (itemInHand != Items.DEBUG_STICK || world.isClient) return ActionResult.PASS;

            if (player instanceof ServerPlayerEntity)
            {
                Integer counter = counters.getOrDefault(player, 0);
                if (counter == 0)
                {
                    ServerInteractManagerMixin manager = (ServerInteractManagerMixin) ((ServerPlayerEntity) player).interactionManager;
                    manager.setMiningPos(pos);
                    itemInHand.canMine(world.getBlockState(pos), world, pos, player);
                }

                if (counter >= 3)
                {
                    counter = 0;
                }
                else
                {
                    ++counter;
                }
                counters.put(player, counter);

                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }
}
