package com.p1nero.tudigong.network;

import com.p1nero.dialog_lib.network.packet.BasePacket;
import com.p1nero.tudigong.TuDiGongMod;
import com.p1nero.tudigong.network.packet.client.AddJourneyMapWaypointPacket;
import com.p1nero.tudigong.network.packet.client.AddXaeroMapWaypointPacket;
import com.p1nero.tudigong.network.packet.client.SyncHistoryEntryPacket;
import com.p1nero.tudigong.network.packet.client.SyncResourceKeysPacket;
import com.p1nero.tudigong.network.packet.client.SyncStructureTagsPacket;
import com.p1nero.tudigong.network.packet.server.HandleSearchPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;

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
        register(SyncHistoryEntryPacket.class, SyncHistoryEntryPacket::decode);

        register(HandleSearchPacket.class, HandleSearchPacket::decode);
    }

    private static <MSG extends BasePacket> void register(final Class<MSG> packet, Function<FriendlyByteBuf, MSG> decoder) {
        INSTANCE.messageBuilder(packet, index++).encoder(BasePacket::encode).decoder(decoder).consumerMainThread(BasePacket::handle).add();
    }
}
