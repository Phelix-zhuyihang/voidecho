package com.example.voidecho.entity.client.renderer;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.client.model.CrystalSpriteModel;
import com.example.voidecho.entity.mob.CrystalSpriteEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class CrystalSpriteRenderer extends MobEntityRenderer<CrystalSpriteEntity, CrystalSpriteModel> {
    private static final Identifier TEXTURE = Identifier.of(VoidEcho.MOD_ID, "textures/entity/crystal_sprite.png");

    public CrystalSpriteRenderer(EntityRendererFactory.Context context) {
        super(context, new CrystalSpriteModel(context.getPart(CrystalSpriteModel.LAYER_LOCATION)), 0.2f);
    }

    @Override
    public Identifier getTexture(CrystalSpriteEntity entity) {
        return TEXTURE;
    }

    @Override
    protected RenderLayer getRenderLayer(CrystalSpriteEntity entity, boolean showBody, boolean translucent, boolean showOutline) {
        return RenderLayer.getEntityTranslucent(getTexture(entity));
    }
}
