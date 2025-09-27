package com.p1nero.tudigong.client.widget;

import com.google.common.collect.BiMap;
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
import java.util.Set;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class ResourceList extends ObjectSelectionList<ResourceList.Entry> {

    private final BiMap<ResourceLocation, String> map;
    private final Set<String> names;
    private final Set<ResourceLocation> ids;
    private EditBox box;

    public ResourceList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight, BiMap<ResourceLocation, String> map, EditBox box) {
        super(minecraft, width, height, y0, y1, itemHeight);
        this.setRenderBackground(false);//泥土丑甚
        this.names = map.values();
        this.ids = map.keySet();
        this.map = map;
        this.box = box;
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

        // First, try to match by display name (which can be Chinese)
        java.util.List<Entry> nameMatches = this.names.stream()
                .filter(name -> com.p1nero.tudigong.compat.JECharactersIntegration.match(name, keyword))
                .sorted()
                .map(Entry::new)
                .toList();

        if (!nameMatches.isEmpty()) {
            nameMatches.forEach(this::addEntry);
        } else {
            // If no name matches, try to match by resource location
            this.ids.stream()
                    .filter(id -> com.p1nero.tudigong.compat.JECharactersIntegration.match(id.toString(), keyword))
                    .map(this.map::get) // Convert ResourceLocation back to display name
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