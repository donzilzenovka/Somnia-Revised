package com.kingrunes.somnia.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import com.kingrunes.somnia.common.tiredness.TirednessData;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class SomniaPlayerHandler {

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.entity;
            if (TirednessData.get(player) == null) {
                TirednessData.register(player);
            }
        }
    }
}
