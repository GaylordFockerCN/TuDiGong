package com.p1nero.tudigong.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("deprecation")
public class StructureUtils {

    public static ListMultimap<ResourceLocation, ResourceLocation> getTypeKeysToStructureKeys(ServerLevel level) {
        ListMultimap<ResourceLocation, ResourceLocation> typeKeysToStructureKeys = ArrayListMultimap.create();
        for (Structure structure : getStructureRegistry(level)) {
            typeKeysToStructureKeys.put(getTypeForStructure(level, structure), getKeyForStructure(level, structure));
        }
        return typeKeysToStructureKeys;
    }

    public static Map<ResourceLocation, ResourceLocation> getStructureKeysToTypeKeys(ServerLevel level) {
        Map<ResourceLocation, ResourceLocation> structureKeysToStructureKeys = new HashMap<>();
        for (Structure structure : getStructureRegistry(level)) {
            structureKeysToStructureKeys.put(getKeyForStructure(level, structure), getTypeForStructure(level, structure));
        }
        return structureKeysToStructureKeys;
    }

    public static ResourceLocation getTypeForStructure(ServerLevel level, Structure structure) {
        Registry<StructureSet> registry = getStructureSetRegistry(level);
        for (StructureSet set : registry) {
            for (StructureSet.StructureSelectionEntry entry : set.structures()) {
                if (entry.structure().get().equals(structure)) {
                    return registry.getKey(set);
                }
            }
        }
        return ResourceLocation.fromNamespaceAndPath("tudigong", "none");
    }

    public static ResourceLocation getKeyForStructure(ServerLevel level, Structure structure) {
        return getStructureRegistry(level).getKey(structure);
    }

    public static Structure getStructureForKey(ServerLevel level, ResourceLocation key) {
        return getStructureRegistry(level).get(key);
    }

    public static Holder<Structure> getHolderForStructure(ServerLevel level, Structure structure) {
        return getStructureRegistry(level).wrapAsHolder(structure);
    }

    public static List<ResourceLocation> getAllowedStructureKeys(ServerLevel level) {
        final List<ResourceLocation> structures = new ArrayList<>();
        for (Structure structure : getStructureRegistry(level)) {
            if (structure != null && getKeyForStructure(level, structure) != null && !structureIsBlacklisted(level, structure) && !structureIsHidden(level, structure)) {
                structures.add(getKeyForStructure(level, structure));
            }
        }
        return structures;
    }

    public static boolean structureIsBlacklisted(ServerLevel level, Structure structure) {
        // TODO: Implement a configuration handler for blacklisting structures
        final List<String> structureBlacklist = new ArrayList<>();
        for (String structureKey : structureBlacklist) {
            if (getKeyForStructure(level, structure).toString().matches(convertToRegex(structureKey))) {
                return true;
            }
        }
        return false;
    }

    public static boolean structureIsHidden(ServerLevel level, Structure structure) {
        final Registry<Structure> structureRegistry = getStructureRegistry(level);
        return structureRegistry.wrapAsHolder(structure).getTagKeys().anyMatch(tag -> tag.location().getPath().equals("c:hidden_from_locator_selection"));
    }

    public static List<ResourceLocation> getGeneratingDimensionKeys(ServerLevel serverLevel, Structure structure) {
        final List<ResourceLocation> dimensions = new ArrayList<>();
        for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
            ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
            Set<Holder<Biome>> biomeSet = chunkGenerator.getBiomeSource().possibleBiomes();
            if (structure.biomes().stream().anyMatch(biomeSet::contains)) {
                dimensions.add(level.dimension().location());
            }
        }
        // Fix empty dimensions for stronghold
        if (structure.type() == StructureType.STRONGHOLD && dimensions.isEmpty()) {
            dimensions.add(ResourceLocation.parse("minecraft:overworld"));
        }
        return dimensions;
    }

    public static ListMultimap<ResourceLocation, ResourceLocation> getGeneratingDimensionsForAllowedStructures(ServerLevel serverLevel) {
        ListMultimap<ResourceLocation, ResourceLocation> dimensionsForAllowedStructures = ArrayListMultimap.create();
        for (ResourceLocation structureKey : getAllowedStructureKeys(serverLevel)) {
            Structure structure = getStructureForKey(serverLevel, structureKey);
            if (structure != null) {
                dimensionsForAllowedStructures.putAll(structureKey, getGeneratingDimensionKeys(serverLevel, structure));
            }
        }
        return dimensionsForAllowedStructures;
    }

    public static int getHorizontalDistanceToLocation(Player player, int x, int z) {
        return getHorizontalDistanceToLocation(player.blockPosition(), x, z);
    }

    public static int getHorizontalDistanceToLocation(BlockPos startPos, int x, int z) {
        return (int) Mth.sqrt((float) startPos.distSqr(new BlockPos(x, startPos.getY(), z)));
    }

    @OnlyIn(Dist.CLIENT)
    public static String getPrettyStructureName(ResourceLocation key) {
        String name = key.toString();
        // TODO: Implement a configuration handler for translating structure names
        boolean translate = true;
        if (translate) {
            name = I18n.get(Util.makeDescriptionId("structure", key));
        }
        if (name.equals(Util.makeDescriptionId("structure", key)) || !translate) {
            name = key.toString();
            if (name.contains(":")) {
                name = name.substring(name.indexOf(":") + 1);
            }
            name = WordUtils.capitalize(name.replace('_', ' '));
        }
        return name;
    }

    @OnlyIn(Dist.CLIENT)
    public static String getPrettyStructureSource(ResourceLocation key) {
        if (key == null) {
            return "";
        }
        String registryEntry = key.toString();
        String modid = registryEntry.substring(0, registryEntry.indexOf(":"));
        if (modid.equals("minecraft")) {
            return "Minecraft";
        }
        Optional<? extends ModContainer> sourceContainer = ModList.get().getModContainerById(modid);
        if (sourceContainer.isPresent()) {
            return sourceContainer.get().getModInfo().getDisplayName();
        }
        return modid;
    }

    @OnlyIn(Dist.CLIENT)
    public static String dimensionKeysToString(List<ResourceLocation> dimensions) {
        Set<String> dimensionNames = new HashSet<>();
        dimensions.forEach((key) -> dimensionNames.add(TextUtil.getDimensionName(key)));
        return String.join(", ", dimensionNames);
    }

    private static Registry<Structure> getStructureRegistry(ServerLevel level) {
        return level.registryAccess().registry(Registries.STRUCTURE).orElseThrow();
    }

    private static Registry<StructureSet> getStructureSetRegistry(ServerLevel level) {
        return level.registryAccess().registry(Registries.STRUCTURE_SET).orElseThrow();
    }

    private static String convertToRegex(String glob) {
        StringBuilder regex = new StringBuilder("^");
        for (char c : glob.toCharArray()) {
            switch (c) {
                case '*':
                    regex.append(".*");
                    break;
                case '?':
                    regex.append(".");
                    break;
                case '.':
                    regex.append("\\.");
                    break;
                default:
                    regex.append(c);
                    break;
            }
        }
        regex.append("$");
        return regex.toString();
    }

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

    public static Optional<ResourceLocation> getRandomStructureForSet(ServerLevel level, String setName) {
        ResourceLocation setLocation = ResourceLocation.tryParse(setName);
        if (setLocation == null) {
            return Optional.empty();
        }
        ResourceKey<StructureSet> setKey = ResourceKey.create(Registries.STRUCTURE_SET, setLocation);
        Registry<StructureSet> registry = getStructureSetRegistry(level);
        var structureSetHolder = registry.getHolder(setKey);
        if (structureSetHolder.isEmpty()) {
            return Optional.empty();
        }

        List<StructureSet.StructureSelectionEntry> structures = structureSetHolder.get().get().structures();
        if (structures.isEmpty()) {
            return Optional.empty();
        }

        int totalWeight = 0;
        for (StructureSet.StructureSelectionEntry entry : structures) {
            totalWeight += entry.weight();
        }

        if (totalWeight == 0) {
            return structures.get(new Random().nextInt(structures.size())).structure().unwrapKey().map(ResourceKey::location);
        }

        int randomWeight = new Random().nextInt(totalWeight);
        int currentWeight = 0;
        for (StructureSet.StructureSelectionEntry entry : structures) {
            currentWeight += entry.weight();
            if (randomWeight < currentWeight) {
                return entry.structure().unwrapKey().map(ResourceKey::location);
            }
        }

        return Optional.empty();
    }
}
