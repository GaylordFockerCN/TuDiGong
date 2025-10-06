package com.p1nero.tudigong.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.p1nero.tudigong.TuDiGongMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SearchHistoryManager {

    private static final int MAX_HISTORY_SIZE = 20;
    private static final Deque<SearchHistoryEntry> HISTORY = new ArrayDeque<>(MAX_HISTORY_SIZE);
    private static final File HISTORY_FILE = new File(FMLPaths.CONFIGDIR.get().toFile(), "tudigong/search_history.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Represents a single entry in the search history.
     */
    public record SearchHistoryEntry(String searchTerm, Component type, @Nullable BlockPos position, @Nullable ResourceKey<Level> dimension, long timestamp) {
    }

    public static void addEntry(SearchHistoryEntry entry) {
        // Prevent exact duplicates
        HISTORY.removeIf(e -> e.equals(entry));
        // Add to the front
        HISTORY.addFirst(entry);
        // Trim the history if it's too large
        if (HISTORY.size() > MAX_HISTORY_SIZE) {
            HISTORY.removeLast();
        }
        save();
    }

    public static void remove(SearchHistoryEntry entry) {
        HISTORY.remove(entry);
        save();
    }

    public static Deque<SearchHistoryEntry> getHistory() {
        return HISTORY;
    }

    public static void clearHistory() {
        HISTORY.clear();
        save();
    }

    public static void save() {
        HISTORY_FILE.getParentFile().mkdirs();
        List<HistorySaveEntry> saveEntries = new ArrayList<>();
        for (SearchHistoryEntry entry : HISTORY) {
            saveEntries.add(new HistorySaveEntry(entry));
        }
        try (FileWriter writer = new FileWriter(HISTORY_FILE)) {
            GSON.toJson(saveEntries, writer);
        } catch (IOException e) {
            TuDiGongMod.LOGGER.error("Failed to save search history", e);
        }
    }

    public static void load() {
        if (!HISTORY_FILE.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(HISTORY_FILE)) {
            Type listType = new TypeToken<List<HistorySaveEntry>>() {}.getType();
            List<HistorySaveEntry> saveEntries = GSON.fromJson(reader, listType);
            if (saveEntries != null) {
                HISTORY.clear();
                for (HistorySaveEntry saveEntry : saveEntries) {
                    HISTORY.add(saveEntry.toHistoryEntry());
                }
            }
        } catch (Exception e) {
            TuDiGongMod.LOGGER.error("Failed to load search history", e);
        }
    }

    private static class HistorySaveEntry {
        String searchTerm;
        String type;
        int[] position;
        String dimension;
        long timestamp;

        public HistorySaveEntry(SearchHistoryEntry entry) {
            this.searchTerm = entry.searchTerm();
            this.type = Component.Serializer.toJson(entry.type());
            if (entry.position() != null) {
                this.position = new int[]{entry.position().getX(), entry.position().getY(), entry.position().getZ()};
            }
            if (entry.dimension() != null) {
                this.dimension = entry.dimension().location().toString();
            }
            this.timestamp = entry.timestamp();
        }

        public SearchHistoryEntry toHistoryEntry() {
            Component typeComp = Component.Serializer.fromJson(this.type);
            BlockPos pos = this.position != null ? new BlockPos(this.position[0], this.position[1], this.position[2]) : null;
            ResourceKey<Level> dimKey = this.dimension != null ? ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(this.dimension)) : null;
            return new SearchHistoryEntry(this.searchTerm, typeComp, pos, dimKey, this.timestamp);
        }
    }
}