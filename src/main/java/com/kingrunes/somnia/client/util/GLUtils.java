package com.kingrunes.somnia.client.util;

import org.lwjgl.opengl.GL11;

/**
 * Utility methods for managing OpenGL state.
 */
public class GLUtils {

    /**
     * Disables certain GL features with default blending enabled.
     */
    public static void glDisable() {
        glDisable(true);
    }

    /**
     * Disables texture, lighting, and depth testing.
     * Optionally configures blending.
     *
     * @param enableBlend whether to enable default blending function
     */
    public static void glDisable(boolean enableBlend) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        if (enableBlend) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
    }

    /**
     * Enables texture, depth test, and optionally lighting.
     * Disables line smoothing and blending.
     *
     * @param enableLighting whether to enable lighting
     */
    public static void glEnable(boolean enableLighting) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        if (enableLighting) {
            GL11.glEnable(GL11.GL_LIGHTING);
        }
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
