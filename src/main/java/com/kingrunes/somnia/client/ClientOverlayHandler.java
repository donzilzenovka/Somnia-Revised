package com.kingrunes.somnia.client;

import com.kingrunes.somnia.common.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import static com.kingrunes.somnia.Somnia.proxy;

/**
 * Renders a fullscreen black overlay with alpha based on sleeping progress.
 */
public class ClientOverlayHandler {

    private final ClientTickHandler tickHandler;

    public ClientOverlayHandler(ClientTickHandler tickHandler) {
        this.tickHandler = tickHandler;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;

        if (!tickHandler.isSleeping()) return;

        if (!proxy.fading) return;

        float alpha = tickHandler.getSleepFade();
        if (alpha <= 0.0F) return;

        int width = event.resolution.getScaledWidth();
        int height = event.resolution.getScaledHeight();

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(0.00F, 0.00F, 0.0F, alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, 0);
        GL11.glVertex2f(0, height);
        GL11.glVertex2f(width, height);
        GL11.glVertex2f(width, 0);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
    }


}
