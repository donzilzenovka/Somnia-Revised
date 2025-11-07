package com.kingrunes.somnia.client;

import static com.kingrunes.somnia.Somnia.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;

import com.kingrunes.somnia.Somnia;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Handles client-side ticking for Somnia, including:
 * - Smooth fade overlay while sleeping
 * - Smooth audio fade when sleeping (respects manual volume changes)
 * - Auto wake-up logic
 */
public class ClientTickHandler {

    private float sleepFade = 0.0F; // 0 = awake, 1 = fully asleep (black)
    private float targetVolume = -1.0F; // volume to fade to while sleeping
    private float userMasterVolume = -1.0F; // current user-set master volume (awake baseline)
    private static final float FADE_SPEED = 0.01F; // speed of fade per tick

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null) return;

        boolean sleeping = mc.thePlayer.isPlayerSleeping();

        // Track user volume while awake and not fading
        if (!sleeping && sleepFade <= 0.0F) {
            userMasterVolume = mc.gameSettings.getSoundLevel(SoundCategory.MASTER);
        }

        // Determine target volume for sleeping
        if (proxy.muteSoundWhenSleeping && sleeping) {
            if (targetVolume < 0.0F) {
                // first tick of sleep, capture current user volume
                if (userMasterVolume < 0.0F) {
                    userMasterVolume = mc.gameSettings.getSoundLevel(SoundCategory.MASTER);
                }
                targetVolume = 0.0F; // fade to silence
            }
        } else if (!sleeping && targetVolume >= 0.0F) {
            // waking up: fade back to user volume
            targetVolume = userMasterVolume;
        }

        // Smooth sleep fade overlay
        float desiredFade = sleeping ? 1.0F : 0.0F;
        if (sleepFade < desiredFade) {
            sleepFade = Math.min(desiredFade, sleepFade + FADE_SPEED);
        } else if (sleepFade > desiredFade) {
            sleepFade = Math.max(desiredFade, sleepFade - FADE_SPEED);
        }

        // Smooth audio fade
        if (proxy.muteSoundWhenSleeping && targetVolume >= 0.0F) {
            float currentVolume = mc.gameSettings.getSoundLevel(SoundCategory.MASTER);
            float volumeDiff = targetVolume - currentVolume;
            float step = FADE_SPEED; // you can tweak for speed of audio fade
            if (Math.abs(volumeDiff) < step) {
                mc.gameSettings.setSoundLevel(SoundCategory.MASTER, targetVolume);
            } else {
                mc.gameSettings.setSoundLevel(SoundCategory.MASTER, currentVolume + (volumeDiff > 0 ? step : -step));
            }

            // Reset target volume when fade finished
            if (!sleeping
                && Math.abs(mc.gameSettings.getSoundLevel(SoundCategory.MASTER) - userMasterVolume) < 0.001F) {
                targetVolume = -1.0F;
            }
        }

        // Auto wake-up logic ** disabled no wake up
        if (Somnia.clientAutoWakeTime > -1L && sleeping
            && proxy.playersWakeNaturally
            && mc.theWorld.getTotalWorldTime() >= Somnia.clientAutoWakeTime) {

            Somnia.clientAutoWakeTime = -1L;
            mc.thePlayer.wakeUpPlayer(false, false, true);
        }
    }

    public float getSleepFade() {
        return sleepFade;
    }

    public boolean isSleeping() {
        return sleepFade > 0.0F;
    }
}
