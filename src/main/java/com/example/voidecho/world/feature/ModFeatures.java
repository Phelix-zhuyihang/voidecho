package com.example.voidecho.world.feature;

import com.example.voidecho.VoidEcho;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;

/**
 * Registers custom features for world generation.
 * Feature JSON definitions are in data/void_echo/worldgen/configured_feature/ and
 * data/void_echo/worldgen/placed_feature/.
 * This class provides registry keys and bootstrap methods.
 */
public class ModFeatures {

    // Configured feature keys
    public static final RegistryKey<ConfiguredFeature<?, ?>> CRYSTAL_ORE_VEIN =
            RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE,
                    Identifier.of(VoidEcho.MOD_ID, "crystal_ore_vein"));

    public static final RegistryKey<ConfiguredFeature<?, ?>> VOID_GRASS_PATCH =
            RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE,
                    Identifier.of(VoidEcho.MOD_ID, "void_grass_patch"));

    // Placed feature keys
    public static final RegistryKey<PlacedFeature> CRYSTAL_ORE_PLACED =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE,
                    Identifier.of(VoidEcho.MOD_ID, "crystal_ore_placed"));

    public static final RegistryKey<PlacedFeature> CRYSTAL_ORE_OVERWORLD_PLACED =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE,
                    Identifier.of(VoidEcho.MOD_ID, "crystal_ore_overworld_placed"));

    public static final RegistryKey<PlacedFeature> VOID_GRASS_PATCH_PLACED =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE,
                    Identifier.of(VoidEcho.MOD_ID, "void_grass_patch_placed"));

    public static void init() {
        VoidEcho.LOGGER.info("Registering Void Echo features...");
    }

    /**
     * Bootstrap configured features for data generation.
     */
    public static void bootstrapConfigured(Registerable<ConfiguredFeature<?, ?>> context) {
        // Configured features are loaded from JSON files.
        // This is a hook point for programmatic feature generation if needed.
    }

    /**
     * Bootstrap placed features for data generation.
     */
    public static void bootstrapPlaced(Registerable<PlacedFeature> context) {
        RegistryEntryLookup<ConfiguredFeature<?, ?>> registryLookup =
                context.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        // Placed features are loaded from JSON files.
        // This is a hook point for programmatic placement if needed.
    }

    private ModFeatures() {}
}
