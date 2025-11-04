/*    */
package com.kingrunes.somnia.asm;

/*    */
/*    */ import com.google.common.eventbus.EventBus;
/*    */ import com.kingrunes.somnia.SomniaVersion;
/*    */ import cpw.mods.fml.common.DummyModContainer;
/*    */ import cpw.mods.fml.common.LoadController;
/*    */ import cpw.mods.fml.common.ModMetadata;
/*    */ import java.util.Arrays;

/*    */
/*    */
/*    */
/*    */ public class SDummyContainer/*    */ extends DummyModContainer
/*    */ {

    /*    */ public SDummyContainer() {
        /* 16 */ super(new ModMetadata());
        /* 17 */ ModMetadata meta = getMetadata();
        /* 18 */ meta.modId = "SomniaCore";
        /* 19 */ meta.name = "SomniaCore";
        /* 20 */ meta.version = SomniaVersion.getCoreVersionString();
        /* 21 */ meta.authorList = Arrays.asList(new String[] { "Kingrunes" });
        /* 22 */ meta.description = "This mod modifies Minecraft to allow Somnia to hook in";
        /* 23 */ meta.url = "";
        /* 24 */ meta.updateUrl = "";
        /* 25 */ meta.screenshots = new String[0];
        /* 26 */ meta.logoFile = "";
        /*    */ }

    /*    */
    /*    */
    /*    */
    /*    */ public boolean registerBus(EventBus bus, LoadController controller) {
        /* 32 */ bus.register(this);
        /* 33 */ return true;
        /*    */ }
    /*    */ }

/*
 * Location: C:\MinecraftModding\Decompiled\somnia_mod_1.7.10.jar!\com\kingrunes\somnia\asm\SDummyContainer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version: 1.1.3
 */
