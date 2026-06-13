package com.example.voidecho.world.structure;

import com.example.voidecho.VoidEcho;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

/**
 * Registers all custom structures for the Void Echo mod.
 * Structures use a hybrid approach: Jigsaw JSON configs define placement rules,
 * while programmatic piece generators create the actual block layouts.
 */
public class ModStructures {

    // Structure registry keys
    public static final RegistryKey<Structure> FORGOTTEN_ALTAR_KEY =
            RegistryKey.of(RegistryKeys.STRUCTURE, id("forgotten_altar"));

    public static final RegistryKey<Structure> VOID_FORTRESS_KEY =
            RegistryKey.of(RegistryKeys.STRUCTURE, id("void_fortress"));

    public static final RegistryKey<Structure> ECHO_SANCTUM_KEY =
            RegistryKey.of(RegistryKeys.STRUCTURE, id("echo_sanctum"));

    // Structure types
    @SuppressWarnings("unchecked")
    public static final StructureType<ForgottenAltarStructure> FORGOTTEN_ALTAR_TYPE =
            (StructureType<ForgottenAltarStructure>) registerStructureType("forgotten_altar",
                    ForgottenAltarStructure.CODEC);

    @SuppressWarnings("unchecked")
    public static final StructureType<VoidFortressStructure> VOID_FORTRESS_TYPE =
            (StructureType<VoidFortressStructure>) registerStructureType("void_fortress",
                    VoidFortressStructure.CODEC);

    @SuppressWarnings("unchecked")
    public static final StructureType<EchoSanctumStructure> ECHO_SANCTUM_TYPE =
            (StructureType<EchoSanctumStructure>) registerStructureType("echo_sanctum",
                    EchoSanctumStructure.CODEC);

    @SuppressWarnings("unchecked")
    public static final StructureType<VoidRiftCoreStructure> VOID_RIFT_CORE_TYPE =
            (StructureType<VoidRiftCoreStructure>) registerStructureType("void_rift_core",
                    VoidRiftCoreStructure.CODEC);

    private static Identifier id(String path) {
        return Identifier.of(VoidEcho.MOD_ID, path);
    }

    private static <S extends Structure> StructureType<S> registerStructureType(String id, com.mojang.serialization.MapCodec<S> codec) {
        return Registry.register(Registries.STRUCTURE_TYPE, id(id), () -> codec);
    }

    public static void init() {
        VoidEcho.LOGGER.info("Registering Void Echo structures...");
    }

    private ModStructures() {}
}
