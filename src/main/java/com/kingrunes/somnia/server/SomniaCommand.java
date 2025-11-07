package com.kingrunes.somnia.server;

import java.lang.ref.WeakReference;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.tiredness.TirednessData;
import com.kingrunes.somnia.common.util.ListUtils;

public class SomniaCommand extends CommandBase {

    private static final String COMMAND_NAME = "somnia";
    private static final String COMMAND_USAGE = "Type /somnia for details";

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return String.format("/%s %s", COMMAND_NAME, COMMAND_USAGE);
    }

    public int func_82362_a() {
        return 3;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        // If no arguments, show general help
        if (args.length < 1) {
            sendHelp(sender);
            return;
        }

        // -----------------------------
        // Existing override handling
        // -----------------------------
        if (args[0].equalsIgnoreCase("override")) {
            if (args.length < 2) {
                sendHelp(sender);
                return;
            }

            EntityPlayerMP player;
            if (args.length > 2) {
                player = MinecraftServer.getServer()
                    .getConfigurationManager()
                    .func_152612_a(args[2]);
            } else if (sender instanceof EntityPlayerMP) {
                player = (EntityPlayerMP) sender;
            } else {
                sendHelp(sender);
                return;
            }

            switch (args[1].toLowerCase()) {
                case "add":
                    Somnia.instance.ignoreList.add(new WeakReference<>(player));
                    sender.addChatMessage(
                        new ChatComponentText("Added " + player.getCommandSenderName() + " to override list."));
                    break;
                case "remove":
                    Somnia.instance.ignoreList.remove(ListUtils.getWeakRef(player, Somnia.instance.ignoreList));
                    sender.addChatMessage(
                        new ChatComponentText("Removed " + player.getCommandSenderName() + " from override list."));
                    break;
                case "list":
                    List<EntityPlayerMP> players = ListUtils.extractRefs(Somnia.instance.ignoreList);
                    String[] astring = ListUtils.playersToStringArray(players);
                    sender.addChatMessage(
                        new ChatComponentText(
                            (astring.length > 0) ? joinNiceString((Object[]) astring) : "Nothing to see here..."));
                    break;
                default:
                    sendHelp(sender);
                    break;
            }
            return;
        }

        // -----------------------------
        // Tiredness system commands
        // -----------------------------
        if (args[0].equalsIgnoreCase("tiredness")) {
            if (!sender.canCommandSenderUseCommand(2, COMMAND_NAME)) {
                sender.addChatMessage(new ChatComponentText("You do not have permission to use this command."));
                return;
            }

            if (args.length < 2) {
                sendTirednessHelp(sender);
                return;
            }

            String sub = args[1];

            if (sub.equalsIgnoreCase("toggle")) {
                boolean newState = !Somnia.proxy.enableTirednessSystem;
                Somnia.proxy.enableTirednessSystem = newState;
                sender.addChatMessage(
                    new ChatComponentText("Tiredness system is now " + (newState ? "ENABLED" : "DISABLED")));
                return;
            }

            if (args.length < 3) {
                sendTirednessHelp(sender);
                return;
            }

            EntityPlayerMP player = MinecraftServer.getServer()
                .getConfigurationManager()
                .func_152612_a(args[2]);
            if (player == null) {
                sender.addChatMessage(new ChatComponentText("Player not found: " + args[2]));
                return;
            }

            TirednessData data = TirednessData.get(player);
            if (data == null) {
                TirednessData.register(player);
                data = TirednessData.get(player);
            }

            if (sub.equalsIgnoreCase("get")) {
                sender.addChatMessage(
                    new ChatComponentText(player.getCommandSenderName() + " tiredness: " + data.getExhaustion()));
            } else if (sub.equalsIgnoreCase("set")) {
                if (args.length < 4) {
                    sender.addChatMessage(new ChatComponentText("You must specify a value between 0.0 and 100.0"));
                    return;
                }
                try {
                    float val = Float.parseFloat(args[3]);
                    val = Math.max(0.0F, Math.min(100.0F, val));
                    data.setExhaustion(val);
                    data.saveToPlayer(player);
                    sender.addChatMessage(
                        new ChatComponentText(player.getCommandSenderName() + " tiredness set to " + val));
                } catch (NumberFormatException e) {
                    sender.addChatMessage(new ChatComponentText("Invalid number: " + args[3]));
                }
            } else if (sub.equalsIgnoreCase("reset")) {
                data.resetFatigueData(player);
                sender.addChatMessage(
                    new ChatComponentText(player.getCommandSenderName() + " tiredness has been reset."));
            } else {
                sendTirednessHelp(sender);
            }
        }
    }

    // -----------------------------
    // Help methods
    // -----------------------------
    private void sendHelp(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText("/somnia [override] [add|remove|list] <player>"));
        sender.addChatMessage(new ChatComponentText("/somnia tiredness [get|set|reset] <player> <value>"));
        sender.addChatMessage(new ChatComponentText("/somnia tiredness [toggle]"));
    }

    private void sendTirednessHelp(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText("Tiredness commands:"));
        sender.addChatMessage(new ChatComponentText("/somnia tiredness get <player>"));
        sender.addChatMessage(new ChatComponentText("/somnia tiredness set <player> [0.0~100.0]"));
        sender.addChatMessage(new ChatComponentText("/somnia tiredness reset <player>"));
        sender.addChatMessage(new ChatComponentText("/somnia tiredness toggle"));
    }

}
