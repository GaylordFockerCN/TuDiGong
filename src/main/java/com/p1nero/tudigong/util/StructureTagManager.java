package com.p1nero.tudigong.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.p1nero.tudigong.TuDiGongMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class StructureTagManager {
    private static final Gson GSON = new Gson();
    private static final File CONFIG_FILE = new File(FMLPaths.CONFIGDIR.get().toFile(), "tudigong/structure_tags.json");
    private static Map<String, Set<ResourceLocation>> tags = new HashMap<>();

    public static void load() {
        tags.clear();
        if (!CONFIG_FILE.exists()) {
            createDefaultConfigFile();
            return;
        }
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root != null && root.has("tags")) {
                Type tagsType = new TypeToken<Map<String, List<String>>>() {}.getType();
                Map<String, List<String>> rawTags = GSON.fromJson(root.get("tags"), tagsType);
                if (rawTags != null) {
                    rawTags.forEach((tag, structures) -> {
                        Set<ResourceLocation> locs = structures.stream().map(ResourceLocation::parse).collect(Collectors.toSet());
                        tags.put(tag.toLowerCase(), locs);
                    });
                }
            }
        } catch (Exception e) {
            TuDiGongMod.LOGGER.error("Failed to load structure tags", e);
        }
    }

    private static void createDefaultConfigFile() {
        CONFIG_FILE.getParentFile().mkdirs();
        Map<String, Object> defaultConfig = new LinkedHashMap<>();
        defaultConfig.put("//", "Maps a tag name to a list of structure resource locations. Example below.");
        Map<String, List<String>> exampleTags = new LinkedHashMap<>();
        exampleTags.put("village", Collections.singletonList("minecraft:village_plains"));
        exampleTags.put("ocean", Arrays.asList("minecraft:shipwreck", "minecraft:ocean_ruin_warm"));
        defaultConfig.put("tags", exampleTags);

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(defaultConfig, writer);
        } catch (Exception e) {
            TuDiGongMod.LOGGER.error("Failed to create default structure tags config", e);
        }

        // Populate the live map with the default values
        exampleTags.forEach((tag, structures) -> {
            Set<ResourceLocation> locs = structures.stream().map(ResourceLocation::parse).collect(Collectors.toSet());
            tags.put(tag.toLowerCase(), locs);
        });
    }

    public static Optional<ResourceLocation> getRandomStructureForTag(String tagName) {
        tagName = tagName.toLowerCase();
        Set<ResourceLocation> structures = tags.get(tagName);
        if (structures == null || structures.isEmpty()) {
            return Optional.empty();
        }
        List<ResourceLocation> structureList = new ArrayList<>(structures);
        return Optional.of(structureList.get(new Random().nextInt(structureList.size())));
    }

    public static Map<String, Set<ResourceLocation>> getTags() {
        return tags;
    }

    public static boolean createTag(String tagName) {
        tagName = tagName.toLowerCase();
        if (tags.containsKey(tagName)) {
            return false; // Tag already exists
        }
        tags.put(tagName, new HashSet<>());
        return true;
    }

    public static boolean addStructureToTag(String tagName, ResourceLocation structureId) {
        tagName = tagName.toLowerCase();
        if (!tags.containsKey(tagName)) {
            return false; // Tag doesn't exist
        }
        return tags.get(tagName).add(structureId);
    }

    public static boolean removeStructureFromTag(String tagName, ResourceLocation structureId) {
        tagName = tagName.toLowerCase();
        if (!tags.containsKey(tagName)) {
            return false; // Tag doesn't exist
        }
        return tags.get(tagName).remove(structureId);
    }

    public static void save() {
        CONFIG_FILE.getParentFile().mkdirs();

        Map<String, List<String>> serializableTags = new HashMap<>();
        tags.forEach((tag, locs) -> {
            List<String> locStrings = locs.stream().map(ResourceLocation::toString).sorted().collect(Collectors.toList());
            serializableTags.put(tag, locStrings);
        });

        Map<String, Object> configToWrite = new LinkedHashMap<>();
        configToWrite.put("//", "Maps a tag name to a list of structure resource locations.");
        configToWrite.put("tags", serializableTags);

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(configToWrite, writer);
        } catch (Exception e) {
            TuDiGongMod.LOGGER.error("Failed to save structure tags config", e);
        }
    }
}
