package com.example.voidecho;

import com.example.voidecho.block.ModBlocks;
import com.example.voidecho.entity.ModEntities;
import com.example.voidecho.entity.client.ModEntityRenderers;
import com.example.voidecho.world.dimension.ModDimensionEffects;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class VoidEchoClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // ---- Entity Renderers & Model Layers (delegates to ModEntityRenderers) ----
        ModEntityRenderers.register();

        // VoidBolt projectile -- render as a flying item (purple crystal texture)
        EntityRendererRegistry.register(ModEntities.VOID_BOLT, FlyingItemEntityRenderer::new);

        // ---- Particle Factories ----
        ParticleFactoryRegistry.getInstance().register(
                ModParticleTypes.VOID_AMBIENT, VoidAmbientParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(
                ModParticleTypes.VOID_BEAM, VoidBeamParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(
                ModParticleTypes.VOID_BURST, VoidBurstParticle.Factory::new);

        // ---- Block Render Layers (cutout for transparent / non-opaque blocks) ----
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CRYSTAL_ORE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DEEPSLATE_CRYSTAL_ORE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.VOID_GRASS_BLOCK, RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CRYSTAL_BLOCK, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.ECHO_ALTAR, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CRYSTAL_BLOOM, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.VOID_FORGE, RenderLayer.getCutout());

        // ---- Dimension Effects (voids_end sky / fog) ----
        ModDimensionEffects.registerEffects();

        // ---- Forge Upgrade Tooltip ----
        com.example.voidecho.client.ForgeUpgradeTooltip.register();
    }
}
