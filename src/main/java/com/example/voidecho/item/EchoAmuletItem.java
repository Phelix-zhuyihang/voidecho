package com.example.voidecho.item;

import com.example.voidecho.world.dimension.ModDimensions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

public class EchoAmuletItem extends Item {
    public EchoAmuletItem() {
        super(new Settings().rarity(Rarity.EPIC).fireproof().maxCount(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity player) {
            if (world.getRegistryKey().equals(ModDimensions.VOIDS_END_LEVEL_KEY)) {
                applyIfNeeded(player, StatusEffects.STRENGTH, 120, 0);
                applyIfNeeded(player, StatusEffects.SPEED, 120, 0);
                applyIfNeeded(player, StatusEffects.RESISTANCE, 120, 0);
            }
        }
    }

    private void applyIfNeeded(PlayerEntity player, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect,
                                int duration, int amplifier) {
        StatusEffectInstance existing = player.getStatusEffect(effect);
        if (existing == null || existing.getDuration() <= 30) {
            player.addStatusEffect(new StatusEffectInstance(effect, duration, amplifier, false, false, true));
        }
    }
}
