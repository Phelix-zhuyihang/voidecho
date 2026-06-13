package com.example.voidecho.entity.client;

import com.example.voidecho.entity.ModEntities;
import com.example.voidecho.entity.client.model.*;
import com.example.voidecho.entity.client.renderer.*;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class ModEntityRenderers {

    public static void register() {
        // --- Register model layers ---
        EntityModelLayerRegistry.registerModelLayer(
                VoidWormModel.LAYER_LOCATION, VoidWormModel::getTexturedModelData
        );
        EntityModelLayerRegistry.registerModelLayer(
                CrystalWraithModel.LAYER_LOCATION, CrystalWraithModel::getTexturedModelData
        );
        EntityModelLayerRegistry.registerModelLayer(
                ShardGuardModel.LAYER_LOCATION, ShardGuardModel::getTexturedModelData
        );
        EntityModelLayerRegistry.registerModelLayer(
                VoidStalkerModel.LAYER_LOCATION, VoidStalkerModel::getTexturedModelData
        );
        EntityModelLayerRegistry.registerModelLayer(
                EchoWardenModel.LAYER_LOCATION, EchoWardenModel::getTexturedModelData
        );
        EntityModelLayerRegistry.registerModelLayer(
                CrystalSpriteModel.LAYER_LOCATION, CrystalSpriteModel::getTexturedModelData
        );
        EntityModelLayerRegistry.registerModelLayer(
                CrystalGuardianModel.LAYER_LOCATION, CrystalGuardianModel::getTexturedModelData
        );
        EntityModelLayerRegistry.registerModelLayer(
                VoidShadeModel.LAYER_LOCATION, VoidShadeModel::getTexturedModelData
        );

        // --- Register renderers ---
        EntityRendererRegistry.register(ModEntities.VOID_WORM, VoidWormRenderer::new);
        EntityRendererRegistry.register(ModEntities.CRYSTAL_WRAITH, CrystalWraithRenderer::new);
        EntityRendererRegistry.register(ModEntities.SHARD_GUARD, ShardGuardRenderer::new);
        EntityRendererRegistry.register(ModEntities.VOID_STALKER, VoidStalkerRenderer::new);
        EntityRendererRegistry.register(ModEntities.ECHO_WARDEN, EchoWardenRenderer::new);
        EntityRendererRegistry.register(ModEntities.CRYSTAL_SPRITE, CrystalSpriteRenderer::new);
        EntityRendererRegistry.register(ModEntities.CRYSTAL_GUARDIAN, CrystalGuardianRenderer::new);
        EntityRendererRegistry.register(ModEntities.VOID_SHADE, VoidShadeRenderer::new);
    }

    private ModEntityRenderers() {}
}
