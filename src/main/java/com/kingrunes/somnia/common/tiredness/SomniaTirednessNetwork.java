package com.kingrunes.somnia.common.tiredness;

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class SomniaTirednessNetwork {

    public static SimpleNetworkWrapper NETWORK;

    public static void init() {
        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("SomniaTiredness");
        NETWORK.registerMessage(PacketTirednessSync.Handler.class, PacketTirednessSync.class, 0, Side.CLIENT);
    }

    public static void syncToClient(EntityPlayerMP player, float tiredness) {
        NETWORK.sendTo(new PacketTirednessSync(tiredness), player);
    }
}
