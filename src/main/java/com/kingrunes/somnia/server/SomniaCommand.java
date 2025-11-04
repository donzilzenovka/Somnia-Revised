package com.kingrunes.somnia.server;

import java.lang.ref.WeakReference;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.util.ListUtils;

public class SomniaCommand extends CommandBase {

    private static final String COMMAND_NAME = "somnia";
    private static final String COMMAND_USAGE = "[override] [add|remove|list] <player>";
    private static final String COMMAND_USAGE_CONSOLE = "[override] [add|remove|list] [player]";

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
        if (args.length < 2) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        if (args[0].equalsIgnoreCase("override")) {
            EntityPlayerMP player;

            if (args.length > 2) {
                player = MinecraftServer.getServer()
                    .getConfigurationManager()
                    .func_152612_a(args[2]);
            } else if (sender instanceof EntityPlayerMP) {
                player = (EntityPlayerMP) sender;
            } else {
                throw new WrongUsageException(String.format("/%s %s", COMMAND_NAME, COMMAND_USAGE_CONSOLE));
            }

            switch (args[1].toLowerCase()) {
                case "add":
                    Somnia.instance.ignoreList.add(new WeakReference<>(player));
                    break;
                case "remove":
                    Somnia.instance.ignoreList.remove(ListUtils.getWeakRef(player, Somnia.instance.ignoreList));
                    break;
                case "list":
                    List<EntityPlayerMP> players = ListUtils.extractRefs(Somnia.instance.ignoreList);
                    String[] astring = ListUtils.playersToStringArray(players);
                    ChatComponentText chatComponent = new ChatComponentText(
                        (astring.length > 0) ? joinNiceString((Object[]) astring) : "Nothing to see here...");
                    sender.addChatMessage(chatComponent);
                    break;
                default:
                    throw new WrongUsageException(getCommandUsage(sender));
            }
        }
    }
}
