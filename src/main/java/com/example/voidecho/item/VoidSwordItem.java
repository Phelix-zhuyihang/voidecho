package com.example.voidecho.item;

import com.example.voidecho.ModSoundEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class VoidSwordItem extends SwordItem {
    public VoidSwordItem(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }

        if (world.isClient) {
            return TypedActionResult.success(stack);
        }

        Vec3d look = user.getRotationVector();
        double targetX = user.getX() + look.x * 5.0;
        double targetY = user.getY() + look.y * 5.0;
        double targetZ = user.getZ() + look.z * 5.0;

        // Clamp vertical to buildable world bounds
        targetY = Math.max(targetY, world.getBottomY());
        targetY = Math.min(targetY, world.getTopY() - 1);

        // Clamp X/Z to world border (handles custom center correctly)
        BlockPos borderClamped = world.getWorldBorder().clamp(targetX, targetY, targetZ);
        targetX = borderClamped.getX();
        targetZ = borderClamped.getZ();

        // Check if destination is safe
        BlockPos targetPos = BlockPos.ofFloored(targetX, targetY, targetZ);
        BlockState targetBlock = world.getBlockState(targetPos);
        BlockState headBlock = world.getBlockState(targetPos.up());
        BlockState aboveHeadBlock = world.getBlockState(targetPos.up(2));

        boolean isSafe = (targetBlock.isAir() || !targetBlock.isSolid())
                && (headBlock.isAir() || !headBlock.isSolid())
                && (aboveHeadBlock.isAir() || !aboveHeadBlock.isSolid());
        boolean isHazard = isDangerousBlock(targetBlock)
                || isDangerousBlock(headBlock)
                || isDangerousBlock(aboveHeadBlock);

        if (isSafe && !isHazard) {
            user.teleport(targetX, targetY, targetZ, false);
            stack.damage(1, user, hand == Hand.MAIN_HAND
                    ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            user.getItemCooldownManager().set(this, 200);
            return TypedActionResult.success(stack);
        } else {
            user.sendMessage(Text.translatable("message.void_echo.teleport_blocked"), true);
            user.getItemCooldownManager().set(this, 40);
            return TypedActionResult.fail(stack);
        }
    }

    private static boolean isDangerousBlock(BlockState state) {
        return state.isOf(Blocks.LAVA)
                || state.isOf(Blocks.FIRE)
                || state.isOf(Blocks.SOUL_FIRE)
                || state.isOf(Blocks.MAGMA_BLOCK)
                || state.isOf(Blocks.CAMPFIRE)
                || state.isOf(Blocks.SOUL_CAMPFIRE)
                || state.isOf(Blocks.SWEET_BERRY_BUSH)
                || state.isOf(Blocks.POWDER_SNOW)
                || state.isOf(Blocks.CACTUS)
                || state.isOf(Blocks.WITHER_ROSE)
                || state.isOf(Blocks.POINTED_DRIPSTONE);
    }
}
