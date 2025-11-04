package com.kingrunes.somnia.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import com.kingrunes.somnia.Somnia;

/**
 * GUI for selecting a wake-up time in Somnia.
 * Displays several buttons corresponding to preset times of day.
 */
public class GuiSelectWakeTime extends GuiScreen {

    @Override
    public void initGui() {
        int i = 0;
        int buttonWidth = 90;
        int buttonHeight = 20;

        // Top row
        this.buttonList.add(
            new GuiButton(
                i++,
                this.width / 2 - buttonWidth / 2,
                this.height / 4 - buttonHeight / 2,
                buttonWidth,
                buttonHeight,
                "Noon"));
        this.buttonList.add(
            new GuiButton(
                i++,
                this.width * 5 / 8 - buttonWidth / 2,
                this.height * 3 / 8 - buttonHeight / 2,
                buttonWidth,
                buttonHeight,
                "Mid Afternoon"));
        this.buttonList.add(
            new GuiButton(
                i++,
                this.width * 3 / 4 - buttonWidth / 2,
                this.height / 2 - buttonHeight / 2,
                buttonWidth,
                buttonHeight,
                "Before Sunset"));
        this.buttonList.add(
            new GuiButton(
                i++,
                this.width * 5 / 8 - buttonWidth / 2,
                this.height * 5 / 8 - buttonHeight / 2,
                buttonWidth,
                buttonHeight,
                "After Sunset"));
        this.buttonList.add(
            new GuiButton(
                i++,
                this.width / 2 - buttonWidth / 2,
                this.height * 3 / 4 - buttonHeight / 2,
                buttonWidth,
                buttonHeight,
                "Midnight"));
        this.buttonList.add(
            new GuiButton(
                i++,
                this.width * 3 / 8 - buttonWidth / 2,
                this.height * 5 / 8 - buttonHeight / 2,
                buttonWidth,
                buttonHeight,
                "Before Sunrise"));
        this.buttonList.add(
            new GuiButton(
                i++,
                this.width / 4 - buttonWidth / 2,
                this.height / 2 - buttonHeight / 2,
                buttonWidth,
                buttonHeight,
                "After Sunrise"));
        this.buttonList.add(
            new GuiButton(
                i++,
                this.width * 3 / 8 - buttonWidth / 2,
                this.height * 3 / 8 - buttonHeight / 2,
                buttonWidth,
                buttonHeight,
                "Mid Morning"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        int targetTime;

        switch (button.id) {
            case 0:
                targetTime = 6000;
                break; // Noon
            case 1:
                targetTime = 9000;
                break; // Mid Afternoon
            case 2:
                targetTime = 12000;
                break; // Before Sunset
            case 3:
                targetTime = 14000;
                break; // After Sunset
            case 4:
                targetTime = 18000;
                break; // Midnight
            case 5:
                targetTime = 22000;
                break; // Before Sunrise
            case 6:
                targetTime = 0;
                break; // After Sunrise
            case 7:
                targetTime = 3000;
                break; // Mid Morning
            default:
                return;
        }

        // Calculate the player's wake time based on current world time and selected target
        Somnia.clientAutoWakeTime = Somnia.calculateWakeTime(this.mc.theWorld.getTotalWorldTime(), targetTime);

        // Close the GUI
        this.mc.displayGuiScreen(null);

        // Simulate a right-click on the bed to trigger normal sleep logic
        MovingObjectPosition mop = this.mc.objectMouseOver;
        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            this.mc.playerController.onPlayerRightClick(
                (EntityPlayer) this.mc.thePlayer,
                (World) this.mc.theWorld,
                this.mc.thePlayer.inventory.getCurrentItem(),
                mop.blockX,
                mop.blockY,
                mop.blockZ,
                mop.sideHit,
                mop.hitVec);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
