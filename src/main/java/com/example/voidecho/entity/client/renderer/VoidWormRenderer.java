package com.example.voidecho.entity.client.renderer;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.client.model.VoidWormModel;
import com.example.voidecho.entity.mob.VoidWormEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class VoidWormRenderer extends MobEntityRenderer<VoidWormEntity, VoidWormModel> {
    private static final Identifier TEXTURE = Identifier.of(VoidEcho.MOD_ID, "textures/entity/void_worm.png");

    public VoidWormRenderer(EntityRendererFactory.Context context) {
        super(context, new VoidWormModel(context.getPart(VoidWormModel.LAYER_LOCATION)), 0.3f);
    }

    @Override
    public Identifier getTexture(VoidWormEntity entity) {
        return TEXTURE;
    }
}
