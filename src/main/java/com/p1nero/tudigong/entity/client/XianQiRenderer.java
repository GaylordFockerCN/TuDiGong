package com.p1nero.tudigong.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.p1nero.tudigong.TuDiGongMod;
import com.p1nero.tudigong.entity.XianQiEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class XianQiRenderer extends MobRenderer<XianQiEntity, EntityModel<XianQiEntity>> {

    public XianQiRenderer(EntityRendererProvider.Context p_174304_) {
        super(p_174304_, new EntityModel<>() {
            @Override
            public void setupAnim(XianQiEntity p_102618_, float p_102619_, float p_102620_, float p_102621_, float p_102622_, float p_102623_) {

            }

            @Override
            public void renderToBuffer(PoseStack p_103111_, VertexConsumer p_103112_, int p_103113_, int p_103114_, float p_103115_, float p_103116_, float p_103117_, float p_103118_) {

            }
        }, 0.5F);
    }

    @Override
    public void render(XianQiEntity p_115455_, float p_115456_, float p_115457_, PoseStack p_115458_, MultiBufferSource p_115459_, int p_115460_) {

    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull XianQiEntity pEntity) {
        return ResourceLocation.fromNamespaceAndPath(TuDiGongMod.MOD_ID, "textures/entity/tudigong.png");
    }

}