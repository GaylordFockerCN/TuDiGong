package com.p1nero.tudigong.network.packet.client;

import com.p1nero.dialog_lib.network.packet.BasePacket;
import com.p1nero.tudigong.compat.XaeroMapCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record AddXaeroMapWaypointPacket(String name, BlockPos pos, @Nullable String color) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeBlockPos(pos);
        buf.writeUtf(Objects.requireNonNullElse(color, "null"));
    }

    public static AddXaeroMapWaypointPacket decode(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        BlockPos blockPos = buf.readBlockPos();
        String color = buf.readUtf();
        return new AddXaeroMapWaypointPacket(name, blockPos, color);
    }

    @Override
    public void execute(@Nullable Player playerEntity) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
            XaeroMapCompat.createWaypoint(pos, name, color);
        }
    }
}
