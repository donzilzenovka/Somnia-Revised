package com.kingrunes.somnia.common.tiredness;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import com.kingrunes.somnia.Somnia;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class SomniaTirednessHandler {

    private static final float BASE_FATIGUE = 0.001F; // per tick when awake
    private static final float WALK_MULT = 1.5F;
    private static final float SPRINT_MULT = 3.0F;
    private static final float MINE_MULT = 2.5F;
    private static final float IDLE_MULT = 0.5F;
    private static final float SIT_MULT = 0.3F; // sneaking
    private static final float JUMP_BURST = 0.05F; // per jump

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!Somnia.proxy.enableTirednessSystem) {
            TirednessData data = TirednessData.get(event.player);
            if (data != null) data.resetFatigueData(event.player);
            return;
        }
        if (event.phase != TickEvent.Phase.END) return;
        EntityPlayer player = event.player;
        if (player.worldObj.isRemote) return;

        // Fetch or register data
        TirednessData data = TirednessData.get(player);
        if (data == null) {
            TirednessData.register(player);
            data = TirednessData.get(player);
        }

        boolean isSleeping = player.isPlayerSleeping();
        boolean wasSleeping = data.wasSleeping;

        // Sleeping recovery
        if (isSleeping) {
            data.changeFatigue(-0.1F);
        }

        // Awake fatigue
        if (!isSleeping) {
            float multiplier = BASE_FATIGUE;

            // Activity-based multiplier
            if (player.isSprinting()) multiplier *= SPRINT_MULT;
            else if (player.isSwingInProgress) multiplier *= MINE_MULT;
            else if (player.motionX * player.motionX + player.motionZ * player.motionZ > 0.001) multiplier *= WALK_MULT;
            else if (player.isSneaking()) multiplier *= SIT_MULT;
            else multiplier *= IDLE_MULT;

            // Jump burst
            if (player.motionY > 0.3F) data.changeFatigue(JUMP_BURST);

            data.changeFatigue(multiplier);
        }

        // Apply tiredness effects
        float tired = data.getExhaustion();
        if (tired > 90 && !data.isDrowsy()) {
            data.setDrowsy(true);
            player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 200, 1));
            player.addPotionEffect(new PotionEffect(Potion.weakness.id, 200, 0));
        } else if (tired < 80 && data.isDrowsy()) {
            data.setDrowsy(false);
        }

        if (tired > 50) {
            player.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 100, tired > 75 ? 1 : 0, true));
        }

        // Save to NBT
        data.saveToPlayer(player);

        // Sync periodically
        if (player.ticksExisted % 20 == 0) {
            SomniaTirednessNetwork.syncToClient((EntityPlayerMP) player, data.getExhaustion());
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!com.kingrunes.somnia.Somnia.proxy.enableTirednessSystem) return;

        TirednessData data = TirednessData.get(event.player);
        if (data != null) data.resetFatigueData(event.player);
    }
}
