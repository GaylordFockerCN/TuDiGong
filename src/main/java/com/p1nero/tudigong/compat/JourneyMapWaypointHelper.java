package com.p1nero.tudigong.compat;

import com.p1nero.dialog_lib.network.DialoguePacketRelay;
import com.p1nero.tudigong.TuDiGongMod;
import com.p1nero.tudigong.network.TDGPacketHandler;
import com.p1nero.tudigong.network.packet.client.AddJourneyMapWaypointPacket;
import com.p1nero.tudigong.network.packet.client.AddXaeroMapWaypointPacket;
import com.p1nero.tudigong.util.TextUtil;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.Waypoint;
import journeymap.client.api.event.ClientEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xaero.hud.minimap.waypoint.WaypointColor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.Locale;

@ParametersAreNonnullByDefault
@journeymap.client.api.ClientPlugin
public class JourneyMapWaypointHelper implements IClientPlugin {
    private static IClientAPI jmAPI;
    @Override
    public void initialize(@NotNull IClientAPI iClientAPI) {
        jmAPI = iClientAPI;
    }

    @Override
    public String getModId() {
        return TuDiGongMod.MOD_ID;
    }

    @Override
    public void onEvent(@NotNull ClientEvent clientEvent) {

    }

    public static void sendWaypoint(ServerPlayer player, String key, BlockPos pos) {
        RandomSource random = player.getRandom();
        Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        int rgb = color.getRGB();
        DialoguePacketRelay.sendToPlayer(TDGPacketHandler.INSTANCE, new AddJourneyMapWaypointPacket(key, pos, rgb), player);
    }
    public static void sendWaypoint(ServerPlayer player, String key, BlockPos pos, int color) {
        DialoguePacketRelay.sendToPlayer(TDGPacketHandler.INSTANCE, new AddJourneyMapWaypointPacket(key, pos, color), player);
    }

    public static void createNewWaypoint(String name, int color, BlockPos pos, ResourceKey<Level> dimension) {
        Waypoint bedWaypoint;
        try {
            bedWaypoint = new Waypoint(TuDiGongMod.MOD_ID, name.toLowerCase(Locale.ROOT) + "_" + dimension, TextUtil.tryToGetName(name), dimension, pos)
                    .setColor(color);

            jmAPI.show(bedWaypoint);

        } catch (Throwable t) {
            TuDiGongMod.LOGGER.error(t.getMessage(), t);
        }
    }
}