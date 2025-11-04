package com.kingrunes.somnia.common;

import java.io.DataInputStream;
import java.io.IOException;

import com.google.common.base.Charsets;

import io.netty.buffer.ByteBufOutputStream;

public class StreamUtils {

    public static String readString(DataInputStream in) throws IOException {
        int length = in.readInt();

        if (length < 0) {
            throw new IOException("The received encoded string buffer length is less than zero! Weird string!");
        }

        byte[] buffer = new byte[length];
        in.readFully(buffer);
        return new String(buffer, Charsets.UTF_8);
    }

    public static void writeString(String str, ByteBufOutputStream bbos) throws IOException {
        byte[] buffer = str.getBytes(Charsets.UTF_8);
        bbos.writeInt(buffer.length);
        bbos.write(buffer);
    }

    public static void writeObject(Object object, ByteBufOutputStream bbos) throws IOException {
        if (object instanceof String) {
            writeString((String) object, bbos);
        } else if (object instanceof Integer) {
            bbos.writeInt((Integer) object);
        } else if (object instanceof Long) {
            bbos.writeLong((Long) object);
        } else if (object instanceof Double) {
            bbos.writeDouble((Double) object);
        } else {
            throw new IllegalArgumentException(
                "Unknown data type: " + object.getClass()
                    .getCanonicalName());
        }
    }
}
