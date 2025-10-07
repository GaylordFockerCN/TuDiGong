package com.p1nero.tudigong.client.widget;

import com.google.common.collect.BiMap;
import com.p1nero.tudigong.client.screen.StructureSearchScreen;
import com.p1nero.tudigong.compat.JECharactersIntegration;
import com.p1nero.tudigong.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ResourceList extends ObjectSelectionList<ResourceList.Entry> {

    private final BiMap<ResourceLocation, String> map;
    private final Map<String, Set<ResourceLocation>> tags;
    private final Map<String, Set<ResourceLocation>> modIds;
    private final Map<String, Set<ResourceLocation>> sets;
    private final Map<String, Set<ResourceLocation>> types;
    private final Map<ResourceLocation, ResourceLocation> structureToTypeMap;
    private final Map<ResourceLocation, List<ResourceLocation>> dimensions;
    private final EditBox box;
    private final Map<ResourceLocation, Set<String>> resourceToTagsMap;

    public ResourceList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight, BiMap<ResourceLocation, String> map, EditBox box, Map<String, Set<ResourceLocation>> tags, Map<String, Set<ResourceLocation>> modIds, Map<String, Set<ResourceLocation>> sets, Map<ResourceLocation, List<ResourceLocation>> dimensions, Map<String, Set<ResourceLocation>> types, Map<ResourceLocation, ResourceLocation> structureToTypeMap) {
        super(minecraft, width, height, y0, y1, itemHeight);
        this.setRenderBackground(false); // 泥土丑甚
        this.map = map;
        this.box = box;
        this.tags = tags;
        this.modIds = modIds;
        this.sets = sets;
        this.dimensions = dimensions;
        this.types = types;
        this.structureToTypeMap = structureToTypeMap;

        // Pre-compute a reverse map from resource location to its tags for faster lookups
        this.resourceToTagsMap = new HashMap<>();
        if (tags != null) {
            tags.forEach((tagName, resources) -> {
                for (ResourceLocation resource : resources) {
                    this.resourceToTagsMap.computeIfAbsent(resource, k -> new HashSet<>()).add(tagName);
                }
            });
        }
    }

    @Override
    protected void renderBackground(@NotNull GuiGraphics guiGraphics) {
        guiGraphics.fillGradient(x0, y0, x1, y1, -1072689136, -804253680);
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x1 - 6;
    }

    @Override
    public void setSelected(Entry entry) {
        super.setSelected(entry);
        if (entry != null) {
            box.setValue(entry.value);
        }
    }

    /**
     * Refreshes the list based on the search keyword.
     * The search logic supports:
     * - An empty keyword to show all Structures.
     * - Prefix search with '#' for tags, '@' for mod IDs, '$' for sets, and '~' for types.
     * - Default search by Structure name and resource location ID.
     *
     * @param keyword The search term.
     */
    public void refresh(String keyword) {
        this.setScrollAmount(0.0D);
        this.children().clear();

        List<String> results;
        if (StringUtil.isNullOrEmpty(keyword)) {
            results = new ArrayList<>(this.map.values());
        } else {
            results = findMatches(keyword);
        }

        results.stream().sorted().map(Entry::new).forEach(this::addEntry);
    }

    private List<String> findMatches(String keyword) {
        String lowerCaseKeyword = keyword.toLowerCase();

        // Handle prefix search
        if (keyword.startsWith("#") || keyword.startsWith("@") || keyword.startsWith("$") || keyword.startsWith("~")) {
            char prefix = keyword.charAt(0);
            String term = keyword.length() > 1 ? keyword.substring(1) : "";
            Map<String, Set<ResourceLocation>> lookupMap;
            switch (prefix) {
                case '#':
                    lookupMap = this.tags;
                    term = term.toLowerCase();
                    break;
                case '@':
                    lookupMap = this.modIds;
                    term = term.toLowerCase();
                    break;
                case '$':
                    lookupMap = this.sets;
                    term = term.toLowerCase();
                    break;
                case '~':
                    lookupMap = this.types;
                    term = term.toLowerCase();
                    break;
                default:
                    return Collections.emptyList();
            }

            if (lookupMap != null) {
                String finalTerm = term;
                Set<ResourceLocation> matches = lookupMap.entrySet().stream()
                        .filter(entry -> entry.getKey().toLowerCase().contains(finalTerm))
                        .flatMap(entry -> entry.getValue().stream())
                        .collect(Collectors.toSet());

                return matches.stream()
                        .map(this.map::get)
                        .filter(Objects::nonNull)
                        .sorted()
                        .collect(Collectors.toList());
            }
        }

        // Fallback to a more comprehensive default search
        Set<String> matches = new HashSet<>();
        this.map.forEach((id, name) -> {
            // Search in name
            if (JECharactersIntegration.match(name, lowerCaseKeyword)) {
                matches.add(name);
                return; // Already matched, no need to check others
            }
            // Search in ID
            if (JECharactersIntegration.match(id.toString(), lowerCaseKeyword)) {
                matches.add(name);
                return;
            }
            // Search in tags
            Set<String> associatedTags = this.resourceToTagsMap.get(id);
            if (associatedTags != null) {
                for (String tagName : associatedTags) {
                    if (JECharactersIntegration.match(tagName, lowerCaseKeyword)) {
                        matches.add(name);
                        return;
                    }
                }
            }
        });

        return new ArrayList<>(matches);
    }

    public void handleTabCompletion() {
        String keyword = box.getValue();
        if (keyword.length() < 2 || !(keyword.startsWith("#") || keyword.startsWith("@") || keyword.startsWith("$") || keyword.startsWith("~"))) {
            return;
        }

        char prefix = keyword.charAt(0);
        String term = keyword.substring(1).toLowerCase();
        Map<String, Set<ResourceLocation>> lookupMap;
        switch(prefix) {
            case '#':
                lookupMap = this.tags;
                break;
            case '@':
                lookupMap = this.modIds;
                break;
            case '$':
                lookupMap = this.sets;
                break;
            case '~':
                lookupMap = this.types;
                break;
            default:
                return;
        }

        if (lookupMap == null) {
            return;
        }

        List<String> potentialCompletions = lookupMap.keySet().stream()
                .filter(key -> key.toLowerCase().startsWith(term))
                .collect(Collectors.toList());

        if (potentialCompletions.isEmpty()) {
            return;
        }

        if (potentialCompletions.size() == 1) {
            box.setValue(prefix + potentialCompletions.get(0));
        } else {
            String commonPrefix = StringUtils.getCommonPrefix(potentialCompletions.toArray(new String[0]));
            if (!commonPrefix.isEmpty()) {
                box.setValue(prefix + commonPrefix);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class Entry extends ObjectSelectionList.Entry<Entry> {
        private final String value;

        public Entry(String value) {
            this.value = value;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            guiGraphics.drawString(Minecraft.getInstance().font, this.value, left + 5, top + 2, 16777215, true);

            ResourceLocation resourceKey = ResourceList.this.map.inverse().get(this.value);
            if (resourceKey != null) {
                int yOffset = top + 12;
                int availableWidth = width - 10;

                // Render Type
                if (ResourceList.this.structureToTypeMap != null) {
                    ResourceLocation typeKey = ResourceList.this.structureToTypeMap.get(resourceKey);
                    if (typeKey != null && !typeKey.getPath().equals("none")) {
                        Component typeComponent = Component.literal("Type: " + typeKey.toString()).withStyle(ChatFormatting.DARK_GREEN);
                        guiGraphics.drawString(minecraft.font, typeComponent, left + 5, yOffset, 0xAAAAAA, true);
                        yOffset += 10;
                    }
                }

                // Render Dimensions
                if (ResourceList.this.dimensions != null) {
                    List<ResourceLocation> dims = ResourceList.this.dimensions.get(resourceKey);
                    if (dims != null && !dims.isEmpty()) {
                        String dimString = dims.stream()
                                .map(TextUtil::getDimensionName)
                                .collect(Collectors.joining(", "));
                        Component dimComponent = Component.literal("Dims: " + dimString).withStyle(ChatFormatting.GRAY);
                        if (minecraft.font.width(dimComponent) > availableWidth) {
                            dimString = "Dims: " + minecraft.font.plainSubstrByWidth(dimString, availableWidth - minecraft.font.width("Dims: ..."));
                            dimComponent = Component.literal(dimString + "...").withStyle(ChatFormatting.GRAY);
                        }
                        guiGraphics.drawString(minecraft.font, dimComponent, left + 5, yOffset, 0xAAAAAA, true);
                    }
                }
            }
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.translatable("narrator.select");
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                ResourceList.this.setSelected(this);
                return true;
            } else {
                return false;
            }
        }
    }
}
