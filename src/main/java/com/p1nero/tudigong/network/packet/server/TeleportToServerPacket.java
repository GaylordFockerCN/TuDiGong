package com.p1nero.tudigong.network.packet.server;

import com.p1nero.dialog_lib.network.packet.BasePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record TeleportToServerPacket(BlockPos position, ResourceKey<Level> dimension) implements BasePacket {

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(position);
        buf.writeResourceKey(dimension);
    }

    public static TeleportToServerPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        ResourceKey<Level> dim = buf.readResourceKey(Registries.DIMENSION);
        return new TeleportToServerPacket(pos, dim);
    }

    @Override
    public void execute(@Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerLevel targetLevel = serverPlayer.server.getLevel(this.dimension);
            if (targetLevel != null) {
                double y = this.position.getY();
                if (y == -1145) {
                    y = serverPlayer.getY(); // Use player's current Y level as a fallback
                }
                serverPlayer.teleportTo(targetLevel, this.position.getX() + 0.5, y, this.position.getZ() + 0.5, serverPlayer.getYRot(), serverPlayer.getXRot());
            }
        }
    }
}
