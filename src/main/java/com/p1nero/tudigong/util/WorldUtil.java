package com.p1nero.tudigong.util;

import com.p1nero.tudigong.client.screen.StructureSearchScreen;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorldUtil {

    private static final Pattern LOCATE_STRUCTURE_PATTERN =
            Pattern.compile(".*?\\[\\s*(-?\\d+)\\s*,\\s*~\\s*,\\s*(-?\\d+)\\s*\\].*");
    private static final Pattern LOCATE_BIOME_PATTERN =
            Pattern.compile(".*?\\[\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\].*");

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
        return getLocateStructurePos(serverPlayer, structureId, y);
    }

    @Nullable
    public static BlockPos getNearbyBiomePos(ServerPlayer serverPlayer, String biomeId) {
        return getLocateBiomePos(serverPlayer, biomeId);
    }

    public static BlockPos getLocateStructurePos(ServerPlayer serverPlayer, String structureId, int y) {
        String output = getCommandOutput(serverPlayer.serverLevel(), serverPlayer.position(), "/locate structure " + structureId);
        Matcher matcher = LOCATE_STRUCTURE_PATTERN.matcher(output);
        if (matcher.find()) {
            try {
                String xStr = matcher.group(1).trim();
                String zStr = matcher.group(2).trim();
                return new BlockPos(Integer.parseInt(xStr), y, Integer.parseInt(zStr));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    public static BlockPos getLocateBiomePos(ServerPlayer serverPlayer, String biomeId) {
        String output = getCommandOutput(serverPlayer.serverLevel(), serverPlayer.position(), "/locate biome " + biomeId);
        Matcher matcher = LOCATE_BIOME_PATTERN.matcher(output);
        if (matcher.find()) {
            try {
                String xStr = matcher.group(1).trim();
                String yStr = matcher.group(2).trim();
                String zStr = matcher.group(3).trim();
                return new BlockPos(Integer.parseInt(xStr), Integer.parseInt(yStr), Integer.parseInt(zStr));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    public static String getCommandOutput(ServerLevel serverLevel, @Nullable Vec3 vec, String command) {
        BaseCommandBlock baseCommandBlock = new BaseCommandBlock() {
            @Override
            public @NotNull ServerLevel getLevel() {
                return serverLevel;
            }

            @Override
            public void onUpdated() {
            }

            @Override
            public @NotNull Vec3 getPosition() {
                return Objects.requireNonNullElseGet(vec, () -> new Vec3(0, 0, 0));
            }

            @Override
            public @NotNull CommandSourceStack createCommandSourceStack() {
                return new CommandSourceStack(this, getPosition(), Vec2.ZERO, serverLevel, 2, "dev", Component.literal("dev"), serverLevel.getServer(), null);
            }

            @Override
            public boolean isValid() {
                return true;
            }

            @Override
            public boolean performCommand(Level pLevel) {
                if (!pLevel.isClientSide) {
                    this.setSuccessCount(0);
                    MinecraftServer server = this.getLevel().getServer();
                    try {
                        this.setLastOutput(null);
                        CommandSourceStack commandSourceStack = this.createCommandSourceStack().withCallback((context, success, i) -> {
                            if (success) {
                                this.setSuccessCount(this.getSuccessCount() + 1);
                            }
                        });
                        server.getCommands().performPrefixedCommand(commandSourceStack, this.getCommand());
                    } catch (Throwable ignored) {
                    }
                }
                return true;
            }
        };

        baseCommandBlock.setCommand(command);
        baseCommandBlock.setTrackOutput(true);
        baseCommandBlock.performCommand(serverLevel);

        return baseCommandBlock.getLastOutput().getString();
    }

    @OnlyIn(Dist.CLIENT)
    public static String getBiomeName(ResourceLocation key) {
        return I18n.get(Util.makeDescriptionId("biome", key));
    }

    @OnlyIn(Dist.CLIENT)
    public static String getStructureName(ResourceLocation key) {
        return I18n.get(Util.makeDescriptionId("structure", key));
    }

    @OnlyIn(Dist.CLIENT)
    public static String tryToGetName(String key) {
        if(!ResourceLocation.isValidResourceLocation(key)) {
            return key;
        }
        ResourceLocation resourceLocation = ResourceLocation.parse(key);
        return tryToGetName(resourceLocation);
    }

    public static String tryToGetName(ResourceLocation resourceLocation) {
        if (StructureSearchScreen.STRUCTURE_NAME_MAP.containsKey(resourceLocation)){
            return getStructureName(resourceLocation);
        }
        return getBiomeName(resourceLocation);
    }

}
