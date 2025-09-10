package com.p1nero.tudigong.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

import java.util.function.Supplier;

public class JourneyMapCompat {

    public static void runInJourneyMapLoaded(Supplier<Runnable> handler) {
        if (ModList.get().isLoaded("journeymap")) {
            handler.get().run();
        }
    }

    public static void sendWaypoint(ServerPlayer player, String key, BlockPos pos) {
        runInJourneyMapLoaded(()->()-> JourneyMapWaypointHelper.sendWaypoint(player, key, pos));
    }

    public static void sendWaypoint(ServerPlayer player, String key, BlockPos pos, int color) {
        runInJourneyMapLoaded(()->()-> JourneyMapWaypointHelper.sendWaypoint(player, key, pos, color));
    }

    public static void createNewWaypoint(String name, int color, BlockPos pos, ResourceKey<Level> dimension) {
        runInJourneyMapLoaded(() -> () -> JourneyMapWaypointHelper.createNewWaypoint(name, color, pos, dimension));
    }
}