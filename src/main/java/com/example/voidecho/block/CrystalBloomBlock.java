package com.example.voidecho.block;

import com.example.voidecho.block.ModBlocks;
import com.example.voidecho.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class CrystalBloomBlock extends Block {
    private static final VoxelShape SHAPE = createCuboidShape(4, 0, 4, 12, 6, 12);

    public CrystalBloomBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos below = pos.down();
        BlockState belowState = world.getBlockState(below);
        return belowState.isIn(TagKey.of(
            net.minecraft.registry.RegistryKeys.BLOCK,
            net.minecraft.util.Identifier.of("void_echo", "crystal_bloom_plantable_on")
        ));
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (random.nextFloat() < 0.1f) {
            BlockPos dropPos = pos.up();
            if (world.getBlockState(dropPos).isAir()) {
                dropStack(world, dropPos, new ItemStack(ModItems.CRYSTAL_SHARD));
            }
        }
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }
}
