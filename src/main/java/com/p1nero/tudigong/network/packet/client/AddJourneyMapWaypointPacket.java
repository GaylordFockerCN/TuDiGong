package com.p1nero.tudigong.network.packet.client;

import com.p1nero.dialog_lib.network.packet.BasePacket;
import com.p1nero.tudigong.compat.JourneyMapCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record AddJourneyMapWaypointPacket(String name, BlockPos pos, int color) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeBlockPos(pos);
        buf.writeInt(color);
    }

    public static AddJourneyMapWaypointPacket decode(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        BlockPos blockPos = buf.readBlockPos();
        int color = buf.readInt();
        return new AddJourneyMapWaypointPacket(name, blockPos, color);
    }

    @Override
    public void execute(@Nullable Player playerEntity) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
            JourneyMapCompat.createNewWaypoint(name, color, pos, Minecraft.getInstance().level.dimension());
        }
    }
}
