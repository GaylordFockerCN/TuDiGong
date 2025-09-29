package com.p1nero.tudigong.util;

import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BiomeUtil {

    private static final Pattern LOCATE_BIOME_PATTERN =
            Pattern.compile(".*?\\[\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\].*");
    @Nullable
    public static BlockPos getNearbyBiomePos(ServerPlayer serverPlayer, String biomeId) {
        return getLocateBiomePos(serverPlayer, biomeId);
    }

    public static BlockPos getLocateBiomePos(ServerPlayer serverPlayer, String biomeId) {
        String output = CommandUtil.getCommandOutput(serverPlayer.serverLevel(), serverPlayer.position(), "/locate biome " + biomeId);
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

    @OnlyIn(Dist.CLIENT)
    public static String getBiomeName(ResourceLocation key) {
        return I18n.get(Util.makeDescriptionId("biome", key));
    }
}
