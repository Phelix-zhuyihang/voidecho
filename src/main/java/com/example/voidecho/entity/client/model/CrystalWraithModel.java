package com.example.voidecho.entity.client.model;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.mob.CrystalWraithEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;

public class CrystalWraithModel extends SinglePartEntityModel<CrystalWraithEntity> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of(VoidEcho.MOD_ID, "crystal_wraith"), "main"
    );

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public CrystalWraithModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        modelPartData.addChild("body",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-3.0F, -5.0F, -2.0F, 6.0F, 10.0F, 4.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 16.0F, 0.0F));

        modelPartData.addChild("head",
                ModelPartBuilder.create()
                        .uv(16, 0)
                        .cuboid(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new Dilation(0.0F))
                        .uv(28, 12)
                        .cuboid(-2.0F, -2.0F, -5.0F, 4.0F, 4.0F, 2.0F, new Dilation(0.0F)), // Face
                ModelTransform.pivot(0.0F, 11.0F, 0.0F));

        modelPartData.addChild("left_wing",
                ModelPartBuilder.create()
                        .uv(28, 0)
                        .cuboid(0.0F, -4.0F, -1.0F, 10.0F, 8.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(3.0F, 14.0F, 0.0F));

        modelPartData.addChild("right_wing",
                ModelPartBuilder.create()
                        .uv(28, 0)
                        .cuboid(-10.0F, -4.0F, -1.0F, 10.0F, 8.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-3.0F, 14.0F, 0.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(CrystalWraithEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        // Head look
        this.head.pitch = headPitch * 0.017453292F;
        this.head.yaw = headYaw * 0.017453292F;

        // Wing flapping — faster when moving
        float flapSpeed = 0.2F + limbDistance * 0.3F;
        float wingFlap = (float) Math.sin(animationProgress * flapSpeed) * 0.3F;
        this.leftWing.roll = -0.5F + wingFlap;
        this.rightWing.roll = 0.5F - wingFlap;

        // Body sway
        this.body.roll = (float) Math.sin(animationProgress * 0.1F) * 0.05F;

        // Diving animation
        if (entity.isDiving()) {
            this.body.pitch = 0.8F;
            this.leftWing.roll = -0.8F;
            this.rightWing.roll = 0.8F;
        }
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }
}
