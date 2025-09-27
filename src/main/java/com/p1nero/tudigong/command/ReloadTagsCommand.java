package com.p1nero.tudigong.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.p1nero.dialog_lib.network.DialoguePacketRelay;
import com.p1nero.tudigong.network.TDGPacketHandler;
import com.p1nero.tudigong.network.packet.client.SyncStructureTagsPacket;
import com.p1nero.tudigong.util.StructureTagManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;

public class ReloadTagsCommand {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> command) {
        command.then(Commands.literal("reload_tags")
                .requires(source -> source.hasPermission(2))
                .executes(ReloadTagsCommand::executeReloadTags)
        );
    }

    private static int executeReloadTags(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        StructureTagManager.load();
        syncTagsWithAllPlayers(source.getServer());
        source.sendSuccess(() -> Component.literal("Structure tags reloaded and synced with " + source.getServer().getPlayerList().getPlayerCount() + " players."), true);
        return 1;
    }

    private static void syncTagsWithAllPlayers(MinecraftServer server) {
        PlayerList playerList = server.getPlayerList();
        SyncStructureTagsPacket packet = new SyncStructureTagsPacket(StructureTagManager.getTags());
        playerList.getPlayers().forEach(player -> DialoguePacketRelay.sendToPlayer(TDGPacketHandler.INSTANCE, packet, player));
    }
}
