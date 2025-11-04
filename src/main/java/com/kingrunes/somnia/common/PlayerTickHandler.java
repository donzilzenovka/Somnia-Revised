package com.kingrunes.somnia.common;

import net.minecraft.entity.player.EntityPlayer;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.util.ClassUtils;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

public class PlayerTickHandler {

    public static class State {

        boolean sleepOverride = false;
    }

    private final State clientState = new State();
    private final State serverState = new State();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        State state = event.side == Side.CLIENT ? clientState : serverState;

        if (event.phase == TickEvent.Phase.START) {
            tickStart(state, event.player);
        } else {
            tickEnd(state, event.player);
        }
    }

    private void tickStart(State state, EntityPlayer player) {
        if (player.isPlayerSleeping()) {
            state.sleepOverride = true;
            ClassUtils.setSleeping(player, false);

            if (Somnia.proxy.fading) {
                int sleepTimer = player.getSleepTimer() + 1;
                if (sleepTimer >= 99) sleepTimer = 98;
                ClassUtils.setSleepTimer(player, sleepTimer);
            }
        }
    }

    private void tickEnd(State state, EntityPlayer player) {
        if (state.sleepOverride) {
            ClassUtils.setSleeping(player, true);
            state.sleepOverride = false;
        }
    }
}
