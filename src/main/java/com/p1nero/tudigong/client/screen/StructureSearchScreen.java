package com.p1nero.tudigong.client.screen;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.p1nero.dialog_lib.network.DialoguePacketRelay;
import com.p1nero.dialog_lib.network.packet.serverbound.HandleNpcEntityPlayerInteractPacket;
import com.p1nero.tudigong.client.widget.ResourceList;
import com.p1nero.tudigong.network.TDGPacketHandler;
import com.p1nero.tudigong.network.packet.server.HandleSearchPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class StructureSearchScreen extends Screen {
    private int tudigongId;
    private EditBox searchBox;
    private ResourceList resourceList;
    private Button searchButton;
    private boolean found;
    public static final BiMap<ResourceLocation, String> STRUCTURE_NAME_MAP = HashBiMap.create();

    public StructureSearchScreen(int tudigongId) {
        super(Component.literal(""));
        this.tudigongId = tudigongId;
    }

    @Override
    protected void init() {
        super.init();
        
        // 计算输入框和按钮的位置（屏幕中央）
        int inputBoxWidth = 200;
        int buttonWidth = 60;
        int totalWidth = inputBoxWidth + buttonWidth + 5; // 5像素间距
        int leftPos = (this.width - totalWidth) / 2;
        int topPos = 50;

        // 创建输入框
        this.searchBox = new EditBox(this.font, leftPos, topPos, inputBoxWidth, 20, Component.literal("Structure Resource Location"));
        this.searchBox.setMaxLength(32500);
        this.searchBox.setValue("");
        this.resourceList = new ResourceList(Minecraft.getInstance(), inputBoxWidth, this.height, topPos + 23, this.height - 50, 21, STRUCTURE_NAME_MAP, searchBox);
        this.resourceList.setRenderTopAndBottom(false);
        this.resourceList.setLeftPos(leftPos);
        this.searchBox.setResponder(this.resourceList::refresh);
        this.resourceList.refresh(null);
        this.addRenderableWidget(this.searchBox);
        this.addRenderableWidget(this.resourceList);
        
        // 创建搜索按钮
        this.searchButton = Button.builder(Component.translatable("button.tudigong.ask"), this::onSearchButtonPressed)
                .bounds(leftPos + inputBoxWidth + 5, topPos, buttonWidth, 20)
                .build();
        this.addRenderableWidget(this.searchButton);
        
        // 设置初始焦点
        this.setInitialFocus(this.searchBox);
    }

    private void onSearchButtonPressed(Button button) {
        button.playDownSound(Minecraft.getInstance().getSoundManager());
        String inputText = this.searchBox.getValue().trim();

        if (inputText.isEmpty()) {
            return;
        }

        if (!STRUCTURE_NAME_MAP.containsValue(inputText)) {
            return;
        }

        if(ResourceLocation.isValidResourceLocation(inputText)){
            ResourceLocation inputResourcelocation = ResourceLocation.parse(inputText);
            if(STRUCTURE_NAME_MAP.containsKey(inputResourcelocation)) {
                onStructureFound(inputResourcelocation);
                return;
            }
        }

        ResourceLocation resourceLocation = STRUCTURE_NAME_MAP.inverse().get(inputText);
        onStructureFound(resourceLocation);
    }

    @Override
    public void onClose() {
        super.onClose();
        if(!found) {
            DialoguePacketRelay.sendToServer(new HandleNpcEntityPlayerInteractPacket(tudigongId, 0));
        }
    }

    private void onStructureFound(ResourceLocation structure) {
        DialoguePacketRelay.sendToServer(TDGPacketHandler.INSTANCE, new HandleSearchPacket(tudigongId, structure, true));
        found = true;
        this.onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 按下回车时执行搜索
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            this.onSearchButtonPressed(this.searchButton);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public int getTudigongId() {
        return tudigongId;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}