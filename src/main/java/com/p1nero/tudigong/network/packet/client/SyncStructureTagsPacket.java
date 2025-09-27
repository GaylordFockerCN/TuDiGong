package com.p1nero.tudigong.network.packet.client;

import com.p1nero.dialog_lib.network.packet.BasePacket;
import com.p1nero.tudigong.client.screen.StructureSearchScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record SyncStructureTagsPacket(Map<String, Set<ResourceLocation>> tags) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(tags, FriendlyByteBuf::writeUtf, (byteBuf, resourceLocations) -> byteBuf.writeCollection(resourceLocations, FriendlyByteBuf::writeResourceLocation));
    }

    public static SyncStructureTagsPacket decode(FriendlyByteBuf buf) {
        Map<String, Set<ResourceLocation>> tags = buf.readMap(FriendlyByteBuf::readUtf, byteBuf -> byteBuf.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
        return new SyncStructureTagsPacket(tags);
    }

    @Override
    public void execute(@Nullable Player playerEntity) {
        StructureSearchScreen.STRUCTURE_TAGS.clear();
        StructureSearchScreen.STRUCTURE_TAGS.putAll(tags);
    }
}
