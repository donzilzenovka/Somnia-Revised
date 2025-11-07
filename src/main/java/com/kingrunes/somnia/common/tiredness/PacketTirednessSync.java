package com.kingrunes.somnia.common.tiredness;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketTirednessSync implements IMessage {

    public float tiredness;

    public PacketTirednessSync() {}

    public PacketTirednessSync(float tiredness) {
        this.tiredness = tiredness;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        tiredness = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(tiredness);
    }

    public static class Handler implements IMessageHandler<PacketTirednessSync, IMessage> {

        @Override
        public IMessage onMessage(PacketTirednessSync message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                // Client updates tiredness
                com.kingrunes.somnia.client.ClientTirednessEffects.clientTiredness = message.tiredness;
            }
            return null;
        }
    }
}
