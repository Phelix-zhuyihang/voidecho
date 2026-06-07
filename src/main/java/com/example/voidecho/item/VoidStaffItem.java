package com.example.voidecho.item;

import com.example.voidecho.ModSoundEvents;
import com.example.voidecho.entity.ModEntities;
import com.example.voidecho.entity.VoidBoltEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class VoidStaffItem extends Item {
    public VoidStaffItem(Settings settings) {
        super(settings.maxDamage(250));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }

        if (!world.isClient) {
            Vec3d look = user.getRotationVec(1.0f);
            VoidBoltEntity bolt = new VoidBoltEntity(
                    ModEntities.VOID_BOLT, user,
                    look.x * 3.0, look.y * 3.0, look.z * 3.0,
                    world
            );
            world.spawnEntity(bolt);

            stack.damage(1, user, EquipmentSlot.MAINHAND);

            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    ModSoundEvents.ITEM_VOID_STAFF_CAST, SoundCategory.PLAYERS, 1.0f, 1.0f);

            user.getItemCooldownManager().set(this, 40); // 2 seconds
            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }

        return TypedActionResult.success(stack);
    }
}
