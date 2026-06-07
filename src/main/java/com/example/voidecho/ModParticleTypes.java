package com.example.voidecho;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModParticleTypes {
    public static final SimpleParticleType VOID_AMBIENT = register("void_ambient");
    public static final SimpleParticleType VOID_BEAM = register("void_beam");
    public static final SimpleParticleType VOID_BURST = register("void_burst");

    private static SimpleParticleType register(String name) {
        return Registry.register(
                Registries.PARTICLE_TYPE,
                Identifier.of("void_echo", name),
                FabricParticleTypes.simple()
        );
    }

    public static void init() {
        // Static fields are initialised on class load; calling init() forces the load.
    }
}
