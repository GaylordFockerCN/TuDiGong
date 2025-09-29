package com.p1nero.tudigong.util;

import com.p1nero.tudigong.client.screen.StructureSearchScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TextUtil {
    private static final String[] DIRECTIONS = {
            "direction.tudigong.east", "direction.tudigong.southeast", "direction.tudigong.south",
            "direction.tudigong.southwest", "direction.tudigong.west", "direction.tudigong.northwest",
            "direction.tudigong.north", "direction.tudigong.northeast"
    };

    public static Component getCardinalDirection(Player player, BlockPos target) {
        Vec3 p = player.position();
        Vec3 t = Vec3.atCenterOf(target);
        double angle = Mth.atan2(t.z() - p.z(), t.x() - p.x()) * (180.0 / Math.PI);
        // Normalize angle to be within [0, 360)
        angle = Mth.positiveModulo(angle, 360.0);

        // Add 22.5 degrees to shift the sectors, so that 0-45 degrees corresponds to the first direction.
        // Then divide by 45 to get the index.
        int index = (int) Math.floor((angle + 22.5) / 45.0) & 7;

        return Component.translatable(DIRECTIONS[index]);
    }

    @OnlyIn(Dist.CLIENT)
    public static String tryToGetName(String key) {
        if(!ResourceLocation.isValidResourceLocation(key)) {
            return key;
        }
        ResourceLocation resourceLocation = ResourceLocation.parse(key);
        return tryToGetName(resourceLocation);
    }

    @OnlyIn(Dist.CLIENT)
    public static String tryToGetName(ResourceLocation resourceLocation) {
        if (StructureSearchScreen.STRUCTURE_NAME_MAP.containsKey(resourceLocation)){
            return StructureUtil.getStructureName(resourceLocation);
        }
        return BiomeUtil.getBiomeName(resourceLocation);
    }
}
