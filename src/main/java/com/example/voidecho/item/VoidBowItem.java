package com.example.voidecho.item;

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

    /**
     * Fires an arrow with +4.0 bonus damage.
     * Delegates arrow creation to the standard arrow item so that
     * enchantments (Infinity, Power, Punch, Flame) are handled by Vanilla.
     */
    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;

        boolean creative = player.getAbilities().creativeMode;
        ItemStack arrowStack = player.getProjectileType(stack);

        if (arrowStack.isEmpty() && !creative) return;

        if (arrowStack.isEmpty()) {
            arrowStack = new ItemStack(Items.ARROW);
        }

        int useDuration = this.getMaxUseTime(stack, user) - remainingUseTicks;
        float pullProgress = getPullProgress(useDuration);
        if (pullProgress < 0.1) return;

        // Infinity is handled by PlayerEntity.getProjectileType when the arrow is
        // not actually consumed. We always decrement the stack if not creative;
        // the game will "replenish" if the player has the Infinity enchantment.
        boolean arrowInfinite = creative;

        if (!world.isClient) {
            ArrowItem arrowItem = (ArrowItem) (arrowStack.getItem() instanceof ArrowItem
                    ? arrowStack.getItem()
                    : Items.ARROW);
            PersistentProjectileEntity arrow = arrowItem.createArrow(world, arrowStack, player, stack);
            arrow.setVelocity(player, player.getPitch(), player.getYaw(),
                    0.0f, pullProgress * 3.0f, 1.0f);

            if (pullProgress >= 1.0f) {
                arrow.setCritical(true);
            }

            // Void bow bonus
            arrow.setDamage(arrow.getDamage() + 4.0);

            stack.damage(1, player, LivingEntity.getSlotForHand(user.getActiveHand()));

            if (arrowInfinite) {
                arrow.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            }

            world.spawnEntity(arrow);
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0f,
                1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + pullProgress * 0.5f);

        if (!arrowInfinite) {
            arrowStack.decrementUnlessCreative(1, player);
        }

        player.incrementStat(Stats.USED.getOrCreateStat(this));
    }
}
