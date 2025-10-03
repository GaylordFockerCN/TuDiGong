package com.p1nero.tudigong.client.screen;

import com.p1nero.tudigong.client.util.SearchHistoryManager;
import com.p1nero.tudigong.client.widget.HistoryList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HistoryScreen extends Screen {

    private HistoryList historyList;
    private final Screen parentScreen;
    private EditBox searchBox;

    public HistoryScreen(Screen parentScreen) {
        super(Component.translatable("gui.tudigong.history.title"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();

        int listWidth = 320;
        int listLeft = (this.width - listWidth) / 2;
        int topMargin = 32;
        int bottomMargin = 32;

        this.searchBox = new EditBox(this.font, listLeft, topMargin, listWidth, 20, Component.translatable("gui.tudigong.history.search_placeholder"));
        this.addRenderableWidget(this.searchBox);

        int listY = topMargin + 24;
        this.historyList = new HistoryList(this.minecraft, listWidth, this.height, listY, this.height - bottomMargin);
        this.historyList.setLeftPos(listLeft);
        this.addRenderableWidget(this.historyList);

        this.searchBox.setResponder(this.historyList::filter);
        this.historyList.filter(null);

        int buttonWidth = 100;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.tudigong.history.clear"), (button) -> {
            SearchHistoryManager.clearHistory();
            this.searchBox.setValue("");
            this.historyList.filter(null);
        }).bounds((this.width / 2) - buttonWidth - 5, this.height - 28, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), (button) -> this.onClose())
                .bounds((this.width / 2) + 5, this.height - 28, buttonWidth, 20).build());


        this.setInitialFocus(this.searchBox);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderDirtBackground(guiGraphics);
        this.historyList.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchBox.isFocused() && this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parentScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
