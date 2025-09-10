package com.p1nero.tudigong.network.packet.server;

import com.p1nero.dialog_lib.network.packet.BasePacket;
import com.p1nero.tudigong.entity.TudiGongEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record HandleSearchPacket(int entityID, ResourceLocation resourceLocation, boolean isStructure) implements BasePacket {

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityID());
        buf.writeResourceLocation(this.resourceLocation());
        buf.writeBoolean(isStructure);
    }

    public static HandleSearchPacket decode(FriendlyByteBuf buf) {
        return new HandleSearchPacket(buf.readInt(), buf.readResourceLocation(), buf.readBoolean());
    }

    public void execute(@Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            Entity entity = player.level().getEntity(this.entityID());
            if (entity instanceof TudiGongEntity tudiGongEntity) {
                tudiGongEntity.handleSearch(serverPlayer, resourceLocation, isStructure);
            }
        }

    }
}
