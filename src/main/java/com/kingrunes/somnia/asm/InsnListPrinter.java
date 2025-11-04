package com.kingrunes.somnia.asm;

import java.util.Iterator;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Utility class to print the contents of an {@link InsnList} (ASM instruction list)
 * for debugging bytecode transformations.
 *
 * Works safely in the 1.7.10 environment; outputs method names or instruction info with indices.
 */
public final class InsnListPrinter {

    private InsnListPrinter() {
        // Utility class; prevent instantiation
    }

    /**
     * Prints all instructions in the provided list to {@link System#out} for debugging.
     *
     * @param instructions the {@link InsnList} to print; ignored if null or empty
     */
    public static void printInsnList(InsnList instructions) {
        if (instructions == null || instructions.size() == 0) {
            System.out.println("[Somnia/ASM] (empty instruction list)");
            return;
        }

        int index = 0;
        Iterator<AbstractInsnNode> iter = instructions.iterator();

        System.out.println("[Somnia/ASM] Printing " + instructions.size() + " instructions:");

        while (iter.hasNext()) {
            AbstractInsnNode node = iter.next();

            if (node instanceof MethodInsnNode) {
                MethodInsnNode min = (MethodInsnNode) node;
                System.out.printf("[Somnia/ASM] #%03d  Method call: %s.%s%s%n", index, min.owner, min.name, min.desc);
            } else {
                System.out.printf("[Somnia/ASM] #%03d  %s%n", index, node);
            }

            index++;
        }

        System.out.println("[Somnia/ASM] End of instruction list.");
    }
}
