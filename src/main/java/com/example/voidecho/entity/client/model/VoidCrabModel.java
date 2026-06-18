package com.example.voidecho.entity.client.model;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.VoidCrabEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class VoidCrabModel extends EntityModel<VoidCrabEntity> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of(VoidEcho.MOD_ID, "void_crab"), "main"
    );

    private final ModelPart body;
    private final ModelPart legFrontLeft;
    private final ModelPart legFrontRight;
    private final ModelPart legBackLeft;
    private final ModelPart legBackRight;

    public VoidCrabModel(ModelPart root) {
        this.body = root.getChild("body");
        this.legFrontLeft = root.getChild("leg_front_left");
        this.legFrontRight = root.getChild("leg_front_right");
        this.legBackLeft = root.getChild("leg_back_left");
        this.legBackRight = root.getChild("leg_back_right");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        // Body: 8x4x6 centered
        root.addChild("body", ModelPartBuilder.create()
                .uv(0, 0).cuboid(-4.0f, -2.0f, -3.0f, 8.0f, 4.0f, 6.0f), ModelTransform.pivot(0.0f, 22.0f, 0.0f));
        // Legs: 2x2x2 pillars
        root.addChild("leg_front_left", ModelPartBuilder.create()
                .uv(0, 10).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 2.0f, 2.0f),
                ModelTransform.pivot(3.0f, 22.0f, -2.0f));
        root.addChild("leg_front_right", ModelPartBuilder.create()
                .uv(0, 10).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 2.0f, 2.0f),
                ModelTransform.pivot(-3.0f, 22.0f, -2.0f));
        root.addChild("leg_back_left", ModelPartBuilder.create()
                .uv(0, 10).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 2.0f, 2.0f),
                ModelTransform.pivot(3.0f, 22.0f, 1.0f));
        root.addChild("leg_back_right", ModelPartBuilder.create()
                .uv(0, 10).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 2.0f, 2.0f),
                ModelTransform.pivot(-3.0f, 22.0f, 1.0f));
        return TexturedModelData.of(data, 32, 16);
    }

    @Override
    public void setAngles(VoidCrabEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        body.render(matrices, vertices, light, overlay, color);
        legFrontLeft.render(matrices, vertices, light, overlay, color);
        legFrontRight.render(matrices, vertices, light, overlay, color);
        legBackLeft.render(matrices, vertices, light, overlay, color);
        legBackRight.render(matrices, vertices, light, overlay, color);
    }
}
