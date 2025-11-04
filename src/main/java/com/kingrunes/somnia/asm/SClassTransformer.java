package com.kingrunes.somnia.asm;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Core transformer for Somnia.
 * Injects custom hooks into FMLCommonHandler, EntityRenderer, and WorldServer
 * to allow background ticking and world updates during extended sleep cycles.
 */
public class SClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) return null;

        return switch (name) {
            case "cpw.mods.fml.common.FMLCommonHandler" -> patchFMLCommonHandler(bytes);
            case "net.minecraft.client.renderer.EntityRenderer" -> patchEntityRenderer(bytes, false);
            case "bll" -> // obfuscated EntityRenderer
                patchEntityRenderer(bytes, true);
            case "net.minecraft.world.WorldServer" -> patchWorldServer(bytes, false);
            case "mj" -> // obfuscated WorldServer
                patchWorldServer(bytes, true);
            default -> bytes;
        };
    }

    /** Inject Somnia.tick() at start of FMLCommonHandler.onPostServerTick() */
    private byte[] patchFMLCommonHandler(byte[] bytes) {
        final String targetMethod = "onPostServerTick";

        ClassNode classNode = read(bytes);

        for (MethodNode method : classNode.methods) {
            if (method.name.equals(targetMethod)) {
                InsnList inject = new InsnList();
                inject.add(
                    new FieldInsnNode(
                        Opcodes.GETSTATIC,
                        "com/kingrunes/somnia/Somnia",
                        "instance",
                        "Lcom/kingrunes/somnia/Somnia;"));
                inject.add(
                    new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/kingrunes/somnia/Somnia", "tick", "()V", false));

                method.instructions.insertBefore(method.instructions.getFirst(), inject);
                break;
            }
        }

        return write(classNode);
    }

    /** Modify EntityRenderer to redirect renderWorld calls to Somnia.renderWorld */
    private byte[] patchEntityRenderer(byte[] bytes, boolean obfuscated) {
        final String updateCameraAndRender = obfuscated ? "b" : "updateCameraAndRender";
        final String renderWorld = obfuscated ? "a" : "renderWorld";

        ClassNode classNode = read(bytes);
        boolean found = false;

        for (MethodNode method : classNode.methods) {
            if (method.name.equals(updateCameraAndRender) && method.desc.equals("(F)V")) {
                for (AbstractInsnNode insn : method.instructions.toArray()) {
                    if (insn instanceof MethodInsnNode min && min.name.equals(renderWorld)
                        && min.desc.equalsIgnoreCase("(FJ)V")
                        && min.getOpcode() == Opcodes.INVOKEVIRTUAL) {

                        // Replace call with static Somnia.renderWorld(FJ)V
                        min.setOpcode(Opcodes.INVOKESTATIC);
                        min.name = "renderWorld";
                        min.owner = "com/kingrunes/somnia/Somnia";

                        // Remove preceding VarInsnNode(s) used to push this/context argument
                        int targetIndex = method.instructions.indexOf(min);
                        AbstractInsnNode toRemove = method.instructions.get(targetIndex - 3);
                        method.instructions.remove(toRemove);

                        found = true;
                    }
                }
                if (found) break;
            }
        }

        return write(classNode);
    }

    /** Patch WorldServer.tick() to redirect doMobSpawning rule to Somnia.doMobSpawning() */
    private byte[] patchWorldServer(byte[] bytes, boolean obfuscated) {
        final String tickMethod = obfuscated ? "b" : "tick";
        final String getGameRule = obfuscated ? "b" : "getGameRuleBooleanValue";

        ClassNode classNode = read(bytes);

        for (MethodNode method : classNode.methods) {
            if (method.name.equals(tickMethod) && method.desc.equals("()V")) {

                for (AbstractInsnNode insn : method.instructions.toArray()) {
                    if (insn instanceof MethodInsnNode min && min.name.equals(getGameRule)
                        && min.desc.equals("(Ljava/lang/String;)Z")) {

                        int index = method.instructions.indexOf(min);
                        AbstractInsnNode prev = method.instructions.get(index - 1);

                        if (prev instanceof LdcInsnNode ldc && "doMobSpawning".equals(ldc.cst)) {

                            // Replace call with static Somnia.doMobSpawning(WorldServer)Z
                            min.setOpcode(Opcodes.INVOKESTATIC);
                            min.desc = "(Lnet/minecraft/world/WorldServer;)Z";
                            min.name = "doMobSpawning";
                            min.owner = "com/kingrunes/somnia/Somnia";

                            // Clean up unused string and previous stack load
                            method.instructions.remove(ldc);
                            method.instructions.remove(method.instructions.get(index - 2));

                            break;
                        }
                    }
                }
                break;
            }
        }

        return write(classNode);
    }

    // Utility: Read class into a manipulable tree
    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    // Utility: Write back class bytes
    private static byte[] write(ClassNode node) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        node.accept(cw);
        return cw.toByteArray();
    }
}
