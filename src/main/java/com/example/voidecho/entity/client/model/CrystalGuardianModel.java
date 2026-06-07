package com.example.voidecho.entity.client.model;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.mob.CrystalGuardianEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;

public class CrystalGuardianModel extends SinglePartEntityModel<CrystalGuardianEntity> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of(VoidEcho.MOD_ID, "crystal_guardian"), "main"
    );

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public CrystalGuardianModel(ModelPart root) {
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
                        .cuboid(-4.0F, -6.0F, -3.0F, 8.0F, 12.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 14.0F, 0.0F));

        modelPartData.addChild("head",
                ModelPartBuilder.create()
                        .uv(22, 0)
                        .cuboid(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new Dilation(0.0F))
                        .uv(28, 12)
                        .cuboid(-2.0F, -2.0F, -5.0F, 4.0F, 4.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 8.0F, 0.0F));

        modelPartData.addChild("left_wing",
                ModelPartBuilder.create()
                        .uv(28, 18)
                        .cuboid(0.0F, -4.0F, -1.0F, 12.0F, 8.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(4.0F, 12.0F, 0.0F));

        modelPartData.addChild("right_wing",
                ModelPartBuilder.create()
                        .uv(28, 18)
                        .cuboid(-12.0F, -4.0F, -1.0F, 12.0F, 8.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-4.0F, 12.0F, 0.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(CrystalGuardianEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.head.pitch = headPitch * 0.017453292F;
        this.head.yaw = headYaw * 0.017453292F;

        float wingFlap = (float) Math.sin(animationProgress * 0.25F) * 0.4F;
        this.leftWing.roll = -0.4F + wingFlap;
        this.rightWing.roll = 0.4F - wingFlap;

        this.body.roll = (float) Math.sin(animationProgress * 0.1F) * 0.05F;
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }
}
