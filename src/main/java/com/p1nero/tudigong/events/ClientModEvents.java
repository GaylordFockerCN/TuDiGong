package com.p1nero.tudigong.events;

import com.p1nero.tudigong.TuDiGongMod;
import com.p1nero.tudigong.entity.TDGEntities;
import com.p1nero.tudigong.entity.client.TudiGongModel;
import com.p1nero.tudigong.entity.client.TudiGongRenderer;
import com.p1nero.tudigong.entity.client.XianQiRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = TuDiGongMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(TDGEntities.TU_DI_GONG.get(), TudiGongRenderer::new);
        EntityRenderers.register(TDGEntities.XIAN_QI.get(), XianQiRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TudiGongModel.LAYER_LOCATION, TudiGongModel::createBodyLayer);
    }
}
