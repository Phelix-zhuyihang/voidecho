package com.example.voidecho.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class EchoShardBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(4, 0, 4, 12, 8, 12);
    public static final String[] LORE_KEYS = {
        "message.void_echo.echo_shard_1", "message.void_echo.echo_shard_2",
        "message.void_echo.echo_shard_3", "message.void_echo.echo_shard_4",
        "message.void_echo.echo_shard_5"
    };
    private final int shardIndex;

    public EchoShardBlock(int shardIndex) {
        super(Settings.create().strength(0.5f).nonOpaque().luminance(s -> 3));
        this.shardIndex = shardIndex;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            String key = LORE_KEYS[Math.min(shardIndex, LORE_KEYS.length - 1)];
            player.sendMessage(Text.translatable(key), false);
        }
        return ActionResult.SUCCESS;
    }
}
