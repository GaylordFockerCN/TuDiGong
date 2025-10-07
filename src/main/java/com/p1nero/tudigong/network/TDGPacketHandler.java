package com.p1nero.tudigong.network;

import com.p1nero.dialog_lib.network.packet.BasePacket;
import com.p1nero.tudigong.TuDiGongMod;
import com.p1nero.tudigong.client.screen.StructureSearchScreen;
import com.p1nero.tudigong.network.packet.client.*;
import com.p1nero.tudigong.network.packet.server.HandleSearchPacket;
import com.p1nero.tudigong.network.packet.server.TeleportToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TDGPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(TuDiGongMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals
    );

    private static int index;

    public static synchronized void register() {
        register(AddXaeroMapWaypointPacket.class, AddXaeroMapWaypointPacket::decode);
        register(AddJourneyMapWaypointPacket.class, AddJourneyMapWaypointPacket::decode);
        register(SyncResourceKeysPacket.class, SyncResourceKeysPacket::decode);
        register(SyncStructureTagsPacket.class, SyncStructureTagsPacket::decode);
        register(SyncStructureSetsPacket.class, SyncStructureSetsPacket::decode);
        register(SyncStructureDimensionsPacket.class, SyncStructureDimensionsPacket::decode);
        register(SyncBiomeDimensionsPacket.class, SyncBiomeDimensionsPacket::decode);
        register(SyncHistoryEntryPacket.class, SyncHistoryEntryPacket::decode);

        // Special handling for SyncStructureTypesPacket to decouple it from a specific screen
        INSTANCE.messageBuilder(SyncStructureTypesPacket.class, index++)
                .encoder(SyncStructureTypesPacket::encode)
                .decoder(SyncStructureTypesPacket::decode)
                .consumerMainThread((packet, ctx) -> packet.handle(ctx, (types, structureToTypeMap) -> {
                    StructureSearchScreen.STRUCTURE_TYPES.clear();
                    StructureSearchScreen.STRUCTURE_TYPES.putAll(types);
                    StructureSearchScreen.STRUCTURE_TO_TYPE_MAP.clear();
                    StructureSearchScreen.STRUCTURE_TO_TYPE_MAP.putAll(structureToTypeMap);
                }))
                .add();

        register(HandleSearchPacket.class, HandleSearchPacket::decode);
        register(TeleportToServerPacket.class, TeleportToServerPacket::decode);
    }

    private static <MSG extends BasePacket> void register(final Class<MSG> packet, Function<FriendlyByteBuf, MSG> decoder) {
        INSTANCE.messageBuilder(packet, index++).encoder(BasePacket::encode).decoder(decoder).consumerMainThread(BasePacket::handle).add();
    }
}
