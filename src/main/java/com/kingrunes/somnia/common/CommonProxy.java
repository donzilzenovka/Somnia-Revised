package com.kingrunes.somnia.common;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.WorldEvent;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.util.ClassUtils;
import com.kingrunes.somnia.common.util.TimePeriod;
import com.kingrunes.somnia.server.ServerTickHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class CommonProxy {

    // ----------------------------------------
    // CONFIGURATION SETTINGS
    // ----------------------------------------
    private static final int CONFIG_VERSION = 1;

    public boolean playersWakeNaturally;
    public TimePeriod enterSleepPeriod;
    public TimePeriod validSleepPeriod;
    public long maxSleepTimePeriod;
    public long sleepCooldown;

    public double baseMultiplier;
    public double multiplierCap;

    public boolean sleepWithArmor;
    public boolean vanillaBugFixes;
    public boolean fading;
    public boolean somniaGui;
    public boolean muteSoundWhenSleeping;
    public boolean ignoreMonsters;

    public boolean disableCreatureSpawning;
    public boolean disableRendering;
    public boolean disableMoodSoundAndLightCheck;

    // ----------------------------------------
    // CONFIGURATION LOADING
    // ----------------------------------------
    public void configure(File file) {
        Configuration config = new Configuration(file);
        config.load();

        Property versionProp = config.get("general", "configVersion", 0);
        if (versionProp.getInt() != CONFIG_VERSION) {
            file.delete(); // Reset if version mismatch
        }

        config = new Configuration(file);
        config.load();

        config.get("general", "configVersion", CONFIG_VERSION);

        // Timing settings
        this.playersWakeNaturally = config.get("timings", "playersWakeNaturally", true)
                .getBoolean(true);
        this.enterSleepPeriod = new TimePeriod(
            config.get("timings", "enterSleepStart", 0)
                .getInt(),
            config.get("timings", "enterSleepEnd", 24000)
                .getInt());
        this.validSleepPeriod = new TimePeriod(
            config.get("timings", "validSleepStart", 0)
                .getInt(),
            config.get("timings", "validSleepEnd", 24000)
                .getInt());
        this.maxSleepTimePeriod = config.get("timings", "maxSleepTime", 24000)
            .getInt();
        this.sleepCooldown = config.get("timings", "sleepCooldown", 12000)
            .getInt();

        // Logic settings
        this.baseMultiplier = config.get("logic", "baseMultiplier", 1.0D)
            .getDouble(1.0D);
        this.multiplierCap = config.get("logic", "multiplierCap", 100.0D)
            .getDouble(100.0D);

        // Option flags
        this.sleepWithArmor = config.get("options", "sleepWithArmor", false)
            .getBoolean(false);
        this.vanillaBugFixes = config.get("options", "vanillaBugFixes", true)
            .getBoolean(true);
        this.fading = config.get("options", "fading", true)
            .getBoolean(true);
        this.somniaGui = config.get("options", "somniaGui", true)
            .getBoolean(true);
        this.muteSoundWhenSleeping = config.get("options", "muteSoundWhenSleeping", true)
            .getBoolean(false);
        this.ignoreMonsters = config.get("options", "ignoreMonsters", true)
            .getBoolean(false);

        // Performance flags
        this.disableCreatureSpawning = config.get("performance", "disableCreatureSpawning", false)
            .getBoolean(false);
        this.disableRendering = config.get("performance", "disableRendering", true)
            .getBoolean(false);
        this.disableMoodSoundAndLightCheck = config.get("performance", "disableMoodSoundAndLightCheck", false)
            .getBoolean(false);

        config.save();
    }

    // ----------------------------------------
    // EVENT REGISTRATION
    // ----------------------------------------
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance()
            .bus()
            .register(new PlayerTickHandler());
    }

    // ----------------------------------------
    // WORLD EVENT HOOKS
    // ----------------------------------------
    @SubscribeEvent
    public void worldLoadHook(WorldEvent.Load event) {
        if (event.world instanceof WorldServer worldServer) {
            Somnia.instance.tickHandlers.add(new ServerTickHandler(worldServer));
            System.out.println("[Somnia] Registering tick handler for loading world!");
        }
    }

    @SubscribeEvent
    public void worldUnloadHook(WorldEvent.Unload event) {
        if (event.world instanceof WorldServer) {
            WorldServer worldServer = (WorldServer) event.world;

            boolean removed = Somnia.instance.tickHandlers.removeIf(handler -> {
                if (handler.worldServer == worldServer) {
                    System.out.println("[Somnia] Removing tick handler for unloading world!");
                    return true; // remove this handler
                }
                return false; // keep
            });

            if (!removed) {
                System.out.println("[Somnia] No tick handler found to remove for this world.");
            }
        }
    }

    // ----------------------------------------
    // PLAYER EVENT HOOKS
    // ----------------------------------------
    @SubscribeEvent
    public void onPlayerDamage(LivingHurtEvent event) {
        if (event.entityLiving instanceof EntityPlayerMP player) {
            if (player.isPlayerSleeping()) {
                Somnia.channel.sendTo(PacketHandler.buildGUIClosePacket(), player);
            }
        }
    }

    @SubscribeEvent
    public void sleepHook(PlayerSleepInBedEvent event) {
        onSleep(event);
    }

    public void onSleep(PlayerSleepInBedEvent event) {
        EntityPlayer player = event.entityPlayer;

        // Reject invalid sleep attempts
        if (event.result != null && event.result != EntityPlayer.EnumStatus.OK) return;
        if (!player.worldObj.isRemote) {
            if (!this.enterSleepPeriod.isTimeWithin(player.worldObj.getWorldTime() % 24000L)) {
                event.result = EntityPlayer.EnumStatus.NOT_POSSIBLE_NOW;
                return;
            }
            if (!this.sleepWithArmor && Somnia.doesPlayHaveAnyArmor(player)) {
                event.result = EntityPlayer.EnumStatus.OTHER_PROBLEM;
                player.addChatMessage(new ChatComponentText("That armor looks uncomfortable!"));
                return;
            }
            if (player.isPlayerSleeping() || !player.isEntityAlive()) {
                event.result = EntityPlayer.EnumStatus.OTHER_PROBLEM;
                return;
            }
            if (!player.worldObj.provider.isSurfaceWorld()) {
                event.result = EntityPlayer.EnumStatus.NOT_POSSIBLE_HERE;
                return;
            }
            if (Math.abs(player.posX - event.x) > 3.0D || Math.abs(player.posY - event.y) > 2.0D
                || Math.abs(player.posZ - event.z) > 3.0D) {
                event.result = EntityPlayer.EnumStatus.TOO_FAR_AWAY;
                return;
            }
            if (!this.ignoreMonsters) {
                double dx = 8.0D, dy = 5.0D;
                List<?> mobs = player.worldObj.getEntitiesWithinAABB(
                    EntityMob.class,
                    AxisAlignedBB.getBoundingBox(
                        event.x - dx,
                        event.y - dy,
                        event.z - dx,
                        event.x + dx,
                        event.y + dy,
                        event.z + dx));
                if (!mobs.isEmpty()) {
                    event.result = EntityPlayer.EnumStatus.NOT_SAFE;
                    return;
                }
            }
        }

        // Unmount if riding
        if (player.isRiding()) player.mountEntity(null);

        // Shrink bounding box and reset Y offset
        ClassUtils.setSize(player, 0.2F, 0.2F);
        player.yOffset = -0.5F;

        // Default bed block center
        double bedX = event.x + 0.5;
        double bedY = event.y + 0.3; // slightly above bed to avoid clipping
        double bedZ = event.z + 0.5;

        if (player.worldObj.blockExists(event.x, event.y, event.z)) {
            int direction = player.worldObj.getBlock(event.x, event.y, event.z)
                .getBedDirection((IBlockAccess) player.worldObj, event.x, event.y, event.z);

            // Apply small offset along bed's long axis so head/foot aligns naturally
            switch (direction) {
                case 0 -> bedZ += 0.2; // South
                case 1 -> bedX -= 0.2; // West
                case 2 -> bedZ -= 0.2; // North
                case 3 -> bedX += 0.2; // East
            }

            // Align yaw with bed
            switch (direction) {
                case 0 -> player.rotationYaw = 0F;
                case 1 -> player.rotationYaw = 90F;
                case 2 -> player.rotationYaw = 180F;
                case 3 -> player.rotationYaw = -90F;
            }

            ClassUtils.call_func_71013_b(player, direction);
        }

        // Apply final position
        player.setPosition(bedX, bedY, bedZ);

        // Set sleeping state and zero motion
        ClassUtils.setSleeping(player, true);
        ClassUtils.setSleepTimer(player, 0);
        player.motionX = player.motionY = player.motionZ = 0.0D;
        player.playerLocation = new ChunkCoordinates(event.x, event.y, event.z);

        if (!player.worldObj.isRemote) player.worldObj.updateAllPlayersSleepingFlag();

        event.result = EntityPlayer.EnumStatus.OK;

        if (!player.worldObj.isRemote) {
            Somnia.channel.sendTo(PacketHandler.buildGUIOpenPacket(), (EntityPlayerMP) player);
        }
    }

    // ----------------------------------------
    // NETWORK PACKET STUBS
    // ----------------------------------------
    public void handleGUIOpenPacket() throws IOException {}

    public void handleGUIUpdatePacket(DataInputStream in) throws IOException {}

    public void handleGUIClosePacket(EntityPlayerMP player) {}
}
