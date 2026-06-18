package com.example.voidecho.item;

import com.example.voidecho.entity.VoidLureBobberEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class VoidLureItem extends Item {
    public VoidLureItem(Settings settings) { super(settings); }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // Check if player already has active bobber (tracked via vehicle)
        if (user.getVehicle() instanceof VoidLureBobberEntity bobber) {
            if (!world.isClient) {
                bobber.tryReelIn();
                stack.damage(1, user, hand == Hand.MAIN_HAND
                        ? EquipmentSlot.MAINHAND
                        : EquipmentSlot.OFFHAND);
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.PLAYERS, 0.5f, 0.6f);
            return TypedActionResult.success(stack);
        }

        // Cast — throw bobber
        if (!world.isClient) {
            VoidLureBobberEntity bobber = new VoidLureBobberEntity(world, user);
            world.spawnEntity(bobber);
            user.startRiding(bobber);
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_FISHING_BOBBER_THROW, SoundCategory.PLAYERS, 0.5f, 0.6f);
        }
        return TypedActionResult.success(stack);
    }
}
