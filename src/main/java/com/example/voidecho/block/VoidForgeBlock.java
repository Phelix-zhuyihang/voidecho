package com.example.voidecho.block;

import com.example.voidecho.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
        if (world.isClient) return ActionResult.SUCCESS;

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

        // Use a mutable result holder so inner lambdas can signal success
        ActionResult[] result = { ActionResult.PASS };

        // Use .apply() to merge NBT changes, preserving other mods' CUSTOM_DATA keys
        mainHand.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, existing -> {
            NbtCompound nbt = existing.copyNbt();

            // Crystal Shard: repair durability (costs 4 shards)
            if (offHand.isOf(ModItems.CRYSTAL_SHARD) && offHand.getCount() >= 4) {
                int durUpgrade = nbt.getInt("void_echo:durability_upgrade");
                if (durUpgrade >= 4) {
                    player.sendMessage(Text.translatable("message.void_echo.forge_max_durability"), true);
                    return existing; // no change
                }
                mainHand.setDamage(Math.max(0, mainHand.getDamage() - 50));
                nbt.putInt("void_echo:durability_upgrade", durUpgrade + 1);
                offHand.decrement(4);
                playForgeEffects(world, pos);
                player.sendMessage(Text.translatable("message.void_echo.forge_durability_upgraded"), true);
                result[0] = ActionResult.SUCCESS;
                return NbtComponent.of(nbt);
            }

            // Echo Core: base echo upgrade (tier 1) — +2 damage/armor
            if (offHand.isOf(ModItems.ECHO_CORE)) {
                if (nbt.contains("void_echo:echo_upgrade")) {
                    player.sendMessage(Text.translatable("message.void_echo.forge_already_echo"), true);
                    return existing;
                }
                nbt.remove("void_echo:rift_upgrade");
                nbt.remove("void_echo:rift_walker");
                nbt.remove("void_echo:rift_fury");
                nbt.putBoolean("void_echo:echo_upgrade", true);
                offHand.decrement(1);
                playForgeEffects(world, pos);
                player.sendMessage(Text.translatable("message.void_echo.forge_echo_upgraded"), true);
                result[0] = ActionResult.SUCCESS;
                return NbtComponent.of(nbt);
            }

            // Echo Strike (tier 2 echo, offensive)
            if (offHand.isOf(ModItems.VOID_CATALYST) && nbt.contains("void_echo:echo_upgrade")
                    && !nbt.contains("void_echo:echo_strike") && !nbt.contains("void_echo:echo_guard")) {
                if (player.getInventory().count(ModItems.ECHO_CORE) < 1) {
                    player.sendMessage(Text.translatable("message.void_echo.forge_need_echo_core"), true);
                    return existing;
                }
                player.getInventory().remove(s -> s.isOf(ModItems.ECHO_CORE), 1, null);
                nbt.putBoolean("void_echo:echo_strike", true);
                offHand.decrement(1);
                playForgeEffects(world, pos);
                player.sendMessage(Text.translatable("message.void_echo.forge_echo_strike"), true);
                result[0] = ActionResult.SUCCESS;
                return NbtComponent.of(nbt);
            }

            // Echo Guard (tier 2 echo, defensive)
            if (offHand.isOf(ModItems.RIFT_FRAGMENT) && offHand.getCount() >= 3
                    && nbt.contains("void_echo:echo_upgrade")
                    && !nbt.contains("void_echo:echo_strike") && !nbt.contains("void_echo:echo_guard")) {
                nbt.putBoolean("void_echo:echo_guard", true);
                offHand.decrement(3);
                playForgeEffects(world, pos);
                player.sendMessage(Text.translatable("message.void_echo.forge_echo_guard"), true);
                result[0] = ActionResult.SUCCESS;
                return NbtComponent.of(nbt);
            }

            // Rift Fragment: base rift upgrade (tier 1)
            if (offHand.isOf(ModItems.RIFT_FRAGMENT) && offHand.getCount() >= 2) {
                if (nbt.contains("void_echo:rift_upgrade")) {
                    player.sendMessage(Text.translatable("message.void_echo.forge_already_rift"), true);
                    return existing;
                }
                nbt.remove("void_echo:echo_upgrade");
                nbt.remove("void_echo:echo_strike");
                nbt.remove("void_echo:echo_guard");
                nbt.putBoolean("void_echo:rift_upgrade", true);
                offHand.decrement(2);
                playForgeEffects(world, pos);
                player.sendMessage(Text.translatable("message.void_echo.forge_rift_upgraded"), true);
                result[0] = ActionResult.SUCCESS;
                return NbtComponent.of(nbt);
            }

            // Rift Walker (tier 2 rift, utility)
            if (offHand.isOf(ModItems.VOID_CATALYST) && nbt.contains("void_echo:rift_upgrade")
                    && !nbt.contains("void_echo:rift_walker") && !nbt.contains("void_echo:rift_fury")) {
                if (player.getInventory().count(ModBlocks.CRYSTAL_BLOCK.asItem()) < 1) {
                    player.sendMessage(Text.translatable("message.void_echo.forge_need_crystal_block"), true);
                    return existing;
                }
                player.getInventory().remove(s -> s.isOf(ModBlocks.CRYSTAL_BLOCK.asItem()), 1, null);
                nbt.putBoolean("void_echo:rift_walker", true);
                offHand.decrement(1);
                playForgeEffects(world, pos);
                player.sendMessage(Text.translatable("message.void_echo.forge_rift_walker"), true);
                result[0] = ActionResult.SUCCESS;
                return NbtComponent.of(nbt);
            }

            // Rift Fury (tier 2 rift, offensive)
            if (offHand.isOf(ModItems.VOID_ALLOY_INGOT) && nbt.contains("void_echo:rift_upgrade")
                    && !nbt.contains("void_echo:rift_walker") && !nbt.contains("void_echo:rift_fury")) {
                if (player.getInventory().count(ModItems.VOID_CATALYST) < 1) {
                    player.sendMessage(Text.translatable("message.void_echo.forge_need_catalyst"), true);
                    return existing;
                }
                player.getInventory().remove(s -> s.isOf(ModItems.VOID_CATALYST), 1, null);
                nbt.putBoolean("void_echo:rift_fury", true);
                offHand.decrement(1);
                playForgeEffects(world, pos);
                player.sendMessage(Text.translatable("message.void_echo.forge_rift_fury"), true);
                result[0] = ActionResult.SUCCESS;
                return NbtComponent.of(nbt);
            }

            return existing; // No matching upgrade
        });

        return result[0];
    }

    private void playForgeEffects(World world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.5f);
    }
}
