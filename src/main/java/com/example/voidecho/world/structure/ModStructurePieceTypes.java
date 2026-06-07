package com.example.voidecho.world.structure;

import net.minecraft.structure.StructurePieceType;

/**
 * Registers custom structure piece types for programmatic structure generation.
 */
public class ModStructurePieceTypes {

    // In 1.21.1, structure piece types bypass the frozen registry.
    // They're defined as direct functional-interface constants.
    public static final StructurePieceType ALTAR_PIECE = (ctx, nbt) ->
            new ForgottenAltarStructure.AltarPiece(
                    ModStructurePieceTypes.ALTAR_PIECE, nbt.getInt("length"),
                    new net.minecraft.util.math.BlockBox(
                            nbt.getInt("minX"), nbt.getInt("minY"), nbt.getInt("minZ"),
                            nbt.getInt("maxX"), nbt.getInt("maxY"), nbt.getInt("maxZ"))
            );

    public static final StructurePieceType FORTRESS_PIECE = (ctx, nbt) ->
            new VoidFortressStructure.FortressEntrancePiece(
                    ModStructurePieceTypes.FORTRESS_PIECE, nbt.getInt("length"),
                    new net.minecraft.util.math.BlockBox(
                            nbt.getInt("minX"), nbt.getInt("minY"), nbt.getInt("minZ"),
                            nbt.getInt("maxX"), nbt.getInt("maxY"), nbt.getInt("maxZ"))
            );

    public static final StructurePieceType SANCTUM_PIECE = (ctx, nbt) ->
            new EchoSanctumStructure.SanctumPiece(
                    ModStructurePieceTypes.SANCTUM_PIECE, nbt.getInt("length"),
                    new net.minecraft.util.math.BlockBox(
                            nbt.getInt("minX"), nbt.getInt("minY"), nbt.getInt("minZ"),
                            nbt.getInt("maxX"), nbt.getInt("maxY"), nbt.getInt("maxZ"))
            );

    public static void init() {}

    private ModStructurePieceTypes() {}
}
