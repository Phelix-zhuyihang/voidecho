package com.example.voidecho.item;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;

public class VoidBowItem extends BowItem {
    public VoidBowItem(Settings settings) {
        super(settings);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;

        boolean infinite = player.getAbilities().creativeMode
                || EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0;
        ItemStack arrowStack = player.getProjectileType(stack);

        if (arrowStack.isEmpty() && !infinite) return;

        if (arrowStack.isEmpty()) {
            arrowStack = new ItemStack(Items.ARROW);
        }

        int useDuration = this.getMaxUseTime(stack, user) - remainingUseTicks;
        float pullProgress = getPullProgress(useDuration);
        if (pullProgress < 0.1) return;

        boolean arrowInfinite = infinite && arrowStack.isOf(Items.ARROW);

        if (!world.isClient) {
            ArrowItem arrowItem = (ArrowItem) (arrowStack.getItem() instanceof ArrowItem a
                    ? a
                    : Items.ARROW);
            PersistentProjectileEntity arrow = arrowItem.createArrow(world, arrowStack, player, stack);
            arrow.setVelocity(player, player.getPitch(), player.getYaw(),
                    0.0f, pullProgress * 3.0f, 1.0f);

            if (pullProgress >= 1.0f) {
                arrow.setCritical(true);
            }

            // Power enchantment
            int powerLevel = EnchantmentHelper.getLevel(Enchantments.POWER, stack);
            if (powerLevel > 0) {
                arrow.setDamage(arrow.getDamage() + (double) powerLevel * 0.5 + 0.5);
            }

            // Void bow bonus
            arrow.setDamage(arrow.getDamage() + 4.0);

            // Punch enchantment
            int punchLevel = EnchantmentHelper.getLevel(Enchantments.PUNCH, stack);
            if (punchLevel > 0) {
                arrow.setPunch(punchLevel);
            }

            // Flame enchantment
            if (EnchantmentHelper.getLevel(Enchantments.FLAME, stack) > 0) {
                arrow.setOnFireFor(100);
            }

            stack.damage(1, player, LivingEntity.getSlotForHand(user.getActiveHand()));

            if (arrowInfinite || player.getAbilities().creativeMode
                    && (arrowStack.isOf(Items.SPECTRAL_ARROW) || arrowStack.isOf(Items.TIPPED_ARROW))) {
                arrow.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            }

            world.spawnEntity(arrow);

            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0f,
                    1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + pullProgress * 0.5f);
        }

        if (!arrowInfinite) {
            arrowStack.decrementUnlessCreative(1, player);
        }

        player.incrementStat(Stats.USED.getOrCreateStat(this));
    }
}
