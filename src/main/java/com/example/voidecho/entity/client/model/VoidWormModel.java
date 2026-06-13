package com.example.voidecho.entity.client.model;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.mob.VoidWormEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;

public class VoidWormModel extends SinglePartEntityModel<VoidWormEntity> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of(VoidEcho.MOD_ID, "void_worm"), "main"
    );

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body1;
    private final ModelPart body2;
    private final ModelPart tail;

    public VoidWormModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body1 = root.getChild("body1");
        this.body2 = root.getChild("body2");
        this.tail = root.getChild("tail");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        modelPartData.addChild("head",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-3.0F, -2.0F, -4.0F, 6.0F, 4.0F, 6.0F, new Dilation(0.0F))
                        .uv(0, 10)
                        .cuboid(-2.0F, -1.0F, -5.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F)), // Snout
                ModelTransform.pivot(0.0F, 22.0F, 0.0F));

        modelPartData.addChild("body1",
                ModelPartBuilder.create()
                        .uv(0, 15)
                        .cuboid(-4.0F, -2.0F, 0.0F, 8.0F, 4.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 22.0F, 2.0F));

        modelPartData.addChild("body2",
                ModelPartBuilder.create()
                        .uv(0, 20)
                        .cuboid(-3.5F, -2.0F, 0.0F, 7.0F, 4.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 22.0F, 6.0F));

        modelPartData.addChild("tail",
                ModelPartBuilder.create()
                        .uv(0, 30)
                        .cuboid(-2.5F, -1.5F, 0.0F, 5.0F, 3.0F, 6.0F, new Dilation(0.0F))
                        .uv(0, 39)
                        .cuboid(-1.5F, -1.0F, 6.0F, 3.0F, 2.0F, 3.0F, new Dilation(0.0F)), // Tail tip
                ModelTransform.pivot(0.0F, 22.0F, 10.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(VoidWormEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        // Head rotation based on yaw/pitch
        this.head.pitch = headPitch * 0.017453292F;
        this.head.yaw = headYaw * 0.017453292F;

        // Body undulation — intensity scales with movement, stops when idle
        float walkMag = Math.min(limbDistance * 0.5F, 0.6F);
        float wave = limbAngle * 0.5F;
        this.body1.yaw = (float) Math.sin(wave) * 0.1F * walkMag / 0.4F;
        this.body2.yaw = (float) Math.sin(wave + 0.5F) * 0.15F * walkMag / 0.4F;
        this.tail.yaw = (float) Math.sin(wave + 1.0F) * 0.2F * walkMag / 0.4F;

        // Idle movement
        float idle = animationProgress * 0.05F;
        this.body1.pitch = (float) Math.sin(idle) * 0.05F;
        this.body2.pitch = (float) Math.sin(idle + 1.0F) * 0.08F;
        this.tail.pitch = (float) Math.sin(idle + 2.0F) * 0.1F;
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }
}
