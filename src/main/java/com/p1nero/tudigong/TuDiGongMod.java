package com.p1nero.tudigong;

import com.mojang.logging.LogUtils;
import com.p1nero.dialog_lib.network.DialoguePacketRelay;
import com.p1nero.tudigong.block.TDGBlockEntities;
import com.p1nero.tudigong.block.TDGBlocks;
import com.p1nero.tudigong.entity.TDGEntities;
import com.p1nero.tudigong.entity.TudiGongEntity;
import com.p1nero.tudigong.item.TDGItemTabs;
import com.p1nero.tudigong.item.TDGItems;
import com.p1nero.tudigong.network.TDGPacketHandler;
import com.p1nero.tudigong.network.packet.client.SyncResourceKeysPacket;
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
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
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

import java.util.ArrayList;
import java.util.List;

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
        modEventBus.addListener(this::onDatapackLoad);
        context.registerConfig(ModConfig.Type.COMMON, TDGConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        TDGPacketHandler.register();
    }

    private void onServerChat(ServerChatEvent event) {
        if(event.getRawText().contains("土地") || event.getRawText().contains("TuDi")) {
            ServerPlayer serverPlayer = event.getPlayer();
            if(!serverPlayer.hasPermissions(2)) {
                return;
            }
            if(serverPlayer.level().getBlockState(serverPlayer.getOnPos().below()).is(BlockTags.DIRT)) {
                TudiGongEntity tudiGongEntity = TDGEntities.TU_DI_GONG.get().spawn(serverPlayer.serverLevel(), serverPlayer.getOnPos().above(1), MobSpawnType.MOB_SUMMONED);
                if(tudiGongEntity != null) {
                    finishAdvancement(TuDiGongMod.MOD_ID + ":sincerity", serverPlayer);
                    serverPlayer.displayClientMessage(ComponentUtils.wrapInSquareBrackets(tudiGongEntity.getDisplayName()).append(": ").append(Component.translatable("entity.tudigong.tudigong.dialog1")), false);
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
        }
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
