package com.example.voidecho.entity.client.renderer;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.client.model.EchoWardenModel;
import com.example.voidecho.entity.boss.EchoWardenEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class EchoWardenRenderer extends MobEntityRenderer<EchoWardenEntity, EchoWardenModel> {
    private static final Identifier TEXTURE = Identifier.of(VoidEcho.MOD_ID, "textures/entity/echo_warden.png");
    private static final float SCALE = 1.4f;

    public EchoWardenRenderer(EntityRendererFactory.Context context) {
        super(context, new EchoWardenModel(context.getPart(EchoWardenModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    public Identifier getTexture(EchoWardenEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(EchoWardenEntity entity, net.minecraft.client.util.math.MatrixStack matrices, float tickDelta) {
        matrices.scale(SCALE, SCALE, SCALE);
    }
}
