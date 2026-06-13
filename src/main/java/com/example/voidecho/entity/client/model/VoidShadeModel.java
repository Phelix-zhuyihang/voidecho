package com.example.voidecho.entity.client.model;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.mob.VoidShadeEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;

public class VoidShadeModel extends SinglePartEntityModel<VoidShadeEntity> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of(VoidEcho.MOD_ID, "void_shade"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;

    public VoidShadeModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("body", ModelPartBuilder.create().uv(0, 16)
                .cuboid(-4, 0, -2, 8, 12, 4, Dilation.NONE), ModelTransform.pivot(0, 12, 0));
        root.addChild("head", ModelPartBuilder.create().uv(0, 0)
                .cuboid(-4, -8, -4, 8, 8, 8, Dilation.NONE), ModelTransform.pivot(0, 12, 0));
        root.addChild("left_arm", ModelPartBuilder.create().uv(40, 16)
                .cuboid(-1, -2, -2, 4, 12, 4, Dilation.NONE), ModelTransform.pivot(-5, 14, 0));
        root.addChild("right_arm", ModelPartBuilder.create().uv(40, 16)
                .cuboid(-3, -2, -2, 4, 12, 4, Dilation.NONE), ModelTransform.pivot(5, 14, 0));
        return TexturedModelData.of(data, 64, 64);
    }

    @Override
    public void setAngles(VoidShadeEntity entity, float limbAngle, float limbDistance,
                           float animationProgress, float headYaw, float headPitch) {
        this.head.pitch = headPitch * 0.017453292F;
        this.head.yaw = headYaw * 0.017453292F;
        float walkMag = Math.min(limbDistance * 0.4F, 0.5F);
        this.leftArm.pitch = (float) Math.sin(limbAngle * 0.5F) * walkMag;
        this.rightArm.pitch = (float) Math.sin(limbAngle * 0.5F + Math.PI) * walkMag;
        this.body.pitch = (float) Math.sin(animationProgress * 0.05F) * 0.03F;
    }

    @Override
    public ModelPart getPart() { return this.root; }
}
