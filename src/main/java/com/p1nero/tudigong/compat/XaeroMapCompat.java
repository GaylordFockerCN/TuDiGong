package com.p1nero.tudigong.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;

import java.util.function.Supplier;

public class XaeroMapCompat {

    public static void sendWaypoint(ServerPlayer player, String key, BlockPos pos) {
        runInXaerMapoLoaded(()->()-> XaeroWaypointHelper.sendWaypoint(player, key, pos));
    }

    public static void sendWaypoint(ServerPlayer player, String key, BlockPos pos, int colorIndex) {
        runInXaerMapoLoaded(()->()-> XaeroWaypointHelper.sendWaypoint(player, key, pos, colorIndex));
    }

    @OnlyIn(Dist.CLIENT)
    public static void createWaypoint(BlockPos pos, String name, String color) {
        runInXaerMapoLoaded(()->()-> XaeroWaypointHelper.addWayPoint(pos, name, color));
    }

    public static void runInXaerMapoLoaded(Supplier<Runnable> handler) {
        if (ModList.get().isLoaded("xaerominimap")) {
            handler.get().run();
        }
    }
}
