package com.kingrunes.somnia.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ClientTirednessEffects {

    private static final Minecraft mc = Minecraft.getMinecraft();
    public static float clientTiredness = 0.0F; // 0 to 100

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (mc.thePlayer == null) return;

        // Base FOV
        float baseFov = mc.gameSettings.fovSetting;

        if (clientTiredness > 50.0F) {
            // Narrowing FOV as player gets more tired
            float modifier = 1.0F - (clientTiredness - 50F) / 400F;
            mc.gameSettings.fovSetting = baseFov * modifier;
        } else {
            mc.gameSettings.fovSetting = baseFov;
        }
    }

    @SubscribeEvent
    public void onRenderDebug(RenderGameOverlayEvent.Text event) {
        if (mc.thePlayer == null) return;

        // Only show if in F3 debug mode
        if (mc.gameSettings.showDebugInfo) {
            if (com.kingrunes.somnia.Somnia.proxy.enableTirednessSystem) {
                event.left.add(String.format("Tiredness: %.1f%%", clientTiredness));
            }

        }
    }
}
