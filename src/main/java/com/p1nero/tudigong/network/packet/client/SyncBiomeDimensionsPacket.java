package com.p1nero.tudigong.network.packet.client;

import com.p1nero.dialog_lib.network.packet.BasePacket;
import com.p1nero.tudigong.client.screen.BiomeSearchScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record SyncBiomeDimensionsPacket(Map<ResourceLocation, List<ResourceLocation>> dimensions) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(dimensions, FriendlyByteBuf::writeResourceLocation, (byteBuf, list) -> byteBuf.writeCollection(list, FriendlyByteBuf::writeResourceLocation));
    }

    public static SyncBiomeDimensionsPacket decode(FriendlyByteBuf buf) {
        return new SyncBiomeDimensionsPacket(buf.readMap(FriendlyByteBuf::readResourceLocation, byteBuf -> byteBuf.readList(FriendlyByteBuf::readResourceLocation)));
    }

    @Override
    public void execute(@Nullable Player playerEntity) {
        BiomeSearchScreen.BIOME_DIMENSIONS.clear();
        BiomeSearchScreen.BIOME_DIMENSIONS.putAll(dimensions);
    }
}
