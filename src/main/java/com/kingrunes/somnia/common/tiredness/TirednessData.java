package com.kingrunes.somnia.common.tiredness;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class TirednessData {

    private static final String NBT_TAG_TIREDNESS = "SomniaTiredness";
    private static final String NBT_TAG_DROWSY = "SomniaDrowsy";
    private static final Float RECOVERY_RATE = 0.5F;

    private float tiredness = 0.0F;
    private boolean drowsy = false;
    public boolean wasSleeping = false;

    // -----------------------------
    // Tiredness accessors
    // -----------------------------
    public float getExhaustion() {
        return tiredness;
    }

    public void setExhaustion(float tiredness) {
        this.tiredness = Math.max(0.0F, Math.min(100.0F, tiredness));
    }

    public void changeFatigue(float amount) {
        setExhaustion(this.tiredness + amount);
    }

    public void resetFatigueData(EntityPlayer player) {
        this.tiredness = 0.0F;
        this.drowsy = false;
        this.wasSleeping = false;

        saveToPlayer(player);
    }

    // -----------------------------
    // Drowsy accessors
    // -----------------------------
    public boolean isDrowsy() {
        return drowsy;
    }

    public void setDrowsy(boolean value) {
        drowsy = value;
    }

    // -----------------------------
    // NBT serialization
    // -----------------------------
    public NBTTagCompound writeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setFloat(NBT_TAG_TIREDNESS, tiredness);
        tag.setBoolean(NBT_TAG_DROWSY, drowsy);
        return tag;
    }

    public void readNBT(NBTTagCompound tag) {
        if (tag.hasKey(NBT_TAG_TIREDNESS)) {
            this.tiredness = tag.getFloat(NBT_TAG_TIREDNESS);
        }
        if (tag.hasKey(NBT_TAG_DROWSY)) {
            this.drowsy = tag.getBoolean(NBT_TAG_DROWSY);
        }
    }

    // -----------------------------
    // Static helpers
    // -----------------------------
    public static TirednessData get(EntityPlayer player) {
        if (player.getEntityData()
            .hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            NBTTagCompound persist = player.getEntityData()
                .getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            if (persist.hasKey(NBT_TAG_TIREDNESS)) {
                TirednessData data = new TirednessData();
                data.readNBT(persist);
                return data;
            }
        }
        return null;
    }

    public static void register(EntityPlayer player) {
        NBTTagCompound persist = player.getEntityData()
            .getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        persist.setFloat(NBT_TAG_TIREDNESS, 0.0F);
        persist.setBoolean(NBT_TAG_DROWSY, false);
        player.getEntityData()
            .setTag(EntityPlayer.PERSISTED_NBT_TAG, persist);
    }

    public void saveToPlayer(EntityPlayer player) {
        NBTTagCompound persist = player.getEntityData()
            .getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        persist.setFloat(NBT_TAG_TIREDNESS, tiredness);
        persist.setBoolean(NBT_TAG_DROWSY, drowsy);
        player.getEntityData()
            .setTag(EntityPlayer.PERSISTED_NBT_TAG, persist);
    }
}
