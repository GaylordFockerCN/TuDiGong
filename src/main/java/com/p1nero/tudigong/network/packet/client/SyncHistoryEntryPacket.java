package com.p1nero.tudigong.network.packet.client;

import com.p1nero.dialog_lib.network.packet.BasePacket;
import com.p1nero.tudigong.client.util.SearchHistoryManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Packet sent from the server to the client to add a new entry to the search history.
 */
public record SyncHistoryEntryPacket(String searchTerm, Component type, @Nullable BlockPos position) implements BasePacket {

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.searchTerm);
        buf.writeComponent(this.type);
        buf.writeNullable(this.position, FriendlyByteBuf::writeBlockPos);
    }

    public static SyncHistoryEntryPacket decode(FriendlyByteBuf buf) {
        String searchTerm = buf.readUtf();
        Component type = buf.readComponent();
        BlockPos position = buf.readNullable(FriendlyByteBuf::readBlockPos);
        return new SyncHistoryEntryPacket(searchTerm, type, position);
    }

    /**
     * Executed on the client side to add the received entry to the history manager.
     */
    @Override
    public void execute(@Nullable Player player) {
        SearchHistoryManager.addEntry(
                new SearchHistoryManager.SearchHistoryEntry(
                        this.searchTerm,
                        this.type,
                        this.position,
                        System.currentTimeMillis()
                )
        );
    }
}
