package com.kingrunes.somnia.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldServer;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.util.SomniaState;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;

public class ServerTickHandler {

    public static final String TRANSLATION_FORMAT = "somnia.status.%s";
    private static int activeTickHandlers = 0;

    public WorldServer worldServer;
    public SomniaState currentState;
    public long lastSleepStart;
    public long currentSleepPeriod;
    public long checkTimer = 0L;
    public long lastTpsMillis = 0L;
    public long liTps = 0L;
    public long tps = 0L;

    private double multiplier = Somnia.proxy.baseMultiplier;
    private double overflow;

    public ServerTickHandler(WorldServer worldServer) {
        this.worldServer = worldServer;
        this.overflow = 0.0D;
    }

    public void tickStart() {
        incrementCounters();

        if (++this.checkTimer == 10L) {
            this.checkTimer = 0L;
            SomniaState prevState = this.currentState;
            this.currentState = SomniaState.getState(this);

            if (prevState != this.currentState) {
                this.currentSleepPeriod = 0L;
                if (this.currentState == SomniaState.ACTIVE) {
                    this.lastSleepStart = this.worldServer.getTotalWorldTime();
                    activeTickHandlers++;
                } else if (prevState == SomniaState.ACTIVE) {
                    activeTickHandlers--;
                    if (this.currentState == SomniaState.EXPIRED || this.currentState == SomniaState.NOT_NOW) {
                        closeGuiWithMessage(this.currentState.toString());
                    }
                }
            }

            if (this.currentState == SomniaState.ACTIVE || this.currentState == SomniaState.WAITING_PLAYERS
                || this.currentState == SomniaState.COOLDOWN) {

                FMLProxyPacket packet = PacketHandler.buildGUIUpdatePacket(
                    new Object[] { "status",
                        (this.currentState == SomniaState.ACTIVE)
                            ? Somnia.timeStringForWorldTime(this.worldServer.getWorldTime())
                            : ("f:" + this.currentState.toString()),
                        "speed", (this.currentState == SomniaState.ACTIVE) ? (this.tps / 20.0D) : 0.0D });

                Somnia.channel.sendToDimension(packet, this.worldServer.provider.dimensionId);
            }
        }

        if (this.currentState == SomniaState.ACTIVE) {
            doMultipliedTicking();
        }
    }

    private void closeGuiWithMessage(String key) {
        FMLProxyPacket packet = PacketHandler.buildGUIClosePacket();
        ChatComponentTranslation chatComponentTranslation = new ChatComponentTranslation(
            String.format("somnia.status.%s", key));

        for (EntityPlayer ep : this.worldServer.playerEntities) {
            if (ep.isPlayerSleeping()) {
                Somnia.channel.sendTo(packet, (EntityPlayerMP) ep);
                if (ep.isPlayerSleeping()) ep.wakeUpPlayer(false, true, true);
                ep.addChatMessage((IChatComponent) chatComponentTranslation);
            }
        }
    }

    private void incrementCounters() {
        this.liTps++;
        if (this.currentState == SomniaState.ACTIVE) this.currentSleepPeriod++;
    }

    private void doMultipliedTicking() {
        int liMultiplier = (int) Math.floor(this.multiplier);
        double target = liMultiplier + this.overflow;
        int liTarget = (int) Math.floor(target);
        this.overflow = target - liTarget;

        long nanoTime = System.nanoTime();
        for (int i = 0; i < liTarget; i++) {
            doMultipliedServerTicking();
        }

        if (nanoTime > 50.0D / activeTickHandlers) {
            this.multiplier += 0.1D;
        } else {
            this.multiplier -= 0.1D;
        }

        if (this.multiplier > Somnia.proxy.multiplierCap) this.multiplier = Somnia.proxy.multiplierCap;
        if (this.multiplier < Somnia.proxy.baseMultiplier) this.multiplier = Somnia.proxy.baseMultiplier;

        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - this.lastTpsMillis > 1000L) {
            this.tps = this.liTps;
            this.liTps = 0L;
            this.lastTpsMillis = currentTimeMillis;
        }
    }

    private void doMultipliedServerTicking() {
        this.worldServer.tick();
        this.worldServer.updateEntities();
        this.worldServer.getEntityTracker()
            .updateTrackedEntities();
        MinecraftServer.getServer()
            .getConfigurationManager()
            .sendPacketToAllPlayersInDimension(
                (Packet) new S03PacketTimeUpdate(
                    this.worldServer.getTotalWorldTime(),
                    this.worldServer.getWorldTime(),
                    this.worldServer.getGameRules()
                        .getGameRuleBooleanValue("doDaylightCycle")),
                this.worldServer.provider.dimensionId);
        incrementCounters();
    }
}
