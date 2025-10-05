package com.p1nero.tudigong.client.widget;

import com.p1nero.tudigong.client.util.SearchHistoryManager;
import com.p1nero.tudigong.client.util.SearchHistoryManager.SearchHistoryEntry;
import com.p1nero.tudigong.compat.JECharactersIntegration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

public class HistoryList extends ObjectSelectionList<HistoryList.Entry> {

    private static final ResourceLocation HISTORY_ENTRY_TEXTURE = ResourceLocation.fromNamespaceAndPath("tudigong", "textures/gui/history_entry.png");

    public HistoryList(Minecraft minecraft, int width, int height, int y0, int y1) {
        super(minecraft, width, height, y0, y1, 40);
        this.setRenderBackground(true);
    }

    public void filter(String keyword) {
        this.clearEntries();
        Stream<SearchHistoryEntry> stream = SearchHistoryManager.getHistory().stream();

        if (!StringUtil.isNullOrEmpty(keyword)) {
            String lowerCaseKeyword = keyword.toLowerCase();
            stream = stream.filter(entry -> {
                if (JECharactersIntegration.match(entry.searchTerm(), lowerCaseKeyword)) return true;
                if (JECharactersIntegration.match(entry.type().getString(), lowerCaseKeyword)) return true;
                if (entry.position() != null) {
                    String posString = entry.position().getX() + ", " + entry.position().getY() + ", " + entry.position().getZ();
                    if (posString.contains(lowerCaseKeyword)) return true;
                }
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(entry.timestamp()));
                return timestamp.contains(lowerCaseKeyword);
            });
        }

        stream.forEach(entry -> this.addEntry(new Entry(this, entry)));
        this.setScrollAmount(0);
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x1 - 6;
    }

    public static class Entry extends ObjectSelectionList.Entry<HistoryList.Entry> {
        private final HistoryList parentList;
        private final SearchHistoryEntry historyEntry;
        private final Button deleteButton;
        private final Minecraft minecraft;
        private final ItemStack icon;

        public Entry(HistoryList parentList, SearchHistoryEntry historyEntry) {
            this.parentList = parentList;
            this.historyEntry = historyEntry;
            this.minecraft = Minecraft.getInstance();

            this.deleteButton = Button.builder(Component.literal("X").withStyle(ChatFormatting.RED), (button) -> {
                SearchHistoryManager.remove(historyEntry);
                this.parentList.filter(null); // Refresh the list
            }).bounds(0, 0, 20, 20).build();

            boolean isStructure = historyEntry.type().getString().equalsIgnoreCase(Component.translatable("history.tudigong.type.structure").getString());
            this.icon = new ItemStack(isStructure ? Items.COMPASS : Items.GRASS_BLOCK);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            // Render entry background
//            guiGraphics.blit(HISTORY_ENTRY_TEXTURE, left, top, 0, isMouseOver ? height : 0, width, height, width, height * 2);

            // Render Icon
            guiGraphics.renderFakeItem(this.icon, left + 8, top + 12);

            // Render Search Term
            Component mainText = Component.literal(historyEntry.searchTerm()).withStyle(ChatFormatting.WHITE);
            guiGraphics.drawString(minecraft.font, mainText, left + 35, top + 7, 0xFFFFFF, true);

            // Render Position
            if (historyEntry.position() != null) {
                BlockPos pos = historyEntry.position();
                String posString = pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
                MutableComponent posComponent = Component.literal(posString)
                        .withStyle(style -> style.withColor(ChatFormatting.GREEN)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip"))));
                guiGraphics.drawString(minecraft.font, posComponent, left + 35, top + 20, 0xFFFFFF);
            } else {
                guiGraphics.drawString(minecraft.font, Component.translatable("gui.tudigong.history.pos_na").withStyle(ChatFormatting.DARK_GRAY), left + 35, top + 20, 0xFFFFFF);
            }

            // Render Timestamp
            String timestamp = new SimpleDateFormat("yy/MM/dd HH:mm").format(new Date(historyEntry.timestamp()));
            int timestampWidth = this.minecraft.font.width(timestamp);
            guiGraphics.drawString(this.minecraft.font, timestamp, left + width - timestampWidth - 30, top + 7, 0xAAAAAA);

            // Render Delete Button
            this.deleteButton.setX(left + width - this.deleteButton.getWidth() - 5);
            this.deleteButton.setY(top + (height - this.deleteButton.getHeight()) / 2);
            this.deleteButton.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.deleteButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(historyEntry.searchTerm());
        }
    }
}
