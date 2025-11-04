package com.kingrunes.somnia.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.launchwrapper.Launch;

/**
 * Utility class for handling reflection on Minecraft classes,
 * including deobfuscation handling.
 */
public class ClassUtils {

    private static Boolean mcp = null;

    /**
     * Checks whether the environment is deobfuscated (MCP) or obfuscated (production).
     *
     * @return true if running in a deobfuscated environment.
     */
    public static boolean deobfuscatedEnvironment() {
        if (mcp == null) {
            mcp = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
            System.out.println("[Somnia] Running in a" + (mcp ? " deobfuscated" : "n obfuscated") + " environment!");
        }
        return mcp;
    }

    /**
     * Sets the sleep timer for a player via reflection.
     *
     * @param player The player instance (EntityPlayer)
     * @param time   Sleep timer value
     */
    public static void setSleepTimer(Object player, int time) {
        try {
            String fieldName = deobfuscatedEnvironment() ? "sleepTimer" : "field_71076_b";
            Field field = EntityPlayer.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(player, time);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Sets whether the player is currently sleeping.
     *
     * @param player The player instance (EntityPlayer)
     * @param state  true if sleeping, false otherwise
     */
    public static void setSleeping(Object player, boolean state) {
        try {
            String fieldName = deobfuscatedEnvironment() ? "sleeping" : "field_71083_bS";
            Field field = EntityPlayer.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(player, state);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Sets the size of an entity.
     *
     * @param entity The entity instance
     * @param width  Width of the entity
     * @param height Height of the entity
     */
    public static void setSize(Object entity, float width, float height) {
        try {
            String methodName = deobfuscatedEnvironment() ? "setSize" : "func_70105_a";
            Method method = Entity.class.getDeclaredMethod(methodName, float.class, float.class);
            method.setAccessible(true);
            method.invoke(entity, width, height);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Calls the obfuscated function func_71013_b on a player with an integer argument.
     * This method is used internally by Minecraft for sleeping/wake-up logic.
     *
     * @param player The player instance (EntityPlayer)
     * @param i1     Argument to pass
     */
    public static void call_func_71013_b(Object player, int i1) {
        try {
            Method method = EntityPlayer.class.getDeclaredMethod("func_71013_b", int.class);
            method.setAccessible(true);
            method.invoke(player, i1);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
