package com.example.voidecho.entity.client.renderer;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.client.model.VoidStalkerModel;
import com.example.voidecho.entity.boss.VoidStalkerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class VoidStalkerRenderer extends MobEntityRenderer<VoidStalkerEntity, VoidStalkerModel> {
    private static final Identifier TEXTURE = Identifier.of(VoidEcho.MOD_ID, "textures/entity/void_stalker.png");
    private static final float SCALE = 1.2f;

    public VoidStalkerRenderer(EntityRendererFactory.Context context) {
        super(context, new VoidStalkerModel(context.getPart(VoidStalkerModel.LAYER_LOCATION)), 0.6f);
    }

    @Override
    public Identifier getTexture(VoidStalkerEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(VoidStalkerEntity entity, net.minecraft.client.util.math.MatrixStack matrices, float tickDelta) {
        matrices.scale(SCALE, SCALE, SCALE);
    }
}
