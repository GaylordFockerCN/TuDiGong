package com.p1nero.tudigong;

import com.mojang.logging.LogUtils;
import com.p1nero.dialog_lib.network.DialoguePacketRelay;
import com.p1nero.tudigong.block.TDGBlockEntities;
import com.p1nero.tudigong.block.TDGBlocks;
import com.p1nero.tudigong.command.ExportStructuresCommand;
import com.p1nero.tudigong.entity.TDGEntities;
import com.p1nero.tudigong.entity.TudiGongEntity;
import com.p1nero.tudigong.item.TDGItemTabs;
import com.p1nero.tudigong.item.TDGItems;
import com.p1nero.tudigong.network.TDGPacketHandler;
import com.p1nero.tudigong.network.packet.client.*;
import com.p1nero.tudigong.util.StructureTagManager;
import com.p1nero.tudigong.util.StructureUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.*;

@Mod(TuDiGongMod.MOD_ID)
public class TuDiGongMod {

    public static final String MOD_ID = "tudigong";

    public static final Logger LOGGER = LogUtils.getLogger();

    public TuDiGongMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        TDGEntities.REGISTRY.register(modEventBus);
        TDGBlocks.REGISTRY.register(modEventBus);
        TDGBlockEntities.REGISTRY.register(modEventBus);
        TDGItems.REGISTRY.register(modEventBus);
        TDGItemTabs.REGISTRY.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerJoinLevel);
        MinecraftForge.EVENT_BUS.addListener(this::onServerChat);
        MinecraftForge.EVENT_BUS.addListener(this::onBlockChange);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        modEventBus.addListener(this::onDatapackLoad);
        context.registerConfig(ModConfig.Type.COMMON, TDGConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        TDGPacketHandler.register();
        event.enqueueWork(StructureTagManager::load);
    }

    private void onServerChat(ServerChatEvent event) {
        if(event.getRawText().contains("土地") || event.getRawText().toLowerCase(Locale.ROOT).contains("tudi")) {
            ServerPlayer serverPlayer = event.getPlayer();
            if(!serverPlayer.hasPermissions(2)) {
                return;
            }
            ServerLevel serverLevel = serverPlayer.serverLevel();
            Vec3 center = serverPlayer.position();
            if(serverPlayer.level().getBlockState(serverPlayer.getOnPos().below()).is(BlockTags.DIRT)
                && serverLevel.getEntitiesOfClass(TudiGongEntity.class, (new AABB(center, center)).inflate(10)).isEmpty()) {
                TudiGongEntity tudiGongEntity = TDGEntities.TU_DI_GONG.get().spawn(serverPlayer.serverLevel(), serverPlayer.getOnPos().above(1), MobSpawnType.MOB_SUMMONED);
                if(tudiGongEntity != null) {
                    finishAdvancement(TuDiGongMod.MOD_ID + ":sincerity", serverPlayer);
                    serverPlayer.displayClientMessage(ComponentUtils.wrapInSquareBrackets(tudiGongEntity.getDisplayName()).append(": ").append(Component.translatable("entity.tudigong.tudigong.tudigong.answer1")), false);
                }
            }
        }
    }

    /**
     * 同步结构和群系
     */
    private void onPlayerJoinLevel(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncRegistry(serverPlayer, Registries.STRUCTURE);
            syncRegistry(serverPlayer, Registries.BIOME);
            DialoguePacketRelay.sendToPlayer(TDGPacketHandler.INSTANCE, new SyncStructureTagsPacket(StructureTagManager.getTags()), serverPlayer);
            syncStructureSets(serverPlayer);
            syncStructureDimensions(serverPlayer);
            syncBiomeDimensions(serverPlayer);
            syncStructureTypes(serverPlayer);
        }
    }

    public static void syncBiomeDimensions(ServerPlayer serverPlayer) {
        Map<ResourceLocation, List<ResourceLocation>> biomeDimensions = new HashMap<>();
        Registry<Biome> biomeRegistry = serverPlayer.serverLevel().registryAccess().registryOrThrow(Registries.BIOME);

        biomeRegistry.holders().forEach(biomeHolder -> {
            biomeHolder.unwrapKey().ifPresent(biomeResourceKey -> {
                List<ResourceLocation> dims = new ArrayList<>();
                for (ServerLevel level : serverPlayer.getServer().getAllLevels()) {
                    if (level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes().contains(biomeHolder)) {
                        dims.add(level.dimension().location());
                    }
                }
                if (!dims.isEmpty()) {
                    biomeDimensions.put(biomeResourceKey.location(), dims);
                }
            });
        });
        DialoguePacketRelay.sendToPlayer(TDGPacketHandler.INSTANCE, new SyncBiomeDimensionsPacket(biomeDimensions), serverPlayer);
    }

    public static void syncRegistry(ServerPlayer serverPlayer, ResourceKey<? extends Registry<?>> registry) {
        List<ResourceLocation> resourceLocations = new ArrayList<>();
        serverPlayer.serverLevel().registryAccess().lookup(registry).ifPresent(registryLookup -> {
            registryLookup.listElementIds().forEach((resourceKey -> {
                resourceLocations.add(resourceKey.location());
            }));
        });
        DialoguePacketRelay.sendToPlayer(TDGPacketHandler.INSTANCE, new SyncResourceKeysPacket(resourceLocations, registry == Registries.STRUCTURE), serverPlayer);
    }

    public static void syncStructureSets(ServerPlayer serverPlayer) {
        Map<String, Set<ResourceLocation>> structureSets = new HashMap<>();
        Registry<StructureSet> registry = serverPlayer.serverLevel().registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
        registry.entrySet().forEach(entry -> {
            ResourceLocation setLocation = entry.getKey().location();
            Set<ResourceLocation> structures = new HashSet<>();
            entry.getValue().structures().forEach(selectionEntry -> {
                selectionEntry.structure().unwrapKey().ifPresent(key -> structures.add(key.location()));
            });
            if (!structures.isEmpty()) {
                structureSets.put(setLocation.toString(), structures);
            }
        });
        DialoguePacketRelay.sendToPlayer(TDGPacketHandler.INSTANCE, new SyncStructureSetsPacket(structureSets), serverPlayer);
    }

    public static void syncStructureDimensions(ServerPlayer serverPlayer) {
        Map<ResourceLocation, List<ResourceLocation>> structureDimensions = new HashMap<>();
        Registry<Structure> structureRegistry = serverPlayer.serverLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
        structureRegistry.forEach(structure -> {
            ResourceLocation key = structureRegistry.getKey(structure);
            if (key != null) {
                List<ResourceLocation> dims = StructureUtils.getGeneratingDimensionKeys(serverPlayer.serverLevel(), structure);
                if (!dims.isEmpty()) {
                    structureDimensions.put(key, dims);
                }
            }
        });
        DialoguePacketRelay.sendToPlayer(TDGPacketHandler.INSTANCE, new SyncStructureDimensionsPacket(structureDimensions), serverPlayer);
    }

    public static void syncStructureTypes(ServerPlayer serverPlayer) {
        DialoguePacketRelay.sendToPlayer(TDGPacketHandler.INSTANCE, new SyncStructureTypesPacket(StructureUtils.getTypeKeysToStructureKeys(serverPlayer.serverLevel()).asMap(), StructureUtils.getStructureKeysToTypeKeys(serverPlayer.serverLevel())), serverPlayer);
    }

    private void onBlockChange(BlockEvent.EntityPlaceEvent event) {
        if(event.getLevel() instanceof ServerLevel serverLevel && event.getPlacedBlock().is(Blocks.TORCH)) {
            for(Direction direction : Direction.values()) {
                BlockPos blockpos = event.getPos().relative(direction);
                if (serverLevel.hasChunkAt(blockpos)) {
                    BlockState blockstate = serverLevel.getBlockState(blockpos);
                    if(blockstate.is(TDGBlocks.TUDI_TEMPLE.get())) {
                        Vec3 center = event.getPos().getCenter();
                        serverLevel.getEntitiesOfClass(Player.class, (new AABB(center, center)).inflate(3)).forEach(player -> {
                            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 2400, 1, false, false, true));
                        });
                        if(event.getEntity() instanceof ServerPlayer serverPlayer) {
                            finishAdvancement(TuDiGongMod.MOD_ID + ":sincerity2", serverPlayer);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void onDatapackLoad(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA && TDGConfig.GENERATE_TEMPLE.get()) {
            addNewDatapack(event, "tu_di_temple");
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        ExportStructuresCommand.register(event.getDispatcher());
    }

    private void addNewDatapack(AddPackFindersEvent event, String name) {
        var resourcePath = ModList.get().getModFileById(MOD_ID).getFile().findResource("packs/" + name);
        var pack = Pack.readMetaAndCreate(name, Component.literal(name), true,
                (path) -> new PathPackResources(path, resourcePath, false), PackType.SERVER_DATA, Pack.Position.TOP, PackSource.WORLD);
        event.addRepositorySource((packConsumer) -> packConsumer.accept(pack));
    }

    public static void finishAdvancement(Advancement advancement, ServerPlayer serverPlayer) {
        AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
        if (!progress.isDone()) {
            for (String criteria : progress.getRemainingCriteria()) {
                serverPlayer.getAdvancements().award(advancement, criteria);
            }
        }
    }

    public static void finishAdvancement(String resourceLocation, ServerPlayer serverPlayer) {
        Advancement advancement = serverPlayer.server.getAdvancements().getAdvancement(ResourceLocation.parse(resourceLocation));
        if (advancement == null) {
            LOGGER.error("advancement:\"{}\" is null!", resourceLocation);
            return;
        }
        finishAdvancement(advancement, serverPlayer);
    }
}
