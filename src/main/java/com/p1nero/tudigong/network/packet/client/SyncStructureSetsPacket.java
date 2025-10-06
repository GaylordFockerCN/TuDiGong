package com.p1nero.tudigong.network.packet.client;

import com.p1nero.dialog_lib.network.packet.BasePacket;
import com.p1nero.tudigong.client.screen.StructureSearchScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record SyncStructureSetsPacket(Map<String, Set<ResourceLocation>> sets) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(sets, FriendlyByteBuf::writeUtf, (byteBuf, resourceLocations) -> byteBuf.writeCollection(resourceLocations, FriendlyByteBuf::writeResourceLocation));
    }

    public static SyncStructureSetsPacket decode(FriendlyByteBuf buf) {
        Map<String, Set<ResourceLocation>> sets = buf.readMap(FriendlyByteBuf::readUtf, byteBuf -> byteBuf.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
        return new SyncStructureSetsPacket(sets);
    }

    @Override
    public void execute(@Nullable Player playerEntity) {
        StructureSearchScreen.STRUCTURE_SETS.clear();
        StructureSearchScreen.STRUCTURE_SETS.putAll(sets);
    }
}
