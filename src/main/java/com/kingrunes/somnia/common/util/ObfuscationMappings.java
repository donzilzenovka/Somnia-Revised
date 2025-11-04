package com.kingrunes.somnia.common.util;

/**
 * Holds field and method names for deobfuscated and obfuscated environments.
 */
public final class ObfuscationMappings {

    // EntityPlayer sleeping state
    public static final String DEOBF_ENTITY_PLAYER_SLEEPING = "sleeping";
    public static final String OBF_ENTITY_PLAYER_SLEEPING = "field_71083_bS";

    // EntityPlayer sleep timer
    public static final String DEOBF_ENTITY_PLAYER_SLEEP_TIMER = "sleepTimer";
    public static final String OBF_ENTITY_PLAYER_SLEEP_TIMER = "field_71076_b";

    // Entity setSize method
    public static final String DEOBF_ENTITY_SET_SIZE = "setSize";
    public static final String OBF_ENTITY_SET_SIZE = "func_70105_a";

    // Private constructor to prevent instantiation
    private ObfuscationMappings() {}
}
