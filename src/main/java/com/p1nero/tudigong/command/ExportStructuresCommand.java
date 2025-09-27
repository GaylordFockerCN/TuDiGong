package com.p1nero.tudigong.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportStructuresCommand {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tudigong")
                .then(Commands.literal("export_structures")
                        .requires(source -> source.hasPermission(2))
                        .executes(ExportStructuresCommand::execute)
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        File outputDir = server.getFile("exported_structures");

        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                source.sendFailure(Component.literal("Failed to create directory: " + outputDir.getAbsolutePath()));
                return 0;
            }
        }

        Map<String, List<String>> modStructures = new HashMap<>();
        Registry<Structure> structureRegistry = server.registryAccess().registryOrThrow(Registries.STRUCTURE);

        for (ResourceKey<Structure> key : structureRegistry.registryKeySet()) {
            String modId = key.location().getNamespace();
            modStructures.computeIfAbsent(modId, k -> new ArrayList<>()).add(key.location().toString());
        }

        int filesWritten = 0;
        for (Map.Entry<String, List<String>> entry : modStructures.entrySet()) {
            String modId = entry.getKey();
            List<String> structures = entry.getValue();
            File outputFile = new File(outputDir, modId + ".json");

            try (FileWriter writer = new FileWriter(outputFile)) {
                GSON.toJson(structures, writer);
                filesWritten++;
            } catch (IOException e) {
                source.sendFailure(Component.literal("Failed to write file for mod '" + modId + "': " + e.getMessage()));
            }
        }

        int finalFilesWritten = filesWritten;
        File finalOutputDir = outputDir;
        source.sendSuccess(() -> Component.literal("Successfully exported " + finalFilesWritten + " files to '" + finalOutputDir.getName() + "' directory."), true);
        return filesWritten;
    }
}
