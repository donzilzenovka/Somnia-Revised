package com.kingrunes.somnia.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;

import com.kingrunes.somnia.Somnia;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ClientRenderHandler {

    private float fadeAlpha = 0.0F;
    private final float fadeSpeed = 0.02F; // Adjust for faster/slower fade

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();

        // Only fade if we are sleeping and fading is enabled
        if (!mc.thePlayer.isPlayerSleeping() || !Somnia.proxy.fading) {
            fadeAlpha = 0.0F; // Reset fade when not sleeping
            return;
        }

        // Increment fade
        if (fadeAlpha < 1.0F) {
            fadeAlpha += fadeSpeed;
            if (fadeAlpha > 1.0F) fadeAlpha = 1.0F;
        }

        // Draw full-screen black overlay
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int width = sr.getScaledWidth();
        int height = sr.getScaledHeight();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(0F, 0F, 0F, fadeAlpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2d(0, 0);
        GL11.glVertex2d(0, height);
        GL11.glVertex2d(width, height);
        GL11.glVertex2d(width, 0);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
