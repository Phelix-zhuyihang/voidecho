package com.example.voidecho.item;

import com.example.voidecho.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

public class ModToolMaterials {
    public static final ToolMaterial VOID = new ToolMaterial() {
        @Override
        public int getDurability() { return 2031; }

        @Override
        public float getMiningSpeedMultiplier() { return 9.0f; }

        @Override
        public float getAttackDamage() { return 5.0f; }

        @Override
        public TagKey<Block> getInverseTag() { return BlockTags.INCORRECT_FOR_NETHERITE_TOOL; }

        @Override
        public int getEnchantability() { return 15; }

        @Override
        public Ingredient getRepairIngredient() { return Ingredient.ofItems(ModItems.VOID_ALLOY_INGOT); }
    };

    public static final ToolMaterial CRYSTAL = new ToolMaterial() {
        @Override
        public int getDurability() { return 500; }

        @Override
        public float getMiningSpeedMultiplier() { return 7.0f; }

        @Override
        public float getAttackDamage() { return 3.0f; }

        @Override
        public TagKey<Block> getInverseTag() { return BlockTags.INCORRECT_FOR_DIAMOND_TOOL; }

        @Override
        public int getEnchantability() { return 22; }

        @Override
        public Ingredient getRepairIngredient() { return Ingredient.ofItems(ModItems.CRYSTAL_SHARD); }
    };

    public static void init() {
        // Static fields initialise on class load.
    }
}
