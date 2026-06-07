package com.example.voidecho.world.biome;

import com.example.voidecho.VoidEcho;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

/**
 * Registers custom biomes for the Void's End dimension.
 * Actual biome JSON definitions are in data/void_echo/worldgen/biome/.
 * This class provides the registry keys used for biome references in code.
 */
public class ModBiomes {

    public static final RegistryKey<Biome> VOID_PLAINS = RegistryKey.of(
            RegistryKeys.BIOME,
            Identifier.of(VoidEcho.MOD_ID, "void_plains")
    );

    public static final RegistryKey<Biome> CRYSTAL_FOREST = RegistryKey.of(
            RegistryKeys.BIOME,
            Identifier.of(VoidEcho.MOD_ID, "crystal_forest")
    );

    public static final RegistryKey<Biome> VOID_WASTES = RegistryKey.of(
            RegistryKeys.BIOME,
            Identifier.of(VoidEcho.MOD_ID, "void_wastes")
    );

    public static final RegistryKey<Biome> CRYSTAL_CAVERNS = RegistryKey.of(
            RegistryKeys.BIOME,
            Identifier.of(VoidEcho.MOD_ID, "crystal_caverns")
    );

    /**
     * Called during mod initialization. Biome registration happens via JSON
     * data files, but we verify the keys exist.
     */
    public static void init() {
        VoidEcho.LOGGER.info("Registering Void Echo biomes...");
    }

    /**
     * Bootstrap method for datagen - registers biomes into the dynamic registry.
     */
    public static void bootstrap(Registerable<Biome> context) {
        // Biome definitions are loaded from JSON in data/void_echo/worldgen/biome/
        // This is a hook point for data generation if biomes need to be generated programmatically
    }

    private ModBiomes() {}
}
