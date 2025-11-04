package com.kingrunes.somnia.asm;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

/**
 * Somnia coremod entry point.
 *
 * Registers ASM transformers and defines exclusions to prevent the coremod
 * from transforming its own classes.
 */
@MCVersion("1.7.10")
@TransformerExclusions({ "com.kingrunes.somnia.asm" })
public class SFMLLoadingPlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { SClassTransformer.class.getName() };
    }

    @Override
    public String getModContainerClass() {
        return SDummyContainer.class.getName();
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // No setup data required
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
