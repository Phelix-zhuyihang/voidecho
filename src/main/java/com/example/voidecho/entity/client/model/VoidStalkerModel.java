package com.example.voidecho.entity.client.model;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.boss.VoidStalkerEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;

public class VoidStalkerModel extends SinglePartEntityModel<VoidStalkerEntity> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of(VoidEcho.MOD_ID, "void_stalker"), "main"
    );

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart cape;

    public VoidStalkerModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.cape = root.getChild("cape");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        // Head
        modelPartData.addChild("head",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.0F))
                        .uv(24, 0)
                        .cuboid(-3.0F, -3.0F, -6.0F, 6.0F, 4.0F, 2.0F, new Dilation(0.0F)), // Face plate
                ModelTransform.pivot(0.0F, -6.0F, 0.0F));

        // Body - tall and lean
        modelPartData.addChild("body",
                ModelPartBuilder.create()
                        .uv(16, 16)
                        .cuboid(-4.0F, 0.0F, -2.5F, 8.0F, 18.0F, 5.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, -6.0F, 0.0F));

        // Left arm
        modelPartData.addChild("left_arm",
                ModelPartBuilder.create()
                        .uv(32, 16)
                        .cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 20.0F, 4.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-5.0F, -4.0F, 0.0F));

        // Right arm
        modelPartData.addChild("right_arm",
                ModelPartBuilder.create()
                        .uv(32, 16)
                        .cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 20.0F, 4.0F, new Dilation(0.0F)),
                ModelTransform.pivot(5.0F, -4.0F, 0.0F));

        // Left leg
        modelPartData.addChild("left_leg",
                ModelPartBuilder.create()
                        .uv(0, 16)
                        .cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-2.0F, 12.0F, 0.0F));

        // Right leg
        modelPartData.addChild("right_leg",
                ModelPartBuilder.create()
                        .uv(0, 16)
                        .cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F, new Dilation(0.0F)),
                ModelTransform.pivot(2.0F, 12.0F, 0.0F));

        // Cape/cloak
        modelPartData.addChild("cape",
                ModelPartBuilder.create()
                        .uv(48, 0)
                        .cuboid(-5.0F, 0.0F, -1.0F, 10.0F, 24.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, -6.0F, 3.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(VoidStalkerEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        // Head tracking
        this.head.pitch = headPitch * 0.017453292F;
        this.head.yaw = headYaw * 0.017453292F;

        // Walking animation — scale with limbDistance so limbs don't swing when still
        float walkMag = Math.min(limbDistance * 0.5F, 0.6F);
        float walkCycle = limbAngle * 0.5F;
        this.leftLeg.pitch = (float) Math.sin(walkCycle) * walkMag;
        this.rightLeg.pitch = (float) Math.sin(walkCycle + Math.PI) * walkMag;

        this.leftArm.pitch = (float) Math.sin(walkCycle + Math.PI) * walkMag * 0.67F;
        this.rightArm.pitch = (float) Math.sin(walkCycle) * walkMag * 0.67F;

        // Cape sway
        float capeSway = (float) Math.sin(animationProgress * 0.1F) * 0.1F;
        this.cape.pitch = 0.1F + capeSway;

        // Body bob
        this.body.pitch = (float) Math.sin(animationProgress * 0.08F) * 0.02F;
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }
}
