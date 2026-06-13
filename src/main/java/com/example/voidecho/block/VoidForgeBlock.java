package com.example.voidecho.block;

import com.example.voidecho.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VoidForgeBlock extends Block {
    public VoidForgeBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.PASS;

        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        if (mainHand.isEmpty() || offHand.isEmpty()) return ActionResult.PASS;

        boolean isEquipment = mainHand.getItem() instanceof com.example.voidecho.item.armor.VoidArmorItem
                || mainHand.getItem() == ModItems.VOID_SWORD
                || mainHand.getItem() == ModItems.VOID_BOW
                || mainHand.getItem() == ModItems.VOID_STAFF;

        if (!isEquipment) {
            player.sendMessage(Text.translatable("message.void_echo.forge_invalid_item"), true);
            return ActionResult.FAIL;
        }

        // Track what kind of upgrade was applied (enum of operation types)
        String[] appliedUpgrade = { null };

        // Use .apply() to merge NBT changes, preserving other mods' CUSTOM_DATA keys
        // Only NBT modifications happen inside the lambda; side effects are deferred
        // to after apply() succeeds, preventing inconsistent state on failure.
        mainHand.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, existing -> {
            NbtCompound nbt = existing.copyNbt();

            // Crystal Shard: repair durability (costs 4 shards)
            if (offHand.isOf(ModItems.CRYSTAL_SHARD) && offHand.getCount() >= 4) {
                int durUpgrade = nbt.getInt("void_echo:durability_upgrade");
                if (durUpgrade >= 4) {
                    player.sendMessage(Text.translatable("message.void_echo.forge_max_durability"), true);
                    return existing;
                }
                if (mainHand.getDamage() < 50) {
                    player.sendMessage(Text.translatable("message.void_echo.forge_minimal_damage"), true);
                    return existing;
                }
                nbt.putInt("void_echo:durability_upgrade", durUpgrade + 1);
                appliedUpgrade[0] = "durability";
                return NbtComponent.of(nbt);
            }

            // Echo Core: base echo upgrade (tier 1)
            if (offHand.isOf(ModItems.ECHO_CORE)) {
                if (nbt.contains("void_echo:echo_upgrade", NbtElement.BYTE_TYPE)) {
                    player.sendMessage(Text.translatable("message.void_echo.forge_already_echo"), true);
                    return existing;
                }
                nbt.remove("void_echo:rift_upgrade");
                nbt.remove("void_echo:rift_walker");
                nbt.remove("void_echo:rift_fury");
                nbt.putBoolean("void_echo:echo_upgrade", true);
                appliedUpgrade[0] = "echo_core";
                return NbtComponent.of(nbt);
            }

            // Echo Strike (tier 2 echo, offensive)
            if (offHand.isOf(ModItems.VOID_CATALYST) && nbt.contains("void_echo:echo_upgrade", NbtElement.BYTE_TYPE)
                    && !nbt.contains("void_echo:echo_strike", NbtElement.BYTE_TYPE) && !nbt.contains("void_echo:echo_guard", NbtElement.BYTE_TYPE)) {
                if (player.getInventory().count(ModItems.ECHO_CORE) < 1) {
                    player.sendMessage(Text.translatable("message.void_echo.forge_need_echo_core"), true);
                    return existing;
                }
                nbt.putBoolean("void_echo:echo_strike", true);
                appliedUpgrade[0] = "echo_strike";
                return NbtComponent.of(nbt);
            }

            // Echo Guard (tier 2 echo, defensive)
            if (offHand.isOf(ModItems.RIFT_FRAGMENT) && offHand.getCount() >= 3
                    && nbt.contains("void_echo:echo_upgrade", NbtElement.BYTE_TYPE)
                    && !nbt.contains("void_echo:echo_strike", NbtElement.BYTE_TYPE) && !nbt.contains("void_echo:echo_guard", NbtElement.BYTE_TYPE)) {
                nbt.putBoolean("void_echo:echo_guard", true);
                appliedUpgrade[0] = "echo_guard";
                return NbtComponent.of(nbt);
            }

            // Rift Fragment: base rift upgrade (tier 1)
            if (offHand.isOf(ModItems.RIFT_FRAGMENT) && offHand.getCount() >= 2) {
                if (nbt.contains("void_echo:rift_upgrade", NbtElement.BYTE_TYPE)) {
                    player.sendMessage(Text.translatable("message.void_echo.forge_already_rift"), true);
                    return existing;
                }
                nbt.remove("void_echo:echo_upgrade");
                nbt.remove("void_echo:echo_strike");
                nbt.remove("void_echo:echo_guard");
                nbt.putBoolean("void_echo:rift_upgrade", true);
                appliedUpgrade[0] = "rift";
                return NbtComponent.of(nbt);
            }

            // Rift Walker (tier 2 rift, utility)
            if (offHand.isOf(ModItems.VOID_CATALYST) && nbt.contains("void_echo:rift_upgrade", NbtElement.BYTE_TYPE)
                    && !nbt.contains("void_echo:rift_walker", NbtElement.BYTE_TYPE) && !nbt.contains("void_echo:rift_fury", NbtElement.BYTE_TYPE)) {
                if (player.getInventory().count(ModBlocks.CRYSTAL_BLOCK.asItem()) < 1) {
                    player.sendMessage(Text.translatable("message.void_echo.forge_need_crystal_block"), true);
                    return existing;
                }
                nbt.putBoolean("void_echo:rift_walker", true);
                appliedUpgrade[0] = "rift_walker";
                return NbtComponent.of(nbt);
            }

            // Rift Fury (tier 2 rift, offensive)
            if (offHand.isOf(ModItems.VOID_ALLOY_INGOT) && nbt.contains("void_echo:rift_upgrade", NbtElement.BYTE_TYPE)
                    && !nbt.contains("void_echo:rift_walker", NbtElement.BYTE_TYPE) && !nbt.contains("void_echo:rift_fury", NbtElement.BYTE_TYPE)) {
                if (player.getInventory().count(ModItems.VOID_CATALYST) < 1) {
                    player.sendMessage(Text.translatable("message.void_echo.forge_need_catalyst"), true);
                    return existing;
                }
                nbt.putBoolean("void_echo:rift_fury", true);
                appliedUpgrade[0] = "rift_fury";
                return NbtComponent.of(nbt);
            }

            // ---- Tier 3: Rift Core upgrades ----

            // Void Resonance (weapon — Rift Core ×1, requires existing echo or rift upgrade)
            if (offHand.isOf(ModItems.RIFT_CORE)
                    && (nbt.contains("void_echo:echo_upgrade", NbtElement.BYTE_TYPE)
                        || nbt.contains("void_echo:rift_upgrade", NbtElement.BYTE_TYPE))
                    && !nbt.contains("void_echo:void_resonance", NbtElement.BYTE_TYPE)) {
                nbt.putBoolean("void_echo:void_resonance", true);
                appliedUpgrade[0] = "void_resonance";
                return NbtComponent.of(nbt);
            }

            // Crystal Barrier (chestplate — Rift Core ×1 + 3 Crystal Blocks)
            if (offHand.isOf(ModItems.RIFT_CORE) && player.getInventory()
                    .count(ModBlocks.CRYSTAL_BLOCK.asItem()) >= 3
                    && mainHand.getItem() instanceof com.example.voidecho.item.armor.VoidArmorItem armor
                    && armor.getSlotType() == net.minecraft.entity.EquipmentSlot.CHEST
                    && !nbt.contains("void_echo:crystal_barrier", NbtElement.BYTE_TYPE)) {
                nbt.putBoolean("void_echo:crystal_barrier", true);
                appliedUpgrade[0] = "crystal_barrier";
                return NbtComponent.of(nbt);
            }

            return existing; // No matching upgrade
        });

        // Apply side effects AFTER the atomic NBT update succeeded
        if (appliedUpgrade[0] != null) {
            switch (appliedUpgrade[0]) {
                case "durability" -> {
                    mainHand.setDamage(Math.max(0, mainHand.getDamage() - 50));
                    offHand.decrement(4);
                    player.sendMessage(Text.translatable("message.void_echo.forge_durability_upgraded"), true);
                }
                case "echo_core" -> {
                    offHand.decrement(1);
                    player.sendMessage(Text.translatable("message.void_echo.forge_echo_upgraded"), true);
                }
                case "echo_strike" -> {
                    player.getInventory().remove(s -> s.isOf(ModItems.ECHO_CORE), 1);
                    offHand.decrement(1);
                    player.sendMessage(Text.translatable("message.void_echo.forge_echo_strike"), true);
                }
                case "echo_guard" -> {
                    offHand.decrement(3);
                    player.sendMessage(Text.translatable("message.void_echo.forge_echo_guard"), true);
                }
                case "rift" -> {
                    offHand.decrement(2);
                    player.sendMessage(Text.translatable("message.void_echo.forge_rift_upgraded"), true);
                }
                case "rift_walker" -> {
                    player.getInventory().remove(s -> s.isOf(ModBlocks.CRYSTAL_BLOCK.asItem()), 1);
                    offHand.decrement(1);
                    player.sendMessage(Text.translatable("message.void_echo.forge_rift_walker"), true);
                }
                case "rift_fury" -> {
                    player.getInventory().remove(s -> s.isOf(ModItems.VOID_CATALYST), 1);
                    offHand.decrement(1);
                    player.sendMessage(Text.translatable("message.void_echo.forge_rift_fury"), true);
                }
                case "void_resonance" -> {
                    offHand.decrement(1);
                    player.sendMessage(Text.translatable("message.void_echo.forge_void_resonance"), true);
                }
                case "crystal_barrier" -> {
                    offHand.decrement(1);
                    player.getInventory().remove(s -> s.isOf(ModBlocks.CRYSTAL_BLOCK.asItem()), 3);
                    player.sendMessage(Text.translatable("message.void_echo.forge_crystal_barrier"), true);
                }
            }
            playForgeEffects(world, pos);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private void playForgeEffects(World world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.5f);
    }
}
