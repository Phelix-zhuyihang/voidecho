package com.example.voidecho.item.armor;

import com.example.voidecho.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class VoidArmorItem extends ArmorItem {
    public VoidArmorItem(RegistryEntry<ArmorMaterial> material, Type type, Settings settings) {
        super(material, type, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity player) {
            EquipmentSlot wornSlot = this.getSlotType();
            // Only process once per tick via the HEAD slot; avoids 4× redundant
            // countPiecesWorn() calls when all four armor pieces are worn.
            if (wornSlot != EquipmentSlot.HEAD) return;
            if (ItemStack.areItemsEqual(player.getEquippedStack(wornSlot), stack)) {
                int piecesWorn = countPiecesWorn(player);

                // Each piece grants Speed I. Full four pieces = Speed II.
                if (piecesWorn > 0) {
                    int amplifier = piecesWorn >= 4 ? 1 : 0;
                    StatusEffectInstance existing = player.getStatusEffect(StatusEffects.SPEED);
                    if (existing == null || existing.getDuration() <= 30 || existing.getAmplifier() < amplifier) {
                        player.addStatusEffect(new StatusEffectInstance(
                                StatusEffects.SPEED, 40, amplifier, true, false, true
                        ));
                    }
                }

                // Full set: immunity to fall damage
                if (piecesWorn >= 4) {
                    player.fallDistance = 0.0f;
                }
            }
        }
    }

    private int countPiecesWorn(PlayerEntity player) {
        int count = 0;
        if (player.getEquippedStack(EquipmentSlot.HEAD).getItem() instanceof VoidArmorItem) count++;
        if (player.getEquippedStack(EquipmentSlot.CHEST).getItem() instanceof VoidArmorItem) count++;
        if (player.getEquippedStack(EquipmentSlot.LEGS).getItem() instanceof VoidArmorItem) count++;
        if (player.getEquippedStack(EquipmentSlot.FEET).getItem() instanceof VoidArmorItem) count++;
        return count;
    }
}
