package com.kingrunes.somnia.client;

import static com.kingrunes.somnia.Somnia.proxy;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.client.gui.GuiSelectWakeTime;
import com.kingrunes.somnia.client.gui.GuiSomnia;
import com.kingrunes.somnia.common.CommonProxy;
import com.kingrunes.somnia.common.StreamUtils;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public static ClientTickHandler clientTickHandler;

    // x/z offsets for bed head based on direction: 0=S, 1=W, 2=N, 3=E
    private static final int[][] BED_DIRECTION_OFFSETS = { { 0, 1 }, // South
        { -1, 0 }, // West
        { 0, -1 }, // North
        { 1, 0 } // East
    };

    @Override
    public void register() {
        super.register();
        // Register client-side tick handler
        ClientTickHandler tickHandler = new ClientTickHandler();
        MinecraftForge.EVENT_BUS.register(tickHandler);

        // Register overlay renderer
        MinecraftForge.EVENT_BUS.register(new ClientOverlayHandler(tickHandler));

        FMLCommonHandler.instance()
            .bus()
            .register(tickHandler);
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance()
            .bus()
            .register(new ClientTickHandler());

    }

    /**
     * Intercepts right-clicks on beds to open the wake time GUI or set automatic wake time.
     */
    @SubscribeEvent
    public void interactHook(PlayerInteractEvent event) {
        if (!event.world.isRemote) {
            return;
        }

        // Check for right-clicking a bed
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
            && event.entity.worldObj.getBlock(event.x, event.y, event.z)
                .getUnlocalizedName()
                .equals("tile.bed")) {

            int metadata = event.entity.worldObj.getBlockMetadata(event.x, event.y, event.z);
            int direction = metadata & 0x3;

            int bedX = event.x;
            int bedZ = event.z;

            // If this is not the head of the bed, adjust coordinates
            if ((metadata & 0x8) == 0) {
                bedX += BED_DIRECTION_OFFSETS[direction][0];
                bedZ += BED_DIRECTION_OFFSETS[direction][1];
            }

            // Only trigger if player is near the bed
            if (Math.abs(event.entityPlayer.posX - bedX) < 3.0D && Math.abs(event.entityPlayer.posY - event.y) < 2.0D
                && Math.abs(event.entityPlayer.posZ - bedZ) < 3.0D) {

                ItemStack currentItem = event.entityPlayer.inventory.getCurrentItem();

                // If holding a clock, open the wake time GUI
                if (currentItem != null && Objects.requireNonNull(currentItem.getItem())
                    .getUnlocalizedName()
                    .equals("item.clock")) {
                    event.setCanceled(true);
                    Minecraft.getMinecraft()
                        .displayGuiScreen(new GuiSelectWakeTime());
                } else if (proxy.playersWakeNaturally) {
                    // Otherwise, set a default wake time based on time of day if enabled
                    long totalWorldTime = event.world.getTotalWorldTime();
                    Somnia.clientAutoWakeTime = Somnia
                        .calculateWakeTime(totalWorldTime, (totalWorldTime % 24000L > 12000L) ? 0 : 12000);
                }
            }
        } else if (Minecraft.getMinecraft().currentScreen instanceof GuiSelectWakeTime) {
            // Prevent interaction while GUI is open
            event.setCanceled(true);
        }
    }

    /**
     * Opens the Somnia GUI if flagged.
     */
    public void handleGUIOpenPacket() throws IOException {
        if (this.somniaGui) {
            Minecraft.getMinecraft()
                .displayGuiScreen(new GuiSomnia());
        }
    }

    /**
     * Updates the Somnia GUI fields based on network input.
     */
    public void handleGUIUpdatePacket(DataInputStream in) throws IOException {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (currentScreen instanceof GuiSomnia) {
            GuiSomnia gui = (GuiSomnia) currentScreen;

            int fieldCount = in.readInt();
            for (int i = 0; i < fieldCount; i++) {
                String fieldName = StreamUtils.readString(in);
                gui.updateField(fieldName, in);
            }
        }
    }

    /**
     * Closes the Somnia GUI.
     */
    public void handleGUIClosePacket(EntityPlayerMP player) {
        Minecraft.getMinecraft()
            .displayGuiScreen(null);
    }
}
