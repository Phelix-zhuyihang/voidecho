package com.example.voidecho.block;

import com.example.voidecho.VoidEcho;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    // ---- Block instances ----
    public static final Block VOID_STONE = register(
            "void_stone",
            new Block(AbstractBlock.Settings.copy(Blocks.STONE)
                    .requiresTool().strength(3.0f, 6.0f).sounds(BlockSoundGroup.STONE))
    );
    public static final Block VOID_STONE_BRICKS = register(
            "void_stone_bricks",
            new Block(AbstractBlock.Settings.copy(Blocks.STONE_BRICKS)
                    .requiresTool().strength(3.0f, 6.0f).sounds(BlockSoundGroup.STONE))
    );
    public static final Block CRACKED_VOID_STONE_BRICKS = register(
            "cracked_void_stone_bricks",
            new Block(AbstractBlock.Settings.copy(Blocks.CRACKED_STONE_BRICKS)
                    .requiresTool().strength(3.0f, 6.0f).sounds(BlockSoundGroup.STONE))
    );
    public static final Block VOID_GRASS_BLOCK = register(
            "void_grass_block",
            new VoidGrassBlock(AbstractBlock.Settings.copy(Blocks.GRASS_BLOCK)
                    .strength(0.6f).sounds(BlockSoundGroup.GRASS))
    );
    public static final Block VOID_DIRT = register(
            "void_dirt",
            new Block(AbstractBlock.Settings.copy(Blocks.DIRT)
                    .strength(0.5f).sounds(BlockSoundGroup.GRAVEL))
    );
    public static final Block CRYSTAL_ORE = register(
            "crystal_ore",
            new CrystalOreBlock(AbstractBlock.Settings.copy(Blocks.DIAMOND_ORE)
                    .requiresTool().strength(3.0f, 3.0f).sounds(BlockSoundGroup.STONE))
    );
    public static final Block DEEPSLATE_CRYSTAL_ORE = register(
            "deepslate_crystal_ore",
            new CrystalOreBlock(AbstractBlock.Settings.copy(Blocks.DEEPSLATE_DIAMOND_ORE)
                    .requiresTool().strength(4.5f, 3.0f).sounds(BlockSoundGroup.DEEPSLATE))
    );
    public static final Block CRYSTAL_BLOCK = register(
            "crystal_block",
            new Block(AbstractBlock.Settings.copy(Blocks.DIAMOND_BLOCK)
                    .strength(5.0f, 6.0f).sounds(BlockSoundGroup.METAL).mapColor(MapColor.PURPLE))
    );
    public static final Block VOID_PORTAL_FRAME = register(
            "void_portal_frame",
            new VoidPortalFrameBlock(AbstractBlock.Settings.copy(Blocks.OBSIDIAN)
                    .strength(10.0f, 1200.0f).sounds(BlockSoundGroup.STONE).nonOpaque())
    );
    public static final Block ECHO_ALTAR = register(
            "echo_altar",
            new EchoAltarBlock(AbstractBlock.Settings.copy(Blocks.OBSIDIAN)
                    .strength(10.0f, 1200.0f).sounds(BlockSoundGroup.STONE).nonOpaque().luminance(s -> 7))
    );

    // ---- Echo Shards ----
    public static final Block ECHO_SHARD_1 = register("echo_shard_1", new EchoShardBlock(0));
    public static final Block ECHO_SHARD_2 = register("echo_shard_2", new EchoShardBlock(1));
    public static final Block ECHO_SHARD_3 = register("echo_shard_3", new EchoShardBlock(2));
    public static final Block ECHO_SHARD_4 = register("echo_shard_4", new EchoShardBlock(3));
    public static final Block ECHO_SHARD_5 = register("echo_shard_5", new EchoShardBlock(4));

    // ---- Crystal Bloom ----
    public static final Block CRYSTAL_BLOOM = register(
            "crystal_bloom",
            new CrystalBloomBlock(AbstractBlock.Settings.create().noCollision().nonOpaque()
                    .breakInstantly().sounds(BlockSoundGroup.GRASS).luminance(s -> 4))
    );

    // ---- Void Forge ----
    public static final Block VOID_FORGE = register(
            "void_forge",
            new VoidForgeBlock(AbstractBlock.Settings.copy(Blocks.OBSIDIAN)
                    .strength(10f, 1200f).nonOpaque().luminance(s -> 7))
    );

    // Rift Core
    public static final Block RIFT_CORE = register("rift_core",
            new RiftCoreBlock(AbstractBlock.Settings.copy(Blocks.OBSIDIAN)
                    .strength(50f, 1200f).nonOpaque().luminance(s -> 12))
    );

    // ---- Helper methods ----
    private static Block register(String name, Block block) {
        Identifier id = Identifier.of("void_echo", name);
        Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
        return block;
    }

    public static void init() {
        // static fields are initialised on class load
    }
}
