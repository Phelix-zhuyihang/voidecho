package com.example.voidecho.world.structure;

import com.example.voidecho.block.ModBlocks;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.BlockRotation;
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
 * Void Fortress - generates in the Void's End dimension.
 * A large underground complex with corridors, rooms, and a boss chamber.
 * Programmatic generation produces a dungeon-like layout.
 */
public class VoidFortressStructure extends Structure {

    public static final MapCodec<VoidFortressStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(configCodecBuilder(instance))
                    .apply(instance, VoidFortressStructure::new));

    public VoidFortressStructure(Config config) {
        super(config);
    }

    @Override
    protected Optional<StructurePosition> getStructurePosition(Context context) {
        return getStructurePosition(context, Heightmap.Type.WORLD_SURFACE_WG, collector -> {
            ChunkPos chunkPos = context.chunkPos();
            // Start underground in the void dimension
            BlockPos startPos = new BlockPos(chunkPos.getStartX(), 60, chunkPos.getStartZ());
            collector.addPiece(new FortressEntrancePiece(startPos));
        });
    }

    @Override
    public StructureType<?> getType() {
        return ModStructures.VOID_FORTRESS_TYPE;
    }

    /**
     * Fortress entrance - a ruined gateway leading to underground chambers.
     */
    public static class FortressEntrancePiece extends StructurePiece {
        public FortressEntrancePiece(BlockPos startPos) {
            super(ModStructurePieceTypes.FORTRESS_PIECE, 0,
                    new BlockBox(startPos.getX() - 8, startPos.getY() - 50, startPos.getZ() - 8,
                            startPos.getX() + 8, Math.min(startPos.getY() + 80, 127), startPos.getZ() + 8));
        }

        public FortressEntrancePiece(StructurePieceType type, int length, BlockBox box) {
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
                    boundingBox.getMinX() + 8,
                    boundingBox.getMinY(),
                    boundingBox.getMinZ() + 8);

            // Surface entrance - ruined tower
            int surfaceY = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, center.getX(), center.getZ());

            // Entrance tower (above ground)
            buildEntranceTower(world, center.withY(surfaceY), chunkBox, random);

            // Staircase down
            buildStaircase(world, center.withY(surfaceY), chunkBox, random);

            // Main corridor underground
            buildMainCorridor(world, center.withY(surfaceY - 6), chunkBox, random);

            // Boss chamber at the bottom
            BlockPos bossRoom = center.withY(surfaceY - 12);
            buildBossChamber(world, bossRoom, chunkBox, random);

            // Lectern with "Commander's Final Entry" in boss chamber
            BlockPos lecternPos = bossRoom.add(4, -4, 0);
            if (chunkBox.contains(lecternPos)) {
                placeLecternWithBook(world, lecternPos,
                    "book.void_echo.commanders_entry.title",
                    "book.void_echo.commanders_entry.author",
                    "book.void_echo.commanders_entry.page1",
                    "book.void_echo.commanders_entry.page2",
                    "book.void_echo.commanders_entry.page3");
            }

            // Side rooms (anchor to surface Y so they connect to the main structure)
            BlockPos surfaceCenter = center.withY(surfaceY);
            buildBarracks(world, surfaceCenter.add(8, -8, 0), chunkBox, random);
            buildVault(world, surfaceCenter.add(-8, -8, 0), chunkBox, random);
            buildTrapRoom(world, surfaceCenter.add(0, -8, 8), chunkBox, random);
        }

        private void buildEntranceTower(StructureWorldAccess world, BlockPos pos, BlockBox box, Random rand) {
            // Ruined tower - hollow cylinder
            for (int y = 0; y < 6; y++) {
                int radius = y < 3 ? 3 : 2;
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x * x + z * z <= radius * radius) {
                            BlockPos p = pos.add(x, y, z);
                            if (box.contains(p)) {
                                if (x * x + z * z >= (radius - 1) * (radius - 1) ||
                                        (y == 0 || y == 5)) {
                                    world.setBlockState(p,
                                            rand.nextFloat() < 0.2f ?
                                                    ModBlocks.CRACKED_VOID_STONE_BRICKS.getDefaultState() :
                                                    ModBlocks.VOID_STONE_BRICKS.getDefaultState(), 3);
                                } else if (y == 0) {
                                    world.setBlockState(p, ModBlocks.VOID_STONE_BRICKS.getDefaultState(), 2);
                                }
                            }
                        }
                    }
                }
            }
            // Opening in the center for staircase
            for (int dy = 0; dy < 4; dy++) {
                BlockPos floor = pos.add(0, dy, 0);
                if (box.contains(floor)) {
                    world.setBlockState(floor, Blocks.AIR.getDefaultState(), 2);
                }
            }
        }

        private void buildStaircase(StructureWorldAccess world, BlockPos pos, BlockBox box, Random rand) {
            // Spiral staircase going down
            for (int y = 0; y > -10; y--) {
                BlockPos step = pos.add(0, y, 0);
                // Hollow center with stair blocks spiraling
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        if (dx * dx + dz * dz <= 4) {
                            BlockPos wallPos = step.add(dx, 0, dz);
                            if (box.contains(wallPos)) {
                                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                                    world.setBlockState(wallPos, ModBlocks.VOID_STONE_BRICKS.getDefaultState(), 2);
                                } else if (dx == 0 && dz == 0) {
                                    // Spiral stairs
                                    world.setBlockState(wallPos,
                                            y % 2 == 0 ? ModBlocks.VOID_STONE_BRICKS.getDefaultState() :
                                                    Blocks.AIR.getDefaultState(), 3);
                                }
                            }
                        }
                    }
                }
            }
        }

        private void buildMainCorridor(StructureWorldAccess world, BlockPos pos, BlockBox box, Random rand) {
            // 16x4 corridor extending in all 4 directions
            for (Direction dir : Direction.Type.HORIZONTAL) {
                for (int i = 2; i < 8; i++) {
                    BlockPos offset = pos.offset(dir, i);
                    int width = 3;
                    for (int dx = -width; dx <= width; dx++) {
                        for (int dz = -width; dz <= width; dz++) {
                            BlockPos p = offset.add(dx, -1, dz);
                            if (box.contains(p)) {
                                boolean isWall = (dir.getAxis() == Direction.Axis.Z
                                        ? Math.abs(dx) : Math.abs(dz)) == width;
                                world.setBlockState(p.up(2),
                                        isWall ? ModBlocks.VOID_STONE_BRICKS.getDefaultState() : Blocks.AIR.getDefaultState(), 3);
                                world.setBlockState(p.up(1),
                                        isWall ? ModBlocks.VOID_STONE_BRICKS.getDefaultState() : Blocks.AIR.getDefaultState(), 3);
                                world.setBlockState(p,
                                        ModBlocks.VOID_STONE_BRICKS.getDefaultState(), 3);
                                // Torches on walls
                                if (isWall && i % 3 == 0 && rand.nextFloat() < 0.3f) {
                                    world.setBlockState(p.up(1), Blocks.SOUL_TORCH.getDefaultState(), 2);
                                }
                            }
                        }
                    }
                }
            }
        }

        private void buildBossChamber(StructureWorldAccess world, BlockPos pos, BlockBox box, Random rand) {
            // Large circular room for the boss fight
            for (int y = -5; y < 5; y++) {
                int radius = 6;
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x * x + z * z <= radius * radius) {
                            BlockPos p = pos.add(x, y, z);
                            if (box.contains(p)) {
                                if (y == -5) {
                                    // Floor - patterned
                                    world.setBlockState(p,
                                            (x + z) % 2 == 0 ?
                                                    ModBlocks.VOID_STONE_BRICKS.getDefaultState() :
                                                    ModBlocks.CRACKED_VOID_STONE_BRICKS.getDefaultState(), 3);
                                } else if (x * x + z * z >= (radius - 1) * (radius - 1) || y == 4) {
                                    // Walls and ceiling
                                    world.setBlockState(p, ModBlocks.VOID_STONE_BRICKS.getDefaultState(), 2);
                                } else {
                                    world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
                                }
                            }
                        }
                    }
                }
            }
            // Central platform
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos p = pos.add(x, -4, z);
                    if (box.contains(p)) {
                        world.setBlockState(p, ModBlocks.VOID_STONE_BRICKS.getDefaultState(), 2);
                    }
                }
            }

            // Boss arena is constructed; Void Stalker spawning is handled
            // by the structure's spawn_overrides or external mechanics.

            // Echo shard in boss chamber
            BlockPos shardPos = pos.add(2, -4, 2);
            if (box.contains(shardPos)) {
                world.setBlockState(shardPos, ModBlocks.ECHO_SHARD_5.getDefaultState(), 2);
            }
        }

        private void buildBarracks(StructureWorldAccess world, BlockPos pos, BlockBox box, Random rand) {
            // Rectangular room with spawner-like decoration
            for (int y = 0; y < 5; y++) {
                for (int x = -3; x <= 3; x++) {
                    for (int z = -3; z <= 3; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (box.contains(p)) {
                            boolean isWall = Math.abs(x) == 3 || Math.abs(z) == 3 || y == 0 || y == 4;
                            world.setBlockState(p,
                                    isWall ? ModBlocks.VOID_STONE_BRICKS.getDefaultState() : Blocks.AIR.getDefaultState(), 3);
                        }
                    }
                }
            }
            // Place a chest
            BlockPos chest = pos.add(0, 1, 0);
            if (box.contains(chest)) {
                world.setBlockState(chest, Blocks.CHEST.getDefaultState(), 2);
                net.minecraft.block.entity.ChestBlockEntity be = (net.minecraft.block.entity.ChestBlockEntity)
                        world.getBlockEntity(chest);
                if (be != null) {
                    be.setLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE,
                            net.minecraft.util.Identifier.of("void_echo", "chests/void_fortress_vault")),
                            rand.nextLong());
                }
            }

            // Echo shard in barracks
            BlockPos shardPos = pos.add(2, 1, 0);
            if (box.contains(shardPos)) {
                world.setBlockState(shardPos, ModBlocks.ECHO_SHARD_4.getDefaultState(), 2);
            }
        }

        private void buildVault(StructureWorldAccess world, BlockPos pos, BlockBox box, Random rand) {
            // Small vault room with reinforced walls
            for (int y = 0; y < 4; y++) {
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (box.contains(p)) {
                            boolean isWall = Math.abs(x) == 2 || Math.abs(z) == 2 || y == 0 || y == 3;
                            world.setBlockState(p,
                                    isWall ? ModBlocks.CRYSTAL_BLOCK.getDefaultState() : Blocks.AIR.getDefaultState(), 3);
                        }
                    }
                }
            }
            // Double chest in vault
            BlockPos chest1 = pos.add(0, 1, 0);
            BlockPos chest2 = pos.add(1, 1, 0);
            if (box.contains(chest1) && box.contains(chest2)) {
                world.setBlockState(chest1, Blocks.CHEST.getDefaultState(), 2);
                world.setBlockState(chest2, Blocks.CHEST.getDefaultState(), 2);

                net.minecraft.block.entity.ChestBlockEntity vaultChest1 =
                    (net.minecraft.block.entity.ChestBlockEntity) world.getBlockEntity(chest1);
                if (vaultChest1 != null) {
                    vaultChest1.setLootTable(net.minecraft.registry.RegistryKey.of(
                        net.minecraft.registry.RegistryKeys.LOOT_TABLE,
                        net.minecraft.util.Identifier.of("void_echo", "chests/void_fortress_vault")),
                        rand.nextLong());
                }
                net.minecraft.block.entity.ChestBlockEntity vaultChest2 =
                    (net.minecraft.block.entity.ChestBlockEntity) world.getBlockEntity(chest2);
                if (vaultChest2 != null) {
                    vaultChest2.setLootTable(net.minecraft.registry.RegistryKey.of(
                        net.minecraft.registry.RegistryKeys.LOOT_TABLE,
                        net.minecraft.util.Identifier.of("void_echo", "chests/void_fortress_vault")),
                        rand.nextLong());
                }
            }

            // Echo shard in vault room
            BlockPos shardPos = pos.add(-1, 1, 0);
            if (box.contains(shardPos)) {
                world.setBlockState(shardPos, ModBlocks.ECHO_SHARD_3.getDefaultState(), 2);
            }
        }

        private void buildTrapRoom(StructureWorldAccess world, BlockPos pos, BlockBox box, Random rand) {
            // Room with hazards
            for (int y = 0; y < 4; y++) {
                for (int x = -3; x <= 3; x++) {
                    for (int z = -3; z <= 3; z++) {
                        BlockPos p = pos.add(x, y, z);
                        if (box.contains(p)) {
                            boolean isWall = Math.abs(x) == 3 || Math.abs(z) == 3 || y == 0 || y == 3;
                            world.setBlockState(p,
                                    isWall ? ModBlocks.VOID_STONE_BRICKS.getDefaultState() : Blocks.AIR.getDefaultState(), 3);
                        }
                    }
                }
            }
            // Soul sand floor traps
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos p = pos.add(x, 0, z);
                    if (box.contains(p) && rand.nextFloat() < 0.5f) {
                        world.setBlockState(p, Blocks.SOUL_SAND.getDefaultState(), 2);
                        world.setBlockState(p.up(), Blocks.SOUL_FIRE.getDefaultState(), 2);
                    }
                }
            }
        }

        private static void placeLecternWithBook(StructureWorldAccess world, BlockPos pos,
                String titleKey, String authorKey, String p1, String p2, String p3) {
            world.setBlockState(pos, Blocks.LECTERN.getDefaultState()
                    .with(LecternBlock.HAS_BOOK, true).with(LecternBlock.FACING,
                            net.minecraft.util.math.Direction.SOUTH), 2);
            LecternBlockEntity be = (LecternBlockEntity) world.getBlockEntity(pos);
            if (be != null) {
                ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
                book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT,
                    new WrittenBookContentComponent(
                        RawFilteredPair.of(Text.translatable(titleKey)),
                        Text.translatable(authorKey).getString(),
                        0,
                        java.util.List.of(
                            RawFilteredPair.of(Text.translatable(p1)),
                            RawFilteredPair.of(Text.translatable(p2)),
                            RawFilteredPair.of(Text.translatable(p3))
                        ),
                        true));
                be.setBook(book);
            }
        }
    }
}
