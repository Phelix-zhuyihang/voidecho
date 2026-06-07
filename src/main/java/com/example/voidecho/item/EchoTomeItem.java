package com.example.voidecho.item;

import com.example.voidecho.world.dimension.ModDimensions;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

public class EchoTomeItem extends Item {
    public EchoTomeItem() {
        super(new Item.Settings().rarity(Rarity.EPIC).fireproof().maxCount(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity player) {
            if (world.getRegistryKey().equals(ModDimensions.VOIDS_END_LEVEL_KEY)) {
                StatusEffectInstance existing = player.getStatusEffect(StatusEffects.NIGHT_VISION);
                if (existing == null || existing.getDuration() <= 30) {
                    player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NIGHT_VISION, 300, 0, false, false, true));
                }
            }
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
