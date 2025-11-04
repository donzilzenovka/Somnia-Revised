package com.kingrunes.somnia.common;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;

import com.kingrunes.somnia.Somnia;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

public class PacketHandler {

    private static final HashMap<Byte, FMLProxyPacket> cache = new HashMap<>();

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        if ("Somnia".equals(event.packet.channel())) {
            onPacket(event.packet, ((NetHandlerPlayServer) event.handler).playerEntity);
        }
    }

    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        if ("Somnia".equals(event.packet.channel())) {
            onPacket(event.packet, null);
        }
    }

    public void onPacket(FMLProxyPacket packet, EntityPlayerMP player) {
        try (DataInputStream in = new DataInputStream(new ByteBufInputStream(packet.payload()))) {
            byte id = in.readByte();
            switch (id) {
                case 0 -> handleGUIOpenPacket();
                case 1 -> handleGUIClosePacket(player, in);
                case 2 -> handleGUIUpdate(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGUIOpenPacket() throws IOException {
        Somnia.proxy.handleGUIOpenPacket();
    }

    private void handleGUIUpdate(DataInputStream in) throws IOException {
        Somnia.proxy.handleGUIUpdatePacket(in);
    }

    private void handleGUIClosePacket(EntityPlayerMP player, DataInputStream in) throws IOException {
        Somnia.proxy.handleGUIClosePacket(player);
    }

    public static FMLProxyPacket buildGUIOpenPacket() {
        return cache.computeIfAbsent(byteOf(0), k -> buildPacket((byte) 0));
    }

    public static FMLProxyPacket buildGUIClosePacket() {
        return cache.computeIfAbsent(byteOf(1), k -> buildPacket((byte) 1));
    }

    public static FMLProxyPacket buildGUIUpdatePacket(Object... fields) {
        try (ByteBufOutputStream bbos = new ByteBufOutputStream(Unpooled.buffer())) {
            bbos.writeByte(2);
            bbos.writeInt(fields.length / 2);
            for (int i = 0; i < fields.length; i++) {
                StreamUtils.writeString(fields[i].toString(), bbos);
                StreamUtils.writeObject(fields[++i], bbos);
            }
            return new FMLProxyPacket(bbos.buffer(), "Somnia");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static FMLProxyPacket buildPacket(byte id) {
        try (ByteBufOutputStream bbos = new ByteBufOutputStream(Unpooled.buffer())) {
            bbos.writeByte(id);
            return new FMLProxyPacket(bbos.buffer(), "Somnia");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static Byte byteOf(int i) {
        return (byte) i;
    }

    private static void close(OutputStream os) {
        try {
            os.close();
        } catch (IOException ignored) {}
    }
}
