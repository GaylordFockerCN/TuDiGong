package com.p1nero.tudigong.network.packet.client;

import com.p1nero.dialog_lib.network.packet.BasePacket;
import com.p1nero.tudigong.client.screen.BiomeSearchScreen;
import com.p1nero.tudigong.client.screen.StructureSearchScreen;
import com.p1nero.tudigong.util.WorldUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record SyncResourceKeysPacket(List<ResourceLocation> resourceLocations, boolean isStructure) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(isStructure);
        buf.writeInt(resourceLocations.size());
        resourceLocations.forEach(buf::writeResourceLocation);
    }

    public static SyncResourceKeysPacket decode(FriendlyByteBuf buf) {
        boolean isStructure = buf.readBoolean();
        int size = buf.readInt();
        List<ResourceLocation> newResourceLocations = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            newResourceLocations.add(buf.readResourceLocation());
        }
        return new SyncResourceKeysPacket(newResourceLocations, isStructure);
    }

    @Override
    public void execute(@Nullable Player playerEntity) {
        if(Minecraft.getInstance().level != null) {
            if(isStructure) {
                StructureSearchScreen.STRUCTURE_NAME_MAP.clear();
                StructureSearchScreen.STRUCTURE_MOD_IDS.clear();
                resourceLocations.forEach((resourceLocation -> {
                    StructureSearchScreen.STRUCTURE_NAME_MAP.put(resourceLocation, WorldUtil.getStructureName(resourceLocation));
                    String modId = resourceLocation.getNamespace().toLowerCase();
                    StructureSearchScreen.STRUCTURE_MOD_IDS.computeIfAbsent(modId, k -> new java.util.HashSet<>()).add(resourceLocation);
                }));
            } else {
                BiomeSearchScreen.BIOME_NAME_MAP.clear();
                BiomeSearchScreen.BIOME_MOD_IDS.clear();
                resourceLocations.forEach((resourceLocation -> {
                    BiomeSearchScreen.BIOME_NAME_MAP.put(resourceLocation, WorldUtil.getBiomeName(resourceLocation));
                    String modId = resourceLocation.getNamespace().toLowerCase();
                    BiomeSearchScreen.BIOME_MOD_IDS.computeIfAbsent(modId, k -> new java.util.HashSet<>()).add(resourceLocation);
                }));
            }
        }
    }
}
