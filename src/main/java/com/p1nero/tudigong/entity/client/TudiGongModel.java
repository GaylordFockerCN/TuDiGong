package com.p1nero.tudigong.entity.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.p1nero.tudigong.TuDiGongMod;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class TudiGongModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TuDiGongMod.MOD_ID, "tudigong"), "main");
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart maozi;
    private final ModelPart LeftMeimao2;
    private final ModelPart group2;
    private final ModelPart LeftMeimao;
    private final ModelPart group3;
    private final ModelPart noes;
    private final ModelPart RightHuzi;
    private final ModelPart group4;
    private final ModelPart LeftHuzi;
    private final ModelPart group5;
    private final ModelPart Huzi;
    private final ModelPart group;
    private final ModelPart RightHand;
    private final ModelPart yuanbao;
    private final ModelPart LeftHand;
    private final ModelPart gunzi;
    private final ModelPart LeftLeg;
    private final ModelPart RightLeg;
    private final ModelPart LeftPP;
    private final ModelPart RightPP;

    public TudiGongModel(ModelPart root) {
        this.root = root.getChild("root");
        this.head = this.root.getChild("head");
        this.maozi = this.head.getChild("maozi");
        this.LeftMeimao2 = this.head.getChild("LeftMeimao2");
        this.group2 = this.LeftMeimao2.getChild("group2");
        this.LeftMeimao = this.head.getChild("LeftMeimao");
        this.group3 = this.LeftMeimao.getChild("group3");
        this.noes = this.head.getChild("noes");
        this.RightHuzi = this.head.getChild("RightHuzi");
        this.group4 = this.RightHuzi.getChild("group4");
        this.LeftHuzi = this.head.getChild("LeftHuzi");
        this.group5 = this.LeftHuzi.getChild("group5");
        this.Huzi = this.head.getChild("Huzi");
        this.group = this.Huzi.getChild("group");
        this.RightHand = this.root.getChild("RightHand");
        this.yuanbao = this.RightHand.getChild("yuanbao");
        this.LeftHand = this.root.getChild("LeftHand");
        this.gunzi = this.LeftHand.getChild("gunzi");
        this.LeftLeg = this.root.getChild("LeftLeg");
        this.RightLeg = this.root.getChild("RightLeg");
        this.LeftPP = this.root.getChild("LeftPP");
        this.RightPP = this.root.getChild("RightPP");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(0, 0).addBox(-0.75F, 3.625F, -3.5F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 6).addBox(-0.5F, 1.625F, -3.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.25F, 16.375F, 1.5F));

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(14, 4).addBox(0.4297F, -2.4692F, 6.625F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(16, 9).addBox(2.9297F, -2.4692F, 6.625F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(16, 14).addBox(0.4297F, -2.4692F, 6.625F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(14, 19).addBox(0.4297F, -2.4692F, 9.125F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(0.4297F, -0.4692F, 6.625F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(12, 6).addBox(-0.0703F, -2.4692F, 6.125F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(14, 0).addBox(1.0797F, -1.6692F, 6.375F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.6797F, 2.0942F, -9.375F));

        PartDefinition maozi = head.addOrReplaceChild("maozi", CubeListBuilder.create().texOffs(0, 16).addBox(-1.25F, -0.5F, 7.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(4, 22).addBox(-0.75F, -0.85F, 7.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9297F, -2.4692F, -0.125F));

        PartDefinition LeftMeimao2 = head.addOrReplaceChild("LeftMeimao2", CubeListBuilder.create(), PartPose.offset(0.5047F, -2.1339F, 0.5484F));

        PartDefinition cube_r1 = LeftMeimao2.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(8, 22).addBox(-0.125F, -0.25F, -0.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 6.6533F, 0.3927F, 0.0F, 0.0F));

        PartDefinition group2 = LeftMeimao2.addOrReplaceChild("group2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.056F, 0.7886F));

        PartDefinition cube_r2 = group2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(4, 19).addBox(-0.125F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 5.0761F, -0.3927F, 0.0F, 0.0F));

        PartDefinition LeftMeimao = head.addOrReplaceChild("LeftMeimao", CubeListBuilder.create(), PartPose.offset(0.5047F, -2.1339F, 0.5484F));

        PartDefinition cube_r3 = LeftMeimao.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(8, 22).addBox(-0.125F, -0.25F, -0.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 8.0F, -0.3927F, 0.0F, 0.0F));

        PartDefinition group3 = LeftMeimao.addOrReplaceChild("group3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.056F, 0.7886F));

        PartDefinition cube_r4 = group3.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(20, 19).addBox(-0.125F, 0.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 8.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition noes = head.addOrReplaceChild("noes", CubeListBuilder.create(), PartPose.offset(0.4297F, -1.3192F, -8.025F));

        PartDefinition cube_r5 = noes.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(20, 4).addBox(-0.5F, -0.5F, -0.65F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1115F, -0.0079F, 15.85F, 0.0F, 0.0F, 0.3927F));

        PartDefinition cube_r6 = noes.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(8, 16).addBox(-0.5F, -0.25F, -0.35F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2072F, -0.2389F, 15.95F, 0.0F, 0.0F, 0.3927F));

        PartDefinition RightHuzi = head.addOrReplaceChild("RightHuzi", CubeListBuilder.create(), PartPose.offset(0.3047F, -0.9796F, -0.0576F));

        PartDefinition cube_r7 = RightHuzi.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(22, 9).addBox(-0.125F, -0.15F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 8.0F, -0.3927F, 0.0F, 0.0F));

        PartDefinition group4 = RightHuzi.addOrReplaceChild("group4", CubeListBuilder.create(), PartPose.offset(0.0F, 0.075F, 0.4F));

        PartDefinition cube_r8 = group4.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(18, 21).addBox(0.025F, -0.05F, -0.65F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 8.2826F, 0.0F, 0.0F, 0.3927F));

        PartDefinition LeftHuzi = head.addOrReplaceChild("LeftHuzi", CubeListBuilder.create(), PartPose.offset(0.3047F, -0.9796F, -0.0576F));

        PartDefinition cube_r9 = LeftHuzi.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(22, 11).addBox(-0.125F, -0.15F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 7.8652F, 0.3927F, 0.0F, 0.0F));

        PartDefinition group5 = LeftHuzi.addOrReplaceChild("group5", CubeListBuilder.create(), PartPose.offset(0.0F, 0.075F, 0.4F));

        PartDefinition cube_r10 = group5.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(12, 22).addBox(0.025F, -0.05F, -0.35F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 6.7826F, 0.0F, 0.0F, 0.3927F));

        PartDefinition Huzi = head.addOrReplaceChild("Huzi", CubeListBuilder.create(), PartPose.offset(0.3986F, -0.6761F, -0.15F));

        PartDefinition cube_r11 = Huzi.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(22, 13).addBox(-0.225F, 0.0F, -0.475F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 8.0F, 0.0F, 0.0F, 0.3927F));

        PartDefinition group = Huzi.addOrReplaceChild("group", CubeListBuilder.create().texOffs(20, 22).addBox(0.125F, 0.25F, 7.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5236F, 0.6761F, 0.15F));

        PartDefinition RightHand = root.addOrReplaceChild("RightHand", CubeListBuilder.create(), PartPose.offset(0.75F, 2.375F, 0.25F));

        PartDefinition cube_r12 = RightHand.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, 14).addBox(-3.5F, -0.5F, 0.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.25F, 0.0F, 0.0F, -0.3927F));

        PartDefinition yuanbao = RightHand.addOrReplaceChild("yuanbao", CubeListBuilder.create().texOffs(22, 0).addBox(-0.575F, -0.05F, -0.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 2).addBox(-0.55F, -0.35F, -0.875F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 0.2033F, 0.7706F));

        PartDefinition cube_r13 = yuanbao.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(22, 2).addBox(-0.65F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, 0.2967F, 0.3706F, 0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r14 = yuanbao.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(14, 21).addBox(-0.625F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.025F, 0.2967F, -1.1706F, -0.3927F, 0.0F, 0.0F));

        PartDefinition LeftHand = root.addOrReplaceChild("LeftHand", CubeListBuilder.create().texOffs(0, 14).addBox(-3.5F, -0.5F, -0.75F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.75F, 2.375F, -3.25F));

        PartDefinition gunzi = LeftHand.addOrReplaceChild("gunzi", CubeListBuilder.create().texOffs(12, 9).addBox(-0.35F, -2.5F, -0.1F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 0.0F, -0.5F));

        PartDefinition LeftLeg = root.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(0, 19).addBox(-0.75F, -0.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 5.125F, -2.5F));

        PartDefinition RightLeg = root.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(0, 19).addBox(-0.75F, -0.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 5.125F, -0.5F));

        PartDefinition LeftPP = root.addOrReplaceChild("LeftPP", CubeListBuilder.create().texOffs(8, 18).addBox(0.075F, -1.05F, -1.125F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.675F, 4.575F, -2.425F));

        PartDefinition RightPP = root.addOrReplaceChild("RightPP", CubeListBuilder.create().texOffs(8, 18).addBox(0.075F, -1.05F, -0.925F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.675F, 4.575F, -0.525F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return root;
    }
}