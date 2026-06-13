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
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
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
 * Forgotten Altar - generates in overworld plains/desert biomes.
 * A circular stone platform with a void portal frame in the center and pillars around it.
 * Programmatic generation ensures the structure works without NBT files.
 */
public class ForgottenAltarStructure extends Structure {

    public static final MapCodec<ForgottenAltarStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(configCodecBuilder(instance))
                    .apply(instance, ForgottenAltarStructure::new));

    public ForgottenAltarStructure(Config config) {
        super(config);
    }

    @Override
    protected Optional<StructurePosition> getStructurePosition(Context context) {
        return getStructurePosition(context, Heightmap.Type.WORLD_SURFACE_WG, collector -> {
            ChunkPos chunkPos = context.chunkPos();
            BlockPos startPos = new BlockPos(chunkPos.getStartX(), 90, chunkPos.getStartZ());
            collector.addPiece(new AltarPiece(startPos));
        });
    }

    @Override
    public StructureType<?> getType() {
        return ModStructures.FORGOTTEN_ALTAR_TYPE;
    }

    /**
     * Generates the actual Forgotten Altar structure blocks.
     * Layout: 9x9 circular platform of void stone bricks, 4 pillars at corners,
     * void portal frame in center.
     */
    public static class AltarPiece extends StructurePiece {
        public AltarPiece(BlockPos startPos) {
            super(ModStructurePieceTypes.ALTAR_PIECE, 0,
                    new BlockBox(startPos.getX() - 4, startPos.getY() - 50, startPos.getZ() - 4,
                            startPos.getX() + 4, startPos.getY() + 20, startPos.getZ() + 4));
        }

        public AltarPiece(StructurePieceType type, int length, BlockBox box) {
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
                    boundingBox.getMinX() + 4,
                    boundingBox.getMinY(),
                    boundingBox.getMinZ() + 4);

            // Find surface level
            int surfaceY = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, center.getX(), center.getZ());

            // Platform (7x7, slightly irregular circle)
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    // Circle shape
                    if (x * x + z * z <= 12) {
                        BlockPos pos = new BlockPos(center.getX() + x, surfaceY, center.getZ() + z);
                        if (chunkBox.contains(pos)) {
                            if (x * x + z * z <= 4) {
                                // Inner area: patterned accent
                                world.setBlockState(pos, ModBlocks.CRYSTAL_BLOCK.getDefaultState(), 2);
                            } else {
                                world.setBlockState(pos, ModBlocks.VOID_STONE_BRICKS.getDefaultState(), 2);
                            }
                            // Replace grass below
                            world.setBlockState(pos.down(), ModBlocks.VOID_STONE.getDefaultState(), 2);
                        }
                    }
                }
            }

            // 4 Pillars
            int[][] pillarOffsets = {{-3, -3}, {-3, 3}, {3, -3}, {3, 3}};
            for (int[] offset : pillarOffsets) {
                BlockPos pillarBase = new BlockPos(center.getX() + offset[0], surfaceY + 1, center.getZ() + offset[1]);
                for (int y = 0; y < 4; y++) {
                    BlockPos pillarPos = pillarBase.up(y);
                    if (chunkBox.contains(pillarPos)) {
                        world.setBlockState(pillarPos, ModBlocks.VOID_STONE_BRICKS.getDefaultState(), 2);
                    }
                }
                // Crystal block on top
                BlockPos top = pillarBase.up(4);
                if (chunkBox.contains(top)) {
                    world.setBlockState(top, ModBlocks.CRYSTAL_BLOCK.getDefaultState(), 2);
                }
            }

            // Portal frame in center (4 blocks forming a ring, one block elevated)
            BlockPos portalBase = new BlockPos(center.getX(), surfaceY + 1, center.getZ());
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (Math.abs(dx) == 1 || Math.abs(dz) == 1) {
                        BlockPos framePos = portalBase.add(dx, 0, dz);
                        if (chunkBox.contains(framePos)) {
                            world.setBlockState(framePos, ModBlocks.VOID_PORTAL_FRAME.getDefaultState(), 2);
                        }
                    }
                }
            }
            // Center crystal
            BlockPos crystalPos = portalBase.add(0, 1, 0);
            if (chunkBox.contains(crystalPos)) {
                world.setBlockState(crystalPos, ModBlocks.CRYSTAL_BLOCK.getDefaultState(), 2);
            }

            // Place chest with loot under the structure (25% chance)
            if (random.nextFloat() < 0.25f) {
                BlockPos chestPos = portalBase.down(2);
                if (chunkBox.contains(chestPos)) {
                    world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
                    net.minecraft.block.entity.ChestBlockEntity chest = (net.minecraft.block.entity.ChestBlockEntity)
                            world.getBlockEntity(chestPos);
                    if (chest != null) {
                        chest.setLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE,
                                Identifier.of("void_echo", "chests/forgotten_altar")),
                                random.nextLong());
                    }
                }
            }

            // Echo shard near the chest
            BlockPos shardPos = portalBase.add(1, 1, 0);
            if (chunkBox.contains(shardPos)) {
                world.setBlockState(shardPos, ModBlocks.ECHO_SHARD_1.getDefaultState(), 2);
            }

            // Lectern with "Journal of the Last Architect" — north side of platform
            BlockPos lecternPos = portalBase.add(0, 0, -4);
            if (chunkBox.contains(lecternPos)) {
                placeLecternWithBook(world, lecternPos,
                    "book.void_echo.last_architect.title",
                    "book.void_echo.last_architect.author",
                    "book.void_echo.last_architect.page1",
                    "book.void_echo.last_architect.page2",
                    "book.void_echo.last_architect.page3");
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
                            RawFilteredPair.of(titleKey),
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
