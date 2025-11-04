package com.kingrunes.somnia;

/**
 * Central version definition for the Somnia mod.
 * Keeps both the mod version and the "core" version consistent.
 *
 * Compatible with Forge 1.7.10.
 */
public final class SomniaVersion {

    // === Version constants ===
    public static final int MAJOR_VERSION = 1;
    public static final int MINOR_VERSION = 3;
    public static final int REVISION_VERSION = 0;
    public static final int BUILD = 15;

    // Core versioning (mirrors main version for now)
    public static final int CORE_MAJOR_VERSION = MAJOR_VERSION;
    public static final int CORE_MINOR_VERSION = MINOR_VERSION;
    public static final int CORE_REVISION_VERSION = REVISION_VERSION;
    public static final int CORE_BUILD = BUILD;

    private static final String FORMAT = "%d.%d.%d.%d";

    private SomniaVersion() {
        // Prevent instantiation
    }

    /**
     * Returns the full mod version string (e.g. "1.3.0.15").
     */
    public static String getVersionString() {
        return String.format(FORMAT, MAJOR_VERSION, MINOR_VERSION, REVISION_VERSION, BUILD);
    }

    /**
     * Returns the core version string (currently identical to mod version).
     */
    public static String getCoreVersionString() {
        return String.format(FORMAT, CORE_MAJOR_VERSION, CORE_MINOR_VERSION, CORE_REVISION_VERSION, CORE_BUILD);
    }
}
