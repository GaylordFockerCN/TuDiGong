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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class BiomeSearchScreen extends Screen {
    private int tudigongId;
    private EditBox searchBox;
    private ResourceList resourceList;
    private Button searchButton;
    private boolean found;
    public static final BiMap<ResourceLocation, String> BIOME_NAME_MAP = HashBiMap.create();
    public static final Map<String, Set<ResourceLocation>> BIOME_MOD_IDS = new HashMap<>();

    public BiomeSearchScreen(int tudigongId) {
        super(Component.literal(""));
        this.tudigongId = tudigongId;
    }

    @Override
    protected void init() {
        super.init();

        int inputBoxWidth = 200;
        int buttonWidth = 60;
        int totalWidth = inputBoxWidth + buttonWidth + 5;
        int leftPos = (this.width - totalWidth) / 2;
        int topPos = 50;

        this.searchBox = new EditBox(this.font, leftPos, topPos, inputBoxWidth, 20, Component.literal("Biome Resource Location"));
        this.searchBox.setMaxLength(32500);
        this.searchBox.setValue("");
        this.resourceList = new ResourceList(Minecraft.getInstance(), inputBoxWidth, this.height, topPos + 23, this.height - 50, 21, BIOME_NAME_MAP, searchBox, null, BIOME_MOD_IDS);
        this.resourceList.setRenderTopAndBottom(false);
        this.resourceList.setLeftPos(leftPos);
        this.searchBox.setResponder(this.resourceList::refresh);
        this.resourceList.refresh(null);
        this.addRenderableWidget(this.searchBox);
        this.addRenderableWidget(this.resourceList);

        this.searchButton = Button.builder(Component.translatable("button.tudigong.ask"), this::onSearchButtonPressed)
                .bounds(leftPos + inputBoxWidth + 5, topPos, buttonWidth, 20)
                .build();
        this.addRenderableWidget(this.searchButton);

        this.setInitialFocus(this.searchBox);
    }

    private void onSearchButtonPressed(Button button) {
        button.playDownSound(Minecraft.getInstance().getSoundManager());
        String searchString = this.searchBox.getValue().trim();

        if (searchString.isEmpty()) {
            return;
        }

        String searchToSend;
        // If the input is a display name, convert it to a ResourceLocation string
        if (BIOME_NAME_MAP.containsValue(searchString)) {
            searchToSend = BIOME_NAME_MAP.inverse().get(searchString).toString();
        } else {
            // Otherwise, send the raw string (could be a direct resource location)
            searchToSend = searchString;
        }

        DialoguePacketRelay.sendToServer(TDGPacketHandler.INSTANCE, new HandleSearchPacket(tudigongId, searchToSend, false));
        found = true;
        this.onClose();
    }

    @Override
    public void onClose() {
        super.onClose();
        if (!found) {
            DialoguePacketRelay.sendToServer(new HandleNpcEntityPlayerInteractPacket(tudigongId, 0));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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