package com.example.voidecho.item;

import com.example.voidecho.ModSoundEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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

        if (!world.isClient) {
            Vec3d look = user.getRotationVector();
            double targetX = user.getX() + look.x * 5.0;
            double targetY = user.getY() + look.y * 5.0;
            double targetZ = user.getZ() + look.z * 5.0;
            // Clamp vertical to world bounds
            targetY = Math.max(targetY, world.getBottomY() + 1);
            targetY = Math.min(targetY, world.getTopY());
            // Clamp X/Z to world border
            double borderSize = world.getWorldBorder().getSize() / 2.0;
            targetX = Math.max(-borderSize, Math.min(borderSize, targetX));
            targetZ = Math.max(-borderSize, Math.min(borderSize, targetZ));
            // Check if destination is safe
            BlockPos targetPos = BlockPos.ofFloored(targetX, targetY, targetZ);
            BlockState targetBlock = world.getBlockState(targetPos);
            BlockState headBlock = world.getBlockState(targetPos.up());
            BlockState aboveHeadBlock = world.getBlockState(targetPos.up(2));
            boolean isSafe = (targetBlock.isAir() || !targetBlock.isSolid())
                    && (headBlock.isAir() || !headBlock.isSolid())
                    && (aboveHeadBlock.isAir() || !aboveHeadBlock.isSolid());
            boolean isHazard = targetBlock.isOf(Blocks.LAVA) || headBlock.isOf(Blocks.LAVA);
            if (isSafe && !isHazard) {
                user.teleport(targetX, targetY, targetZ, false);
                user.getItemCooldownManager().set(this, 200);
            } else {
                user.sendMessage(Text.translatable("message.void_echo.teleport_blocked"), true);
                user.getItemCooldownManager().set(this, 40);
            }
        }

        return TypedActionResult.success(stack);
    }
}
