package com.p1nero.tudigong.network.packet.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SyncStructureTypesPacket {

    private final Map<String, Set<ResourceLocation>> types;
    private final Map<ResourceLocation, ResourceLocation> structureToTypeMap;

    public SyncStructureTypesPacket(Map<ResourceLocation, Collection<ResourceLocation>> types, Map<ResourceLocation, ResourceLocation> structureToTypeMap) {
        this.types = new HashMap<>();
        types.forEach((key, value) -> this.types.put(key.toString(), new HashSet<>(value)));
        this.structureToTypeMap = structureToTypeMap;
    }

    private SyncStructureTypesPacket(FriendlyByteBuf buf) {
        this.types = buf.readMap(FriendlyByteBuf::readUtf,
                (b) -> b.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
        this.structureToTypeMap = buf.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readResourceLocation);
    }

    public static SyncStructureTypesPacket decode(FriendlyByteBuf buf) {
        return new SyncStructureTypesPacket(buf);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(this.types, FriendlyByteBuf::writeUtf,
                (b, v) -> b.writeCollection(v, FriendlyByteBuf::writeResourceLocation));
        buf.writeMap(this.structureToTypeMap, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeResourceLocation);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx, BiConsumer<Map<String, Set<ResourceLocation>>, Map<ResourceLocation, ResourceLocation>> consumer) {
        ctx.get().enqueueWork(() -> consumer.accept(this.types, this.structureToTypeMap));
        ctx.get().setPacketHandled(true);
    }
}
