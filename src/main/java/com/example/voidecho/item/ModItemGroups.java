package com.example.voidecho.item;

import com.example.voidecho.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup VOID_ECHO_TAB = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.VOID_HEART))
            .displayName(Text.translatable("itemGroup.void_echo_tab"))
            .entries((context, entries) -> {
                // Blocks
                entries.add(ModBlocks.VOID_STONE);
                entries.add(ModBlocks.VOID_STONE_BRICKS);
                entries.add(ModBlocks.CRACKED_VOID_STONE_BRICKS);
                entries.add(ModBlocks.VOID_DIRT);
                entries.add(ModBlocks.VOID_GRASS_BLOCK);
                entries.add(ModBlocks.CRYSTAL_ORE);
                entries.add(ModBlocks.DEEPSLATE_CRYSTAL_ORE);
                entries.add(ModBlocks.CRYSTAL_BLOCK);
                entries.add(ModBlocks.VOID_PORTAL_FRAME);
                entries.add(ModBlocks.ECHO_ALTAR);
                entries.add(ModBlocks.ECHO_SHARD_1);
                entries.add(ModBlocks.ECHO_SHARD_2);
                entries.add(ModBlocks.ECHO_SHARD_3);
                entries.add(ModBlocks.ECHO_SHARD_4);
                entries.add(ModBlocks.ECHO_SHARD_5);
                entries.add(ModBlocks.CRYSTAL_BLOOM);
                entries.add(ModBlocks.VOID_FORGE);
                entries.add(ModBlocks.RIFT_CORE);

                // Items
                entries.add(ModItems.VOID_KEY);
                entries.add(ModItems.VOID_HEART);
                entries.add(ModItems.ECHO_CORE);
                entries.add(ModItems.ECHO_AMULET);
                entries.add(ModItems.ECHO_TOME);
                entries.add(ModItems.VOID_CATALYST);
                entries.add(ModItems.VOID_ALLOY_INGOT);
                entries.add(ModItems.CRYSTAL_SHARD);
                entries.add(ModItems.CRYSTAL_BERRY);
                entries.add(ModItems.RIFT_FRAGMENT);
                entries.add(ModItems.RIFT_CORE);
                entries.add(ModItems.VOID_ECHO_JOURNAL);

                // Crystal Tools
                entries.add(ModItems.CRYSTAL_SWORD);
                entries.add(ModItems.CRYSTAL_PICKAXE);
                entries.add(ModItems.CRYSTAL_AXE);
                entries.add(ModItems.CRYSTAL_SHOVEL);
                entries.add(ModItems.CRYSTAL_HOE);

                // Void Fishing
                entries.add(ModItems.CRYSTAL_LURE);
                entries.add(ModItems.VOID_CARP);
                entries.add(ModItems.COOKED_VOID_CARP);
                entries.add(ModItems.CRYSTAL_RAY);
                entries.add(ModItems.VOID_CRAB_SHELL);
                entries.add(ModItems.RESONANT_CRYSTAL);
                entries.add(ModItems.AEROLITH_FRAGMENT);

                // Tools / Weapons
                entries.add(ModItems.VOID_SWORD);
                entries.add(ModItems.VOID_BOW);
                entries.add(ModItems.VOID_STAFF);
                entries.add(ModItems.VOID_SHIELD);

                // Armour
                entries.add(ModItems.VOID_HELMET);
                entries.add(ModItems.VOID_CHESTPLATE);
                entries.add(ModItems.VOID_LEGGINGS);
                entries.add(ModItems.VOID_BOOTS);

                // Spawn Eggs
                entries.add(ModItems.VOID_WORM_SPAWN_EGG);
                entries.add(ModItems.CRYSTAL_WRAITH_SPAWN_EGG);
                entries.add(ModItems.SHARD_GUARD_SPAWN_EGG);
                entries.add(ModItems.VOID_STALKER_SPAWN_EGG);
                entries.add(ModItems.ECHO_WARDEN_SPAWN_EGG);
                entries.add(ModItems.CRYSTAL_SPRITE_SPAWN_EGG);
                entries.add(ModItems.CRYSTAL_GUARDIAN_SPAWN_EGG);
                entries.add(ModItems.VOID_SHADE_SPAWN_EGG);
            })
            .build();

    public static void init() {
        Registry.register(Registries.ITEM_GROUP,
                Identifier.of("void_echo", "void_echo_tab"),
                VOID_ECHO_TAB);
    }
}
