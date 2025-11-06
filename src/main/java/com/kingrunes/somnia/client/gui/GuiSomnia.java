package com.kingrunes.somnia.client.gui;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.client.gui.GuiSleepMP;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.ChatComponentTranslation;

import org.lwjgl.opengl.GL11;

import com.kingrunes.somnia.common.StreamUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSomnia extends GuiSleepMP {

    public static final String TRANSLATION_FORMAT = "somnia.status.%s";
    public static final String SPEED_FORMAT = "%sx%s";

    public static final String COLOR = "§";
    public static final String BLACK = COLOR + "0";
    public static final String WHITE = COLOR + "f";
    public static final String RED = COLOR + "c";
    public static final String DARK_RED = COLOR + "4";
    public static final String GOLD = COLOR + "6";

    public static final byte[] BYTES_WHITE = { -1, -1, -1 };
    public static final byte[] BYTES_DARK_RED = { -85, 0, 0 };
    public static final byte[] BYTES_RED = { -1, 0, 0 };
    public static final byte[] BYTES_GOLD = { -16, -56, 30 };

    private static final RenderItem presetIconRenderer = new RenderItem();
    private static final ItemStack clockItemStack = new ItemStack(Item.getItemById(347));

    public String status = "Sleeping...";
    public double speed = 0.0D;
    public int sleeping = 0, awake = 0;

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.mc.thePlayer.sendQueue.addToSendQueue((Packet) new C0BPacketEntityAction((Entity) this.mc.thePlayer, 3));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Render the clock icon
        GL11.glPushMatrix();
        GL11.glScalef(4.0F, 4.0F, 1.0F);
        presetIconRenderer.renderItemIntoGUI(
            this.fontRendererObj,
            this.mc.getTextureManager(),
            clockItemStack,
            (this.width - this.width / 5) / 4,
            (this.height / 2 - 36) / 4);
        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glPushMatrix();
        GL11.glScalef(2.0F, 2.0F, 1.0F);

        // Draw speed
        drawString(
            this.fontRendererObj,
            String.format(SPEED_FORMAT, getColorStringForSpeed(this.speed), this.speed),
            5,
            5,
            0x80000000);
        GL11.glPopMatrix();

        // Draw status
        boolean centered = (this.status.length() != 5);
        drawString(
            this.fontRendererObj,
            WHITE + this.status,
            centered ? this.width / 2 - this.fontRendererObj.getStringWidth(this.status) / 2
                : this.width - this.width / 5 + 18,
            centered ? this.height / 8 : this.height / 2,
            0x80000000);
    }

    public void updateField(String field, DataInputStream in) throws IOException {
        if ("status".equalsIgnoreCase(field)) {
            String str = StreamUtils.readString(in);
            this.status = str
                .startsWith("f:")
                    ? new ChatComponentTranslation(
                        String.format(
                            TRANSLATION_FORMAT,
                            str.substring(2)
                                .toLowerCase())).getUnformattedTextForChat()
                    : str;
        } else if ("speed".equalsIgnoreCase(field)) {
            this.speed = in.readDouble();
        }
    }

    public static byte[] getColorForSpeed(double speed) {
        if (speed < 8.0D) return BYTES_WHITE;
        if (speed < 20.0D) return BYTES_DARK_RED;
        if (speed < 30.0D) return BYTES_RED;
        return BYTES_GOLD;
    }

    public static String getColorStringForSpeed(double speed) {
        if (speed < 8.0D) return WHITE;
        if (speed < 20.0D) return DARK_RED;
        if (speed < 30.0D) return RED;
        return GOLD;
    }
}
