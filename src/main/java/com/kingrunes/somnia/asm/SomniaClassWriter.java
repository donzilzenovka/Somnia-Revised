package com.kingrunes.somnia.asm;

import org.objectweb.asm.ClassWriter;

/**
 * Custom ClassWriter for Somnia.
 *
 * Allows overriding or caching of the computed common superclass during
 * ASM transformations to avoid lookup issues under obfuscation or runtime
 * mismatches between classes.
 */
public class SomniaClassWriter extends ClassWriter {

    private String cachedCommonSuperClass;

    /**
     * Constructs a SomniaClassWriter with optional precomputed superclass value.
     *
     * @param flags                         ASM computation flags (e.g., COMPUTE_FRAMES, COMPUTE_MAXS)
     * @param precalculatedCommonSuperClass if non-null, this value will override
     *                                      getCommonSuperClass results
     */
    public SomniaClassWriter(int flags, String precalculatedCommonSuperClass) {
        super(flags);
        this.cachedCommonSuperClass = precalculatedCommonSuperClass;
    }

    @Override
    protected String getCommonSuperClass(String typeA, String typeB) {
        System.out.println("[Somnia][DEBUG] getCommonSuperClass called with: a=" + typeA + ", b=" + typeB);

        if (cachedCommonSuperClass != null) {
            System.out.println("[Somnia] Using cached superclass: " + cachedCommonSuperClass);
        } else {
            cachedCommonSuperClass = super.getCommonSuperClass(typeA, typeB);
            System.out.println("[Somnia][DEBUG] Computed superclass: " + cachedCommonSuperClass);
        }

        return cachedCommonSuperClass;
    }
}
