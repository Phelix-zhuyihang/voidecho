package com.example.voidecho.entity.client.renderer;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.VoidCrabEntity;
import com.example.voidecho.entity.client.model.VoidCrabModel;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class VoidCrabRenderer extends MobEntityRenderer<VoidCrabEntity, VoidCrabModel> {
    private static final Identifier TEXTURE = Identifier.of(VoidEcho.MOD_ID, "textures/entity/void_crab.png");

    public VoidCrabRenderer(EntityRendererFactory.Context context) {
        super(context, new VoidCrabModel(context.getPart(VoidCrabModel.LAYER_LOCATION)), 0.3f);
    }

    @Override
    public Identifier getTexture(VoidCrabEntity entity) {
        return TEXTURE;
    }
}
