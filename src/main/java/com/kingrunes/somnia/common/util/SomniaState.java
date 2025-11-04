package com.kingrunes.somnia.common.util;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.server.ServerTickHandler;

/**
 * Represents the current Somnia sleep state.
 */
public enum SomniaState {

    IDLE,
    ACTIVE,
    WAITING_PLAYERS,
    EXPIRED,
    NOT_NOW,
    COOLDOWN;

    /**
     * Determines the current SomniaState based on the server tick handler.
     *
     * @param handler the server tick handler managing sleep
     * @return the corresponding SomniaState
     */
    public static SomniaState getState(ServerTickHandler handler) {
        // Check if the sleep period has expired
        if (handler.currentSleepPeriod > Somnia.proxy.maxSleepTimePeriod) {
            return EXPIRED;
        }

        long totalWorldTime = handler.worldServer.getWorldTime();

        // Check cooldown between sleep periods
        if (handler.currentState != ACTIVE && handler.lastSleepStart > 0L
            && totalWorldTime - handler.lastSleepStart < Somnia.proxy.sleepCooldown) {
            return COOLDOWN;
        }

        // Check if current time is within the valid sleep period
        if (!Somnia.proxy.validSleepPeriod.isTimeWithin(totalWorldTime % 24000L)) {
            return NOT_NOW;
        }

        List<net.minecraft.entity.player.EntityPlayer> players = handler.worldServer.playerEntities;
        if (players.isEmpty()) {
            return IDLE;
        }

        boolean anySleeping = false;
        boolean allSleeping = true;

        // 2. Change the loop variable type and safely cast inside the loop:
        for (net.minecraft.entity.player.EntityPlayer playerEntity : players) {
            // Since we are on the server (WorldServer), we can safely cast:
            EntityPlayerMP player = (EntityPlayerMP) playerEntity;

            // Use the EntityPlayerMP variable 'player' for the logic
            boolean sleeping = player.isPlayerSleeping() || ListUtils.containsRef(player, Somnia.instance.ignoreList);
            anySleeping |= sleeping;
            allSleeping &= sleeping;
        }

        if (allSleeping) return ACTIVE;
        if (anySleeping) return WAITING_PLAYERS;

        return IDLE;
    }
}
