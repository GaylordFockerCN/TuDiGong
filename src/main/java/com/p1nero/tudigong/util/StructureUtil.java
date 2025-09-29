package com.p1nero.tudigong.util;

import com.mojang.datafixers.util.Pair;
import com.p1nero.tudigong.TDGConfig;
import com.p1nero.tudigong.TuDiGongMod;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StructureUtil {

    private static final Map<ResourceKey<StructureSet>, Set<ResourceKey<Structure>>> setToStructures = new HashMap<>();
    private static boolean mapsBuilt = false;

    public static void buildStructureSetMaps(MinecraftServer server) {
        if (mapsBuilt) return;

        setToStructures.clear();

        Registry<StructureSet> structureSetRegistry = server.registryAccess().registryOrThrow(Registries.STRUCTURE_SET);

        for (Map.Entry<ResourceKey<StructureSet>, StructureSet> setEntry : structureSetRegistry.entrySet()) {
            ResourceKey<StructureSet> setKey = setEntry.getKey();
            StructureSet structureSet = setEntry.getValue();
            Set<ResourceKey<Structure>> structuresInSet = new HashSet<>();

            for (StructureSet.StructureSelectionEntry selectionEntry : structureSet.structures()) {
                selectionEntry.structure().unwrapKey().ifPresent(structuresInSet::add);
            }
            setToStructures.put(setKey, structuresInSet);
        }
        mapsBuilt = true;
        TuDiGongMod.LOGGER.info("Built StructureSet maps for {} sets.", setToStructures.size());
    }

    public static void clearStructureSetMaps() {
        mapsBuilt = false;
        setToStructures.clear();
    }

    public static Map<ResourceKey<StructureSet>, Set<ResourceKey<Structure>>> getSetToStructuresMap() {
        return setToStructures;
    }


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