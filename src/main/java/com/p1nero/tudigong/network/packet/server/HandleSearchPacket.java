package com.p1nero.tudigong.network.packet.server;

import com.p1nero.dialog_lib.network.packet.BasePacket;
import com.p1nero.tudigong.entity.TudiGongEntity;
import com.p1nero.tudigong.util.StructureTagManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record HandleSearchPacket(int entityID, String searchString, boolean isStructure) implements BasePacket {

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityID());
        buf.writeUtf(this.searchString());
        buf.writeBoolean(isStructure);
    }

    public static HandleSearchPacket decode(FriendlyByteBuf buf) {
        return new HandleSearchPacket(buf.readInt(), buf.readUtf(), buf.readBoolean());
    }

    public void execute(@Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            Entity entity = player.level().getEntity(this.entityID());
            if (entity instanceof TudiGongEntity tudiGongEntity) {
                if (isStructure && searchString.startsWith("#")) {
                    String tagName = searchString.substring(1);
                    Optional<ResourceLocation> structureOpt = StructureTagManager.getRandomStructureForTag(tagName);
                    if (structureOpt.isPresent()) {
                        tudiGongEntity.handleSearch(serverPlayer, structureOpt.get(), true);
                    } else {
                        player.sendSystemMessage(Component.literal("Tag not found or is empty: " + tagName));
                    }
                } else {
                    try {
                        @SuppressWarnings("removal") ResourceLocation resourceLocation = new ResourceLocation(searchString);
                        tudiGongEntity.handleSearch(serverPlayer, resourceLocation, isStructure);
                    } catch (Exception e) {
                        player.sendSystemMessage(Component.literal("Invalid resource location: " + searchString));
                    }
                }
            }
        }
    }
}
