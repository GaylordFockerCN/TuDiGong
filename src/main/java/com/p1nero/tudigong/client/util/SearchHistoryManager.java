package com.p1nero.tudigong.client.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;

@OnlyIn(Dist.CLIENT)
public class SearchHistoryManager {

    private static final int MAX_HISTORY_SIZE = 20;
    private static final Deque<SearchHistoryEntry> HISTORY = new ArrayDeque<>(MAX_HISTORY_SIZE);

    /**
     * Represents a single entry in the search history.
     *
     * @param searchTerm The original string the player searched for.
     * @param type       The type of search (e.g., \"Structure\", \"Biome\").
     * @param position   The resulting coordinates. Can be null if the search was for a biome or failed.
     * @param timestamp  The time the search was performed, for sorting.
     */
    public record SearchHistoryEntry(String searchTerm, Component type, @Nullable BlockPos position, long timestamp) {
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
    }

    public static void remove(SearchHistoryEntry entry) {
        HISTORY.remove(entry);
    }

    public static Deque<SearchHistoryEntry> getHistory() {
        return HISTORY;
    }

    public static void clearHistory() {
        HISTORY.clear();
    }
}
