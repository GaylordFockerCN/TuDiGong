package com.p1nero.tudigong.util;

import com.mojang.datafixers.util.Pair;
import com.p1nero.tudigong.TDGConfig;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class StructureUtil {

    public static boolean isInStructure(LivingEntity entity, String structure) {
        if (entity.level().isClientSide) {
            return false;
        }
        return matches(ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse(structure)), ((ServerLevel) entity.level()), entity.getX(), entity.getY(), entity.getZ());
    }

    public static boolean matches(ResourceKey<Structure> resourceKey, ServerLevel serverLevel, double x, double y, double z) {
        return matches(resourceKey, serverLevel, (float) x, (float) y, (float) z);
    }

    public static boolean matches(ResourceKey<Structure> resourceKey, ServerLevel serverLevel, float x, float y, float z) {
        BlockPos blockpos = new BlockPos((int) x, (int) y, (int) z);
        return resourceKey != null && serverLevel.isLoaded(blockpos) && serverLevel.structureManager().getStructureWithPieceAt(blockpos, resourceKey).isValid();
    }

    @Nullable
    public static BlockPos getNearbyStructurePos(ServerPlayer serverPlayer, String structureId, int y) {
        ServerLevel serverLevel = serverPlayer.serverLevel();
        ResourceLocation structureResourceLocation = ResourceLocation.tryParse(structureId);
        if (structureResourceLocation == null) {
            return null;
        }

        ResourceKey<Structure> structureKey = ResourceKey.create(Registries.STRUCTURE, structureResourceLocation);
        Registry<Structure> structureRegistry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);

        var structureHolderOpt = structureRegistry.getHolder(structureKey);
        if (structureHolderOpt.isEmpty()) {
            return null;
        }

        HolderSet<Structure> structureSet = HolderSet.direct(structureHolderOpt.get());

        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        BlockPos playerPos = serverPlayer.blockPosition();

        Pair<BlockPos, Holder<Structure>> result = chunkGenerator.findNearestMapStructure(
                serverLevel,
                structureSet,
                playerPos,
                TDGConfig.STRUCTURE_SEARCH_RADIUS_CHUNKS.get(),
                false
        );

        if (result != null) {
            BlockPos structurePos = result.getFirst();
            return new BlockPos(structurePos.getX(), y, structurePos.getZ());
        }

        return null;
    }

    /**
     * Finds the first structure whose type name (e.g., "jigsaw", "fortress") partially matches the query.
     *
     * @param registryAccess The server's registry access.
     * @param typeNameQuery  The string to search for within structure type names.
     * @return An Optional containing the ResourceKey of the first matching structure, or empty if none found.
     */
    public static Optional<ResourceKey<Structure>> findStructureByTypeName(RegistryAccess registryAccess, String typeNameQuery) {
        Registry<Structure> structureRegistry = registryAccess.registryOrThrow(Registries.STRUCTURE);
        Registry<StructureType<?>> typeRegistry = registryAccess.registryOrThrow(Registries.STRUCTURE_TYPE);
        final String lowerCaseQuery = typeNameQuery.toLowerCase();

        // Stream over all registered structure entries
        return structureRegistry.entrySet().stream()
                .filter(entry -> {
                    // Get the structure's type
                    StructureType<?> structureType = entry.getValue().type();
                    // Get the ResourceKey for that type from the type registry
                    Optional<ResourceKey<StructureType<?>>> typeKeyOpt = typeRegistry.getResourceKey(structureType);
                    // Check if the type's name (the path part of its ID) contains the query string
                    return typeKeyOpt
                            .map(key -> key.location().getPath().toLowerCase().contains(lowerCaseQuery))
                            .orElse(false);
                })
                // If a match is found, get its key
                .map(Map.Entry::getKey)
                .findFirst();
    }

    @OnlyIn(Dist.CLIENT)
    public static String getStructureName(ResourceLocation key) {
        String name = I18n.get(Util.makeDescriptionId("structure", key));
        if (name.equals(Util.makeDescriptionId("structure", key))) {
            name = key.toString();
            if (name.contains(":")) {
                name = name.substring(name.indexOf(":") + 1);
            }
            name = WordUtils.capitalize(name.replace('_', ' '));
        }
        return name;
    }
}
