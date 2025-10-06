package com.p1nero.tudigong.network.packet.server;

import com.p1nero.dialog_lib.network.packet.BasePacket;
import com.p1nero.tudigong.entity.TudiGongEntity;
import com.p1nero.tudigong.util.StructureTagManager;
import com.p1nero.tudigong.util.StructureUtils;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record HandleSearchPacket(int entityID, String searchString, boolean isStructure) implements BasePacket {

    private static final String TAG_PREFIX = "#";
    private static final String SET_PREFIX = "$";

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityID());
        buf.writeUtf(this.searchString());
        buf.writeBoolean(isStructure);
    }

    public static HandleSearchPacket decode(FriendlyByteBuf buf) {
        return new HandleSearchPacket(buf.readInt(), buf.readUtf(), buf.readBoolean());
    }

    @Override
    public void execute(@Nullable Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        Entity entity = serverPlayer.level().getEntity(this.entityID());
        if (!(entity instanceof TudiGongEntity tudiGongEntity)) {
            return;
        }

        // Pass the original search term to the handler
        resolveResourceLocation(serverPlayer).ifPresent(resourceLocation ->
                tudiGongEntity.handleSearch(serverPlayer, this.searchString, resourceLocation, isStructure)
        );
    }

    private Optional<ResourceLocation> resolveResourceLocation(ServerPlayer player) {
        if (isStructure) {
            if (searchString.startsWith(TAG_PREFIX)) {
                return handleTagSearch(player);
            } else if (searchString.startsWith(SET_PREFIX)) {
                return handleStructureSetSearch(player);
            }
        }
        return handleDirectSearch(player);
    }

    private Optional<ResourceLocation> handleTagSearch(ServerPlayer player) {
        String tagName = searchString.substring(TAG_PREFIX.length());
        Optional<ResourceLocation> structureOpt = StructureTagManager.getRandomStructureForTag(tagName);
        if (structureOpt.isEmpty()) {
            player.sendSystemMessage(Component.translatable("error.tudigong.tag_not_found", tagName));
        }
        return structureOpt;
    }

    private Optional<ResourceLocation> handleStructureSetSearch(ServerPlayer player) {
        String setName = searchString.substring(SET_PREFIX.length());
        Optional<ResourceLocation> structureOpt = StructureUtils.getRandomStructureForSet(player.serverLevel(), setName);
        if (structureOpt.isEmpty()) {
            player.sendSystemMessage(Component.translatable("error.tudigong.set_not_found", setName));
        }
        return structureOpt;
    }

    private Optional<ResourceLocation> handleDirectSearch(ServerPlayer player) {
        try {
            // Try to parse as a direct ResourceLocation first.
            @SuppressWarnings("removal")
            ResourceLocation resourceLocation = new ResourceLocation(searchString);
            return Optional.of(resourceLocation);
        } catch (ResourceLocationException e) {
            // If that fails, try our fallback search by structure type name.
            return StructureUtils.findStructureByTypeName(player.server.registryAccess(), searchString)
                    .map(key -> {
                        // If found, convert the key back to a location.
                        return Optional.of(key.location());
                    })
                    .orElseGet(() -> {
                        // If still not found, then send the error to the player.
                        player.sendSystemMessage(Component.translatable("error.tudigong.invalid_resource_location", searchString));
                        return Optional.empty();
                    });
        }
    }
}
