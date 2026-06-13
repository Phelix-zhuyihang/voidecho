package com.example.voidecho.world.structure;

import com.example.voidecho.VoidEcho;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;

/**
 * Registers custom structure piece types for programmatic structure generation.
 */
public class ModStructurePieceTypes {

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

    public static void init() {
        Registry.register(Registries.STRUCTURE_PIECE,
                Identifier.of(VoidEcho.MOD_ID, "altar_piece"), ALTAR_PIECE);
        Registry.register(Registries.STRUCTURE_PIECE,
                Identifier.of(VoidEcho.MOD_ID, "fortress_piece"), FORTRESS_PIECE);
        Registry.register(Registries.STRUCTURE_PIECE,
                Identifier.of(VoidEcho.MOD_ID, "sanctum_piece"), SANCTUM_PIECE);
    }

    private ModStructurePieceTypes() {}
}
