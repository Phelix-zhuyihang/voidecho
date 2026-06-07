package com.example.voidecho.entity.client.renderer;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.client.model.CrystalGuardianModel;
import com.example.voidecho.entity.mob.CrystalGuardianEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class CrystalGuardianRenderer extends MobEntityRenderer<CrystalGuardianEntity, CrystalGuardianModel> {
    private static final Identifier TEXTURE = Identifier.of(VoidEcho.MOD_ID, "textures/entity/crystal_guardian.png");

    public CrystalGuardianRenderer(EntityRendererFactory.Context context) {
        super(context, new CrystalGuardianModel(context.getPart(CrystalGuardianModel.LAYER_LOCATION)), 0.3f);
    }

    @Override
    public Identifier getTexture(CrystalGuardianEntity entity) {
        return TEXTURE;
    }

    @Override
    protected RenderLayer getRenderLayer(CrystalGuardianEntity entity, boolean showBody, boolean translucent, boolean showOutline) {
        return RenderLayer.getEntityTranslucent(getTexture(entity));
    }
}
