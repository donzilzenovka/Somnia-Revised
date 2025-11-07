package com.kingrunes.somnia;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import org.lwjgl.opengl.GL11;

import com.kingrunes.somnia.common.CommonProxy;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.util.SomniaState;
import com.kingrunes.somnia.server.ServerTickHandler;
import com.kingrunes.somnia.server.SomniaCommand;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Main mod class for Somnia
 */
@Mod(modid = "Somnia", name = "Somnia", version = "-au")
public class Somnia {

    public static final String VERSION = SomniaVersion.getVersionString();

    public static long clientAutoWakeTime = -1L;

    // Use thread-safe lists to avoid concurrent modification during ticks
    public List<ServerTickHandler> tickHandlers = new CopyOnWriteArrayList<ServerTickHandler>();
    public List<WeakReference<EntityPlayerMP>> ignoreList = new CopyOnWriteArrayList<WeakReference<EntityPlayerMP>>();

    public static final String MOD_ID = "Somnia";
    public static final String NAME = "Somnia";
    public Configuration config;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        (event.getModMetadata()).version = VERSION;
        proxy.configure(event.getSuggestedConfigurationFile());
        // Initialize tiredness network
        com.kingrunes.somnia.common.tiredness.SomniaTirednessNetwork.init();
    }

    @Instance
    public static Somnia instance;

    @SidedProxy(
        serverSide = "com.kingrunes.somnia.common.CommonProxy",
        clientSide = "com.kingrunes.somnia.client.ClientProxy")
    public static CommonProxy proxy;

    public static FMLEventChannel channel;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("Somnia");
        channel.register(new PacketHandler());

        proxy.register();

        MinecraftForge.EVENT_BUS.register(new com.kingrunes.somnia.common.tiredness.SomniaTirednessHandler());
        FMLCommonHandler.instance()
            .bus()
            .register(new com.kingrunes.somnia.common.tiredness.SomniaTirednessHandler());
        MinecraftForge.EVENT_BUS.register(new com.kingrunes.somnia.common.SomniaPlayerHandler());
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand((ICommand) new SomniaCommand());
    }

    /**
     * Called each server tick (from wherever you hook it)
     */
    public void tick() {
        // synchronize on the local list reference or rely on CopyOnWriteArrayList
        for (ServerTickHandler serverTickHandler : tickHandlers) {
            serverTickHandler.tickStart();
        }
    }

    public static String timeStringForWorldTime(long time) {
        time += 6000L;
        time %= 24000L;
        int hours = (int) Math.floor(time / 1000.0D);
        int minutes = (int) ((time % 1000L) / 1000.0D * 60.0D);

        String lsHours = String.valueOf(hours);
        String lsMinutes = String.valueOf(minutes);

        if (lsHours.length() == 1) lsHours = "0" + lsHours;
        if (lsMinutes.length() == 1) {
            lsMinutes = "0" + lsMinutes;
        }
        return lsHours + ":" + lsMinutes;
    }

    /**
     * Returns true if player has any armor piece equipped.
     * Note: original method name was doesPlayHaveAnyArmor (typo). Kept name for compatibility.
     */
    public static boolean doesPlayHaveAnyArmor(EntityPlayer e) {
        ItemStack[] armor = e.inventory.armorInventory;
        for (int a = 0; a < armor.length; a++) {
            if (armor[a] != null) return true;
        }
        return false;
    }

    public static long calculateWakeTime(long totalWorldTime, int i) {
        long timeInDay = totalWorldTime % 24000L;
        long l = totalWorldTime - timeInDay + i;
        if (timeInDay > i) l += 24000L;
        return l;
    }

    @SideOnly(Side.CLIENT)
    public static void renderWorld(float par1, long par2) {
        if ((Minecraft.getMinecraft()).thePlayer.isPlayerSleeping() && proxy.disableRendering) {
            GL11.glClear(16640);
            return;
        }
        (Minecraft.getMinecraft()).entityRenderer.renderWorld(par1, par2);
    }

    /**
     * Determines whether mobs should spawn in the provided WorldServer.
     *
     * NOTE: preserve original semantics: if creature spawning hasn't been disabled by the proxy
     * (proxy.disableCreatureSpawning == false) OR the world's "doMobSpawning" game rule is false,
     * this method returns false (i.e., do not prevent spawning). Otherwise, it tries to find the
     * ServerTickHandler for the world and returns true/false depending on its state.
     */
    public static boolean doMobSpawning(WorldServer par1WorldServer) {
        boolean gameRule = par1WorldServer.getGameRules()
            .getGameRuleBooleanValue("doMobSpawning");

        // If plugin doesn't want to disable creature spawning, or the world has doMobSpawning disabled,
        // do not interfere (return false = do not prevent spawning).
        if (!proxy.disableCreatureSpawning || !gameRule) {
            return false;
        }

        for (ServerTickHandler serverTickHandler : instance.tickHandlers) {
            if (serverTickHandler.worldServer == par1WorldServer) {
                // If the Somnia state is ACTIVE, we want to prevent spawning (return false).
                // Original code returned (serverTickHandler.currentState != SomniaState.ACTIVE)
                // We'll preserve that semantics:
                return (serverTickHandler.currentState != SomniaState.ACTIVE);
            }
        }

        // No tick handler matched this world — log and allow spawning by default.
        System.err.println("[Somnia] Warning: no ServerTickHandler found for world " + par1WorldServer);
        // Allow spawning by default to avoid breaking servers.
        return true;
    }
}
