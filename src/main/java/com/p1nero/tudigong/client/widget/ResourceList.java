package com.p1nero.tudigong.client.widget;

import com.google.common.collect.BiMap;
import com.p1nero.tudigong.compat.JECharactersIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class ResourceList extends ObjectSelectionList<ResourceList.Entry> {

    private final BiMap<ResourceLocation, String> map;
    private final Set<String> names;
    private final Set<ResourceLocation> ids;
    private final Map<String, Set<ResourceLocation>> tags;
    private final Map<String, Set<ResourceLocation>> modIds;
    private EditBox box;

    public ResourceList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight, BiMap<ResourceLocation, String> map, EditBox box, Map<String, Set<ResourceLocation>> tags, Map<String, Set<ResourceLocation>> modIds) {
        super(minecraft, width, height, y0, y1, itemHeight);
        this.setRenderBackground(false);//泥土丑甚
        this.names = map.values();
        this.ids = map.keySet();
        this.map = map;
        this.box = box;
        this.tags = tags;
        this.modIds = modIds;
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
        box.setValue(entry.value);
    }

    /**
     * 支持两种搜索，用id或者名字
     */
    public void refresh(String keyword) {
        this.setScrollAmount(0.0D);
        this.children().clear();

        if (StringUtil.isNullOrEmpty(keyword)) {
            this.names.stream().sorted().map(Entry::new).forEach(this::addEntry);
            return;
        }

        if (keyword.length() > 1 && (keyword.startsWith("#") || keyword.startsWith("@"))) {
            char prefix = keyword.charAt(0);
            String term = keyword.substring(1).toLowerCase();
            Map<String, Set<ResourceLocation>> lookupMap = (prefix == '#') ? this.tags : this.modIds;

            if (lookupMap != null && !term.isEmpty() && lookupMap.containsKey(term)) {
                Set<ResourceLocation> matches = lookupMap.get(term);
                this.ids.stream()
                        .filter(matches::contains)
                        .map(this.map::get)
                        .filter(java.util.Objects::nonNull)
                        .sorted()
                        .map(Entry::new)
                        .forEach(this::addEntry);
                return;
            }
        }

        // Fallback to default search
        java.util.List<Entry> nameMatches = this.names.stream()
                .filter(name -> JECharactersIntegration.match(name, keyword))
                .sorted()
                .map(Entry::new)
                .toList();

        if (!nameMatches.isEmpty()) {
            nameMatches.forEach(this::addEntry);
        } else {
            this.ids.stream()
                    .filter(id -> JECharactersIntegration.match(id.toString(), keyword))
                    .map(this.map::get)
                    .filter(java.util.Objects::nonNull)
                    .sorted()
                    .map(Entry::new)
                    .forEach(this::addEntry);
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
            guiGraphics.drawString(Minecraft.getInstance().font, this.value, left + 5, top + 5, 16777215, true);
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