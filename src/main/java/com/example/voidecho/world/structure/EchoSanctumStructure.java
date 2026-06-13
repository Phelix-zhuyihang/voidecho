package com.example.voidecho.world.structure;

import com.example.voidecho.block.ModBlocks;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

/**
 * Echo Sanctum - a rare structure in the crystal_forest biome of the Void's End.
 * A small temple with an Echo Altar at the center for summoning the Echo Warden.
 */
public class EchoSanctumStructure extends Structure {

    public static final MapCodec<EchoSanctumStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(configCodecBuilder(instance))
                    .apply(instance, EchoSanctumStructure::new));

    public EchoSanctumStructure(Config config) {
        super(config);
    }

    @Override
    protected Optional<StructurePosition> getStructurePosition(Context context) {
        return getStructurePosition(context, Heightmap.Type.WORLD_SURFACE_WG, collector -> {
            ChunkPos chunkPos = context.chunkPos();
            BlockPos startPos = new BlockPos(chunkPos.getStartX(), 90, chunkPos.getStartZ());
            collector.addPiece(new SanctumPiece(startPos));
        });
    }

    @Override
    public StructureType<?> getType() {
        return ModStructures.ECHO_SANCTUM_TYPE;
    }

    /**
     * Generates the Echo Sanctum: a small temple with floating crystal decoration,
     * central echo altar, and rich loot chests.
     */
    public static class SanctumPiece extends StructurePiece {
        public SanctumPiece(BlockPos startPos) {
            super(ModStructurePieceTypes.SANCTUM_PIECE, 0,
                    new BlockBox(startPos.getX() - 6, startPos.getY() - 60, startPos.getZ() - 6,
                            startPos.getX() + 6, startPos.getY() + 25, startPos.getZ() + 6));
        }

        public SanctumPiece(StructurePieceType type, int length, BlockBox box) {
            super(type, length, box);
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
            nbt.putInt("length", 0);
            nbt.putInt("minX", this.boundingBox.getMinX());
            nbt.putInt("minY", this.boundingBox.getMinY());
            nbt.putInt("minZ", this.boundingBox.getMinZ());
            nbt.putInt("maxX", this.boundingBox.getMaxX());
            nbt.putInt("maxY", this.boundingBox.getMaxY());
            nbt.putInt("maxZ", this.boundingBox.getMaxZ());
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor,
                             ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox,
                             ChunkPos chunkPos, BlockPos pivot) {
            BlockPos center = new BlockPos(
                    boundingBox.getMinX() + 6,
                    boundingBox.getMinY(),
                    boundingBox.getMinZ() + 6);

            int surfaceY = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, center.getX(), center.getZ());

            // Main platform - 5x5 crystal block base
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    int dist = x * x + z * z;
                    if (dist <= 20) {
                        BlockPos p = new BlockPos(center.getX() + x, surfaceY, center.getZ() + z);
                        if (chunkBox.contains(p)) {
                            // Patterned floor
                            if (dist <= 4) {
                                world.setBlockState(p, ModBlocks.CRYSTAL_BLOCK.getDefaultState(), 2);
                            } else if (dist <= 12) {
                                world.setBlockState(p, ModBlocks.VOID_STONE_BRICKS.getDefaultState(), 2);
                            } else {
                                world.setBlockState(p,
                                        random.nextFloat() < 0.3f ?
                                                ModBlocks.CRACKED_VOID_STONE_BRICKS.getDefaultState() :
                                                ModBlocks.VOID_STONE_BRICKS.getDefaultState(), 3);
                            }
                        }
                    }
                }
            }

            // Corner pillars with floating crystals
            int[][] corners = {{-3, -3}, {-3, 3}, {3, -3}, {3, 3}};
            for (int[] corner : corners) {
                BlockPos pillarBase = new BlockPos(center.getX() + corner[0], surfaceY + 1, center.getZ() + corner[1]);
                for (int y = 0; y < 5; y++) {
                    BlockPos p = pillarBase.up(y);
                    if (chunkBox.contains(p)) {
                        world.setBlockState(p, ModBlocks.VOID_STONE_BRICKS.getDefaultState(), 2);
                    }
                }
                // Floating crystal above pillar
                BlockPos crystal = pillarBase.up(5);
                if (chunkBox.contains(crystal)) {
                    world.setBlockState(crystal, ModBlocks.CRYSTAL_BLOCK.getDefaultState(), 2);
                }
                // Floating end rod for effect
                BlockPos rod = pillarBase.up(6);
                if (chunkBox.contains(rod)) {
                    world.setBlockState(rod, Blocks.END_ROD.getDefaultState(), 2);
                }
            }

            // Echo shard on one of the pillars
            int[] shardCorner = corners[1];
            BlockPos shardPos = new BlockPos(center.getX() + shardCorner[0], surfaceY + 1, center.getZ() + shardCorner[1]);
            if (chunkBox.contains(shardPos)) {
                world.setBlockState(shardPos, ModBlocks.ECHO_SHARD_2.getDefaultState(), 2);
            }

            // Walls/arches between pillars
            for (int i = 0; i < 4; i++) {
                int[] c1 = corners[i];
                int[] c2 = corners[(i + 1) % 4];
                // Arch between c1 and c2
                int dx = Integer.signum(c2[0] - c1[0]);
                int dz = Integer.signum(c2[1] - c1[1]);
                int steps = Math.abs(c2[0] - c1[0]) + Math.abs(c2[1] - c1[1]);
                for (int step = 1; step < steps; step++) {
                    int cx = c1[0] + dx * step;
                    int cz = c1[1] + dz * step;
                    for (int y = 1; y <= 3; y++) {
                        BlockPos p = new BlockPos(center.getX() + cx, surfaceY + y, center.getZ() + cz);
                        if (chunkBox.contains(p)) {
                            world.setBlockState(p, ModBlocks.VOID_STONE_BRICKS.getDefaultState(), 2);
                        }
                    }
                }
            }

            // Echo Altar at center
            BlockPos altar = new BlockPos(center.getX(), surfaceY + 1, center.getZ());
            if (chunkBox.contains(altar)) {
                world.setBlockState(altar, ModBlocks.ECHO_ALTAR.getDefaultState(), 2);
            }

            // Surrounding crystal blocks (for summoning ritual)
            int[][] ritualOffsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] off : ritualOffsets) {
                BlockPos ritual = new BlockPos(center.getX() + off[0], surfaceY + 1, center.getZ() + off[1]);
                if (chunkBox.contains(ritual)) {
                    world.setBlockState(ritual, ModBlocks.CRYSTAL_BLOCK.getDefaultState(), 2);
                }
            }

            // Loot chests (hidden under platform)
            for (int i = 0; i < 2; i++) {
                int cx = center.getX() + (random.nextBetween(-3, 3));
                int cz = center.getZ() + (random.nextBetween(-3, 3));
                BlockPos chestPos = new BlockPos(cx, surfaceY - 1, cz);
                if (chunkBox.contains(chestPos) && random.nextFloat() < 0.7f) {
                    world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
                    net.minecraft.block.entity.ChestBlockEntity be = (net.minecraft.block.entity.ChestBlockEntity)
                            world.getBlockEntity(chestPos);
                    if (be != null) {
                        be.setLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE,
                                net.minecraft.util.Identifier.of("void_echo", "chests/echo_sanctum")),
                                random.nextLong());
                    }
                }
            }
        }
    }
}
