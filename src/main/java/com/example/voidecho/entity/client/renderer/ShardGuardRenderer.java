package com.example.voidecho.entity.client.renderer;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.client.model.ShardGuardModel;
import com.example.voidecho.entity.mob.ShardGuardEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class ShardGuardRenderer extends MobEntityRenderer<ShardGuardEntity, ShardGuardModel> {
    private static final Identifier TEXTURE = Identifier.of(VoidEcho.MOD_ID, "textures/entity/shard_guard.png");

    public ShardGuardRenderer(EntityRendererFactory.Context context) {
        super(context, new ShardGuardModel(context.getPart(ShardGuardModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public Identifier getTexture(ShardGuardEntity entity) {
        return TEXTURE;
    }
}
