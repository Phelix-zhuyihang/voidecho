package com.example.voidecho.entity.client.renderer;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.client.model.CrystalWraithModel;
import com.example.voidecho.entity.mob.CrystalWraithEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class CrystalWraithRenderer extends MobEntityRenderer<CrystalWraithEntity, CrystalWraithModel> {
    private static final Identifier TEXTURE = Identifier.of(VoidEcho.MOD_ID, "textures/entity/crystal_wraith.png");

    public CrystalWraithRenderer(EntityRendererFactory.Context context) {
        super(context, new CrystalWraithModel(context.getPart(CrystalWraithModel.LAYER_LOCATION)), 0.4f);
    }

    @Override
    public Identifier getTexture(CrystalWraithEntity entity) {
        return TEXTURE;
    }

    @Override
    protected RenderLayer getRenderLayer(CrystalWraithEntity entity, boolean showBody, boolean translucent, boolean showOutline) {
        // Use translucent rendering for ghostly effect
        return RenderLayer.getEntityTranslucent(getTexture(entity));
    }
}
