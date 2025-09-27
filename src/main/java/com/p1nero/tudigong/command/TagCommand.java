package com.p1nero.tudigong.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.p1nero.dialog_lib.network.DialoguePacketRelay;
import com.p1nero.tudigong.network.TDGPacketHandler;
import com.p1nero.tudigong.network.packet.client.SyncStructureTagsPacket;
import com.p1nero.tudigong.util.StructureTagManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;

import java.util.Set;
import java.util.stream.Stream;

public class TagCommand {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> command) {
        command.then(Commands.literal("tag")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("create")
                        .then(Commands.argument("tagName", StringArgumentType.word())
                                .executes(c -> executeTagCreate(c.getSource(), StringArgumentType.getString(c, "tagName")))
                        )
                )
                .then(Commands.literal("add")
                        .then(Commands.argument("tagName", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(StructureTagManager.getTags().keySet(), b))
                                .then(Commands.argument("structure", ResourceLocationArgument.id())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(c.getSource().getServer().registryAccess().registryOrThrow(Registries.STRUCTURE).keySet().stream().map(ResourceLocation::toString), b))
                                        .executes(c -> executeTagAdd(c.getSource(), StringArgumentType.getString(c, "tagName"), ResourceLocationArgument.getId(c, "structure")))
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("tagName", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(StructureTagManager.getTags().keySet(), b))
                                .then(Commands.argument("structure", ResourceLocationArgument.id())
                                        .suggests((c, b) -> {
                                            String tagName = StringArgumentType.getString(c, "tagName");
                                            Set<ResourceLocation> structures = StructureTagManager.getTags().get(tagName.toLowerCase());
                                            return SharedSuggestionProvider.suggest(structures != null ? structures.stream().map(ResourceLocation::toString) : Stream.empty(), b);
                                        })
                                        .executes(c -> executeTagRemove(c.getSource(), StringArgumentType.getString(c, "tagName"), ResourceLocationArgument.getId(c, "structure")))
                                )
                        )
                )
                .then(Commands.literal("list")
                        .then(Commands.argument("tagName", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(StructureTagManager.getTags().keySet(), b))
                                .executes(c -> executeTagList(c.getSource(), StringArgumentType.getString(c, "tagName")))
                                )
                        )
                .then(Commands.literal("list_all")
                        .executes(c -> executeTagListAll(c.getSource()))
                )
        );
    }

    private static void syncTagsWithAllPlayers(MinecraftServer server) {
        PlayerList playerList = server.getPlayerList();
        SyncStructureTagsPacket packet = new SyncStructureTagsPacket(StructureTagManager.getTags());
        playerList.getPlayers().forEach(player -> DialoguePacketRelay.sendToPlayer(TDGPacketHandler.INSTANCE, packet, player));
    }

    private static int executeTagCreate(CommandSourceStack source, String tagName) {
        if (StructureTagManager.createTag(tagName)) {
            StructureTagManager.save();
            syncTagsWithAllPlayers(source.getServer());
            source.sendSuccess(() -> Component.literal("Tag '" + tagName + "' created."), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("Tag '" + tagName + "' already exists."));
            return 0;
        }
    }

    private static int executeTagAdd(CommandSourceStack source, String tagName, ResourceLocation structureId) {
        if (source.getServer().registryAccess().registryOrThrow(Registries.STRUCTURE).get(structureId) == null) {
            source.sendFailure(Component.literal("Structure '" + structureId + "' does not exist."));
            return 0;
        }
        if (StructureTagManager.addStructureToTag(tagName, structureId)) {
            StructureTagManager.save();
            syncTagsWithAllPlayers(source.getServer());
            source.sendSuccess(() -> Component.literal("Added " + structureId + " to tag '" + tagName + "'."), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("Tag '" + tagName + "' does not exist or already contains the structure."));
            return 0;
        }
    }

    private static int executeTagRemove(CommandSourceStack source, String tagName, ResourceLocation structureId) {
        if (StructureTagManager.removeStructureFromTag(tagName, structureId)) {
            StructureTagManager.save();
            syncTagsWithAllPlayers(source.getServer());
            source.sendSuccess(() -> Component.literal("Removed " + structureId + " from tag '" + tagName + "'."), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("Tag '" + tagName + "' does not exist or does not contain the structure."));
            return 0;
        }
    }

    private static int executeTagList(CommandSourceStack source, String tagName) {
        final String lowerCaseTagName = tagName.toLowerCase();
        Set<ResourceLocation> structures = StructureTagManager.getTags().get(lowerCaseTagName);
        if (structures != null) {
            source.sendSuccess(() -> Component.literal("Structures in tag '" + lowerCaseTagName + "' (" + structures.size() + "):"), false);
            structures.forEach(loc -> source.sendSuccess(() -> Component.literal("- " + loc.toString()), false));
            return structures.size();
        } else {
            source.sendFailure(Component.literal("Tag '" + lowerCaseTagName + "' not found."));
            return 0;
        }
    }

    private static int executeTagListAll(CommandSourceStack source) {
        Set<String> tags = StructureTagManager.getTags().keySet();
        if (!tags.isEmpty()) {
            source.sendSuccess(() -> Component.literal("All tags (" + tags.size() + "):"), false);
            tags.forEach(tag -> source.sendSuccess(() -> Component.literal("- " + tag), false));
            return tags.size();
        } else {
            source.sendSuccess(() -> Component.literal("No tags defined."), false);
            return 0;
        }
    }
}
