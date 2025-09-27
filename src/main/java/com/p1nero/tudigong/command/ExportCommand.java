package com.p1nero.tudigong.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportCommand {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void register(LiteralArgumentBuilder<CommandSourceStack> command) {
        command.then(Commands.literal("export_structures")
                .requires(source -> source.hasPermission(2))
                .executes(ExportCommand::executeOriginal)
                .then(Commands.argument("lang", StringArgumentType.word())
                        .executes(ExportCommand::execute)
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String langCode;
        try {
            langCode = StringArgumentType.getString(context, "lang");
        } catch (IllegalArgumentException e) {
            return executeOriginal(context);
        }
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        File outputDir = server.getFile("exported_structures");
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            source.sendFailure(Component.literal("Failed to create directory: " + outputDir.getAbsolutePath()));
            return 0;
        }
        Map<String, String> translations = new HashMap<>();
        server.getResourceManager().listResources("lang", path -> path.getPath().endsWith(langCode + ".json")).forEach((rl, resource) -> {
            try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                JsonObject jsonObject = GSON.fromJson(reader, JsonObject.class);
                if (jsonObject != null) {
                    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        if (entry.getValue().isJsonPrimitive()) {
                            translations.put(entry.getKey(), entry.getValue().getAsString());
                        }
                    }
                }
            } catch (Exception ex) {
                source.sendFailure(Component.literal("Error reading lang file " + rl.toString() + ": " + ex.getMessage()));
            }
        });
        if (translations.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Warning: Could not load any translations for language '" + langCode + "'. Using fallback names."), false);
        }
        Map<String, Map<String, String>> modStructures = new HashMap<>();
        Registry<Structure> structureRegistry = server.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (ResourceKey<Structure> key : structureRegistry.registryKeySet()) {
            String modId = key.location().getNamespace();
            String structureId = key.location().toString();
            String translationKey = Util.makeDescriptionId("structure", key.location());
            String translatedName = translations.get(translationKey);
            if (translatedName == null) {
                String path = key.location().getPath();
                translatedName = WordUtils.capitalizeFully(path.replace('_', ' '));
            }
            modStructures.computeIfAbsent(modId, k -> new HashMap<>()).put(structureId, translatedName);
        }
        int filesWritten = 0;
        for (Map.Entry<String, Map<String, String>> entry : modStructures.entrySet()) {
            String modId = entry.getKey();
            Map<String, String> structures = entry.getValue();
            File outputFile = new File(outputDir, modId + "_" + langCode + ".json");
            try (FileWriter writer = new FileWriter(outputFile)) {
                GSON.toJson(structures, writer);
                filesWritten++;
            } catch (IOException e) {
                source.sendFailure(Component.literal("Failed to write file for mod '" + modId + "': " + e.getMessage()));
            }
        }
        int finalFilesWritten = filesWritten;
        File finalOutputDir = outputDir;
        source.sendSuccess(() -> Component.literal("Successfully exported " + finalFilesWritten + " files for language '" + langCode + "' to '" + finalOutputDir.getName() + "' directory."), true);
        return filesWritten;
    }

    private static int executeOriginal(CommandContext<CommandSourceStack> context) {
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
