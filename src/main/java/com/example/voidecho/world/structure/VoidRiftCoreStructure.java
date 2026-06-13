package com.example.voidecho.world.structure;

import com.example.voidecho.block.ModBlocks;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

/**
 * Void Rift Core — a small underground structure in the Void Wastes.
 * Features an active Void Rift surrounded by crystal barriers that
 * provide cover from the rift's periodic pulses.
 */
public class VoidRiftCoreStructure extends Structure {

    public static final MapCodec<VoidRiftCoreStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(configCodecBuilder(instance))
                    .apply(instance, VoidRiftCoreStructure::new));

    public VoidRiftCoreStructure(Config config) {
        super(config);
    }

    @Override
    protected Optional<StructurePosition> getStructurePosition(Context context) {
        return getStructurePosition(context, Heightmap.Type.WORLD_SURFACE_WG, collector -> {
            ChunkPos chunkPos = context.chunkPos();
            BlockPos startPos = new BlockPos(chunkPos.getStartX(), 40, chunkPos.getStartZ());
            collector.addPiece(new RiftCorePiece(startPos));
        });
    }

    @Override
    public StructureType<?> getType() {
        return ModStructures.VOID_RIFT_CORE_TYPE;
    }

    public static class RiftCorePiece extends StructurePiece {
        public RiftCorePiece(BlockPos startPos) {
            super(ModStructurePieceTypes.RIFT_CORE_PIECE, 0,
                    new BlockBox(startPos.getX() - 6, startPos.getY() - 20,
                            startPos.getZ() - 6, startPos.getX() + 6,
                            startPos.getY() + 10, startPos.getZ() + 6));
        }

        public RiftCorePiece(StructurePieceType type, int length, BlockBox box) {
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

            int surfaceY = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG,
                    center.getX(), center.getZ());
            int roomY = surfaceY - 10;

            // Excavate a small chamber underground
            for (int y = -3; y <= 4; y++) {
                for (int x = -5; x <= 5; x++) {
                    for (int z = -5; z <= 5; z++) {
                        // Rough circular chamber
                        if (x * x + z * z <= 25) {
                            BlockPos p = new BlockPos(center.getX() + x,
                                    roomY + y, center.getZ() + z);
                            if (chunkBox.contains(p)) {
                                if (y == -3 || y == 4) {
                                    // Floor/ceiling — void stone bricks
                                    world.setBlockState(p,
                                            ModBlocks.VOID_STONE_BRICKS.getDefaultState(), 2);
                                } else if (x * x + z * z >= 20) {
                                    // Walls — cracked void stone bricks
                                    world.setBlockState(p,
                                            random.nextFloat() < 0.3f
                                                    ? ModBlocks.CRACKED_VOID_STONE_BRICKS.getDefaultState()
                                                    : ModBlocks.VOID_STONE_BRICKS.getDefaultState(), 2);
                                } else {
                                    // Interior — hollow with air
                                    world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
                                }
                            }
                        }
                    }
                }
            }

            // Rift Core at center
            BlockPos riftPos = new BlockPos(center.getX(), roomY, center.getZ());
            if (chunkBox.contains(riftPos)) {
                world.setBlockState(riftPos, ModBlocks.RIFT_CORE.getDefaultState(), 2);
            }

            // 4 Crystal barrier pillars around the rift (cardinal directions)
            int[][] barrierOffsets = {{3, 0}, {-3, 0}, {0, 3}, {0, -3}};
            for (int[] off : barrierOffsets) {
                // 2-wide, 3-tall crystal barrier
                for (int dy = 0; dy < 3; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int bx = center.getX() + off[0] + (off[0] == 0 ? dx : 0);
                        int bz = center.getZ() + off[1] + (off[1] == 0 ? dx : 0);
                        BlockPos barrierPos = new BlockPos(bx, roomY + dy, bz);
                        if (chunkBox.contains(barrierPos)) {
                            // Make barriers partially cracked for visual variety
                            boolean cracked = random.nextFloat() < 0.2f && dy < 2;
                            world.setBlockState(barrierPos,
                                    cracked ? ModBlocks.CRACKED_VOID_STONE_BRICKS.getDefaultState()
                                            : ModBlocks.CRYSTAL_BLOCK.getDefaultState(), 2);
                        }
                    }
                }
            }

            // Ruined entrance shaft (from surface to chamber) — 2x2 vertical shaft
            for (int y = roomY + 4; y < surfaceY; y++) {
                for (int dx = -1; dx <= 0; dx++) {
                    for (int dz = -1; dz <= 0; dz++) {
                        BlockPos shaftPos = new BlockPos(center.getX() + dx + 2,
                                y, center.getZ() + dz + 2);
                        if (chunkBox.contains(shaftPos)) {
                            world.setBlockState(shaftPos, Blocks.AIR.getDefaultState(), 2);
                        }
                    }
                }
                // Ladder on shaft wall
                BlockPos ladderPos = new BlockPos(center.getX() + 3, y, center.getZ() + 2);
                if (chunkBox.contains(ladderPos)) {
                    world.setBlockState(ladderPos,
                            Blocks.LADDER.getDefaultState()
                                    .with(net.minecraft.block.LadderBlock.FACING, Direction.WEST), 2);
                }
            }

            // Debris on floor — cracked blocks and void stone
            for (int i = 0; i < 8; i++) {
                int dx = random.nextBetween(-3, 3);
                int dz = random.nextBetween(-3, 3);
                BlockPos debrisPos = new BlockPos(center.getX() + dx, roomY - 2, center.getZ() + dz);
                if (chunkBox.contains(debrisPos) && world.getBlockState(debrisPos).isAir()) {
                    world.setBlockState(debrisPos,
                            random.nextFloat() < 0.5f
                                    ? ModBlocks.VOID_STONE.getDefaultState()
                                    : ModBlocks.VOID_DIRT.getDefaultState(), 2);
                }
            }
        }
    }
}
