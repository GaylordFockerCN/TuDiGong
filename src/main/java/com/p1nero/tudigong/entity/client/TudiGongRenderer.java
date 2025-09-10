package com.p1nero.tudigong.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.p1nero.tudigong.TuDiGongMod;
import com.p1nero.tudigong.entity.TudiGongEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class TudiGongRenderer extends MobRenderer<TudiGongEntity, TudiGongModel<TudiGongEntity>> {
    public TudiGongRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new TudiGongModel<>(pContext.bakeLayer(TudiGongModel.LAYER_LOCATION)), 0.2F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull TudiGongEntity pEntity) {
        return ResourceLocation.fromNamespaceAndPath(TuDiGongMod.MOD_ID, "textures/entity/tudigong.png");
    }

    @Override
    public void render(@NotNull TudiGongEntity tudiGongEntity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int p_115460_) {
        poseStack.pushPose();
        int tickCount = tudiGongEntity.tickCount;
        int timeLeft = tudiGongEntity.getRemoveTimer();
        int max = tudiGongEntity.maxRemoveTime;

        // 计算基础浮动高度
        float floatingHeight = calculateFloatingHeight(tudiGongEntity, partialTicks) + 0.5F;

        // 计算螺旋高度变化
        float spiralHeight = tudiGongEntity.canInteract() ? 0 : getSpiralHeight(tickCount, max, timeLeft) - 2;

        // 应用高度变换
        poseStack.translate(0, 0.2 + floatingHeight + spiralHeight, 0);

        if (!tudiGongEntity.canInteract()) {
            // 根据高度变化添加旋转，实现螺旋效果
            float rotationAngle = (tudiGongEntity.level().getGameTime() + partialTicks) * 10.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(90));

        super.render(tudiGongEntity, entityYaw, partialTicks, poseStack, bufferSource, p_115460_);
        poseStack.popPose();
    }

    private static float getSpiralHeight(int tickCount, int max, int timeLeft) {
        float progress = (float) (timeLeft > 0 ? timeLeft : tickCount) / max;
        return  2.0f * Mth.sin(progress * Mth.PI * 0.5f);
    }

    private float calculateFloatingHeight(TudiGongEntity entity, float partialTicks) {
        final float SPEED = 0.1f;
        final float BOBBING_SCALE = 0.05f;
        float time = (entity.level().getGameTime() + partialTicks) * SPEED;
        float entityPhase = entity.getId() * 0.1f;
        return Mth.cos(time + entityPhase) * BOBBING_SCALE;
    }
}