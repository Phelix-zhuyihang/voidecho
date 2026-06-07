package com.example.voidecho.entity.client.model;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.mob.ShardGuardEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;

public class ShardGuardModel extends SinglePartEntityModel<ShardGuardEntity> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of(VoidEcho.MOD_ID, "shard_guard"), "main"
    );

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public ShardGuardModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        // Head - larger for golem
        modelPartData.addChild("head",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.0F))
                        .uv(24, 0)
                        .cuboid(-2.0F, -4.0F, -6.0F, 4.0F, 4.0F, 2.0F, new Dilation(0.0F)), // Snout/jaw
                ModelTransform.pivot(0.0F, 6.0F, 0.0F));

        // Body - thick golem torso
        modelPartData.addChild("body",
                ModelPartBuilder.create()
                        .uv(16, 16)
                        .cuboid(-5.0F, 0.0F, -3.0F, 10.0F, 14.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 6.0F, 0.0F));

        // Left arm
        modelPartData.addChild("left_arm",
                ModelPartBuilder.create()
                        .uv(36, 16)
                        .cuboid(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-6.0F, 8.0F, 0.0F));

        // Right arm
        modelPartData.addChild("right_arm",
                ModelPartBuilder.create()
                        .uv(36, 16)
                        .cuboid(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.pivot(6.0F, 8.0F, 0.0F));

        // Left leg
        modelPartData.addChild("left_leg",
                ModelPartBuilder.create()
                        .uv(0, 20)
                        .cuboid(-3.0F, 0.0F, -3.0F, 6.0F, 10.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-3.0F, 20.0F, 0.0F));

        // Right leg
        modelPartData.addChild("right_leg",
                ModelPartBuilder.create()
                        .uv(0, 20)
                        .cuboid(-3.0F, 0.0F, -3.0F, 6.0F, 10.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.pivot(3.0F, 20.0F, 0.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(ShardGuardEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        // Head rotation
        this.head.pitch = headPitch * 0.017453292F;
        this.head.yaw = headYaw * 0.017453292F;

        // Walking animation — scale with limbDistance so limbs don't swing when still
        float walkMag = Math.min(limbDistance * 0.5F, 0.6F);
        float walkSpeed = limbAngle * 0.5F;
        this.leftLeg.pitch = (float) Math.sin(walkSpeed) * walkMag;
        this.rightLeg.pitch = (float) Math.sin(walkSpeed + Math.PI) * walkMag;

        this.leftArm.pitch = (float) Math.sin(walkSpeed + Math.PI) * walkMag * 0.75F;
        this.rightArm.pitch = (float) Math.sin(walkSpeed) * walkMag * 0.75F;

        // Idle sway
        float idle = animationProgress * 0.05F;
        this.body.pitch = (float) Math.sin(idle) * 0.02F;
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }
}
