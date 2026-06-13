package com.example.voidecho.entity.client.renderer;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.client.model.VoidShadeModel;
import com.example.voidecho.entity.mob.VoidShadeEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class VoidShadeRenderer extends MobEntityRenderer<VoidShadeEntity, VoidShadeModel> {
    private static final Identifier TEXTURE = Identifier.of(VoidEcho.MOD_ID, "textures/entity/void_shade.png");

    public VoidShadeRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new VoidShadeModel(ctx.getPart(VoidShadeModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public Identifier getTexture(VoidShadeEntity entity) { return TEXTURE; }
}
