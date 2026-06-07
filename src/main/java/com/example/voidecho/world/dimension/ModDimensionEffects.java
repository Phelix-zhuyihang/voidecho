package com.example.voidecho.world.dimension;

import com.example.voidecho.VoidEcho;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Registers custom dimension rendering effects for the Void's End dimension.
 * <p>
 * Uses Fabric API's {@link DimensionRenderingRegistry} to register a custom
 * {@link DimensionEffects} that gives the Void's End a dark purple sky with
 * no clouds, no sun/moon/stars, and a custom fog color.
 */
@Environment(EnvType.CLIENT)
public final class ModDimensionEffects {

    private static final Identifier VOIDS_END_ID = Identifier.of(VoidEcho.MOD_ID, "voids_end");

    private ModDimensionEffects() {}

    /**
     * Called from {@code VoidEchoClient.onInitializeClient()}.
     */
    public static void registerEffects() {
        DimensionRenderingRegistry.registerDimensionEffects(
                VOIDS_END_ID,
                new VoidDimensionEffects()
        );
    }

    /**
     * Custom {@link DimensionEffects} that produces a dark, alien sky for the Void's End.
     */
    private static class VoidDimensionEffects extends DimensionEffects {
        private static final Vec3d VOID_FOG_COLOR = new Vec3d(0.05, 0.0, 0.1);
        private static final Vec3d VOID_SKY_COLOR = new Vec3d(0.02, 0.0, 0.05);

        VoidDimensionEffects() {
            super(Float.NaN, false, SkyType.END, false, false);
        }

        @Override
        public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
            // Very dark purple fog
            return VOID_FOG_COLOR;
        }

        @Override
        public boolean useThickFog(int camX, int camY) {
            // Light fog closer to the player for atmosphere
            return camY < 32;
        }

        @Override
        public float[] getFogColorOverride(float skyAngle, float tickDelta) {
            // Return null to prevent vanilla fog color — we supply our own
            return null;
        }

        @Override
        public boolean isDarkened() {
            return true;
        }

        @Override
        public boolean isAlternateSkyColor() {
            return false;
        }

        @Override
        public boolean shouldBrightenLighting() {
            return false;
        }

        @Override
        public float getCloudsHeight() {
            return Float.NaN; // No clouds
        }
    }
}
