package com.example.voidecho.entity.client.model;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.mob.CrystalSpriteEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;

public class CrystalSpriteModel extends SinglePartEntityModel<CrystalSpriteEntity> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(
            Identifier.of(VoidEcho.MOD_ID, "crystal_sprite"), "main"
    );

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public CrystalSpriteModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        modelPartData.addChild("body",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-3.0F, -4.0F, -3.0F, 6.0F, 6.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 20.0F, 0.0F));

        modelPartData.addChild("left_wing",
                ModelPartBuilder.create()
                        .uv(0, 12)
                        .cuboid(0.0F, -2.0F, -1.0F, 8.0F, 4.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(3.0F, 18.0F, 0.0F));

        modelPartData.addChild("right_wing",
                ModelPartBuilder.create()
                        .uv(0, 12)
                        .cuboid(-8.0F, -2.0F, -1.0F, 8.0F, 4.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-3.0F, 18.0F, 0.0F));

        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public void setAngles(CrystalSpriteEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        // Wing flapping — faster when moving, but always a gentle idle flutter
        float moveScale = 0.5F + Math.min(limbDistance * 1.0F, 0.5F);
        float wingFlap = (float) Math.sin(animationProgress * 0.3F) * 0.4F * moveScale;
        this.leftWing.roll = -0.3F + wingFlap;
        this.rightWing.roll = 0.3F - wingFlap;
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }
}
