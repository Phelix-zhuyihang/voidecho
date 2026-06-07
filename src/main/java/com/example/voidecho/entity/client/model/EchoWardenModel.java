package com.example.voidecho.entity.client.model;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.boss.EchoWardenEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;

public class EchoWardenModel extends SinglePartEntityModel<EchoWardenEntity> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of(VoidEcho.MOD_ID, "echo_warden"), "main"
    );

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart shoulderPads;

    public EchoWardenModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.shoulderPads = root.getChild("shoulder_pads");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        // Head - broad and imposing
        modelPartData.addChild("head",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F, new Dilation(0.0F))
                        .uv(30, 0)
                        .cuboid(-4.0F, -4.0F, -7.0F, 8.0F, 6.0F, 2.0F, new Dilation(0.0F)), // Face mask
                ModelTransform.pivot(0.0F, -8.0F, 0.0F));

        // Body - massive torso
        modelPartData.addChild("body",
                ModelPartBuilder.create()
                        .uv(16, 20)
                        .cuboid(-5.0F, 0.0F, -3.0F, 10.0F, 20.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, -8.0F, 0.0F));

        // Shoulder pads
        modelPartData.addChild("shoulder_pads",
                ModelPartBuilder.create()
                        .uv(48, 20)
                        .cuboid(-8.0F, -2.0F, -3.0F, 16.0F, 6.0F, 6.0F, new Dilation(0.25F)),
                ModelTransform.pivot(0.0F, -6.0F, 0.0F));

        // Left arm
        modelPartData.addChild("left_arm",
                ModelPartBuilder.create()
                        .uv(36, 16)
                        .cuboid(-3.5F, -2.0F, -3.0F, 6.0F, 22.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-6.0F, -6.0F, 0.0F));

        // Right arm
        modelPartData.addChild("right_arm",
                ModelPartBuilder.create()
                        .uv(36, 16)
                        .cuboid(-2.5F, -2.0F, -3.0F, 6.0F, 22.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.pivot(6.0F, -6.0F, 0.0F));

        // Left leg
        modelPartData.addChild("left_leg",
                ModelPartBuilder.create()
                        .uv(0, 20)
                        .cuboid(-3.0F, 0.0F, -3.0F, 6.0F, 16.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-3.0F, 12.0F, 0.0F));

        // Right leg
        modelPartData.addChild("right_leg",
                ModelPartBuilder.create()
                        .uv(0, 20)
                        .cuboid(-3.0F, 0.0F, -3.0F, 6.0F, 16.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.pivot(3.0F, 12.0F, 0.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(EchoWardenEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        // Head tracking
        this.head.pitch = headPitch * 0.017453292F;
        this.head.yaw = headYaw * 0.017453292F;

        // Walk animation
        float walkCycle = limbAngle * 0.4F;
        float walkMag = Math.min(limbDistance * 0.5F, 0.6F);
        this.leftLeg.pitch = (float) Math.sin(walkCycle) * walkMag;
        this.rightLeg.pitch = (float) Math.sin(walkCycle + Math.PI) * walkMag;

        // Arm swing
        this.leftArm.pitch = (float) Math.sin(walkCycle + Math.PI) * walkMag * 0.5F;
        this.rightArm.pitch = (float) Math.sin(walkCycle) * walkMag * 0.5F;

        // Idle animation - slow breathing
        float breathe = (float) Math.sin(animationProgress * 0.05F) * 0.03F;
        this.body.pitch = breathe;
        this.shoulderPads.pitch = breathe;

        // Subtle arm idle sway
        this.leftArm.roll = -0.05F;
        this.rightArm.roll = 0.05F;
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }
}
