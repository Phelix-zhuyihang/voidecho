package com.example.voidecho.block;

import com.example.voidecho.ModSoundEvents;
import com.example.voidecho.entity.ModEntities;
import com.example.voidecho.entity.boss.EchoWardenEntity;
import com.example.voidecho.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.world.BlockView;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

import java.util.List;

public class EchoAltarBlock extends Block {
    private static final VoxelShape SHAPE = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 0, 16, 8, 16),
            Block.createCuboidShape(2, 8, 2, 14, 16, 14)
    );

    public EchoAltarBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        ItemStack held = player.getStackInHand(Hand.MAIN_HAND);
        if (held.isEmpty()) {
            held = player.getStackInHand(Hand.OFF_HAND);
        }

        if (held.isOf(ModItems.VOID_HEART)) {
            // Check if Echo Warden already exists nearby
            List<EchoWardenEntity> existing =
                    world.getEntitiesByClass(
                            EchoWardenEntity.class,
                            Box.of(pos.toCenterPos(), 64, 64, 64),
                            e -> true
                    );
            if (!existing.isEmpty()) {
                if (!world.isClient) {
                    player.sendMessage(Text.translatable("message.void_echo.warden_already_exists"), true);
                }
                return ActionResult.FAIL;
            }

            // Check for 4 adjacent crystal_blocks
            BlockPos.Mutable mut = new BlockPos.Mutable();
            int crystalCount = 0;
            for (Direction dir : Direction.Type.HORIZONTAL) {
                mut.set(pos, dir);
                if (world.getBlockState(mut).isOf(ModBlocks.CRYSTAL_BLOCK)) {
                    crystalCount++;
                }
            }

            if (crystalCount >= 4) {
                if (!world.isClient) {
                    // Spawn Echo Warden
                    EchoWardenEntity warden =
                            ModEntities.ECHO_WARDEN.create(world);
                    if (warden != null) {
                        // Consume the heart on successful spawn
                        if (!player.getAbilities().creativeMode) {
                            held.decrement(1);
                        }
                        warden.setPosition(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
                        world.spawnEntity(warden);
                    }

                    // Dramatic sound
                    world.playSound(null, pos, ModSoundEvents.BLOCK_PORTAL_ACTIVATE,
                            SoundCategory.HOSTILE, 2.0f, 0.5f);

                    player.sendMessage(
                            Text.translatable("message.void_echo.echo_warden_summoned"), true);

                    // Destroy surrounding crystal blocks for effect
                    for (Direction dir : Direction.Type.HORIZONTAL) {
                        mut.set(pos, dir);
                        world.breakBlock(mut, false);
                    }
                }
                return ActionResult.SUCCESS;
            } else {
                if (!world.isClient) {
                    player.sendMessage(
                            Text.translatable("message.void_echo.altar_incomplete"), true);
                }
                return ActionResult.FAIL;
            }
        }

        return ActionResult.PASS;
    }
}
