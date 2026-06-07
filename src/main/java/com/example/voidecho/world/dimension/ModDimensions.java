package com.example.voidecho.world.dimension;

import com.example.voidecho.VoidEcho;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;

/**
 * Registers the Void's End custom dimension.
 * The dimension JSON definition is in data/void_echo/dimension/voids_end.json.
 * The dimension type JSON is in data/void_echo/dimension_type/voids_end.json.
 */
public class ModDimensions {

    public static final RegistryKey<DimensionOptions> VOIDS_END_KEY =
            RegistryKey.of(RegistryKeys.DIMENSION,
                    Identifier.of(VoidEcho.MOD_ID, "voids_end"));

    public static final RegistryKey<World> VOIDS_END_LEVEL_KEY =
            RegistryKey.of(RegistryKeys.WORLD,
                    Identifier.of(VoidEcho.MOD_ID, "voids_end"));

    public static final RegistryKey<DimensionType> VOIDS_END_DIMENSION_TYPE_KEY =
            RegistryKey.of(RegistryKeys.DIMENSION_TYPE,
                    Identifier.of(VoidEcho.MOD_ID, "voids_end_type"));

    /**
     * Initializes dimension registry keys.
     * The actual dimension is loaded from JSON data files.
     */
    public static void init() {
        VoidEcho.LOGGER.info("Registering Void's End dimension...");
    }

    /**
     * Returns the dimension registry key for teleportation purposes.
     */
    public static RegistryKey<World> getVoidsEndWorldKey() {
        return VOIDS_END_LEVEL_KEY;
    }

    private ModDimensions() {}
}
