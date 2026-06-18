package com.example.voidecho.item;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.ModEntities;
import com.example.voidecho.item.armor.VoidArmorItem;
import com.example.voidecho.item.armor.VoidArmorMaterial;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.List;

public class ModItems {
    // ---- Items ----
    private static final Text tooltip(String key, Formatting... formats) {
        return Text.translatable(key).formatted(formats);
    }

    public static final Item VOID_KEY = register("void_key",
            new TooltippedItem(new Item.Settings().rarity(Rarity.RARE).maxCount(1).fireproof(),
                    tooltip("tooltip.void_echo.void_key", Formatting.GRAY), true));

    public static final Item VOID_HEART = register("void_heart",
            new TooltippedItem(new Item.Settings().rarity(Rarity.EPIC).maxCount(1).fireproof(),
                    tooltip("tooltip.void_echo.void_heart", Formatting.LIGHT_PURPLE), true));

    public static final Item ECHO_CORE = register("echo_core",
            new TooltippedItem(new Item.Settings().rarity(Rarity.EPIC).maxCount(1).fireproof(),
                    tooltip("tooltip.void_echo.echo_core", Formatting.AQUA), true));

    public static final Item VOID_ALLOY_INGOT = register("void_alloy_ingot",
            new TooltippedItem(new Item.Settings().rarity(Rarity.RARE).fireproof(),
                    tooltip("tooltip.void_echo.void_alloy_ingot", Formatting.GRAY), false));

    public static final Item CRYSTAL_SHARD = register("crystal_shard",
            new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));

    public static final Item CRYSTAL_BERRY = register("crystal_berry",
            new Item(new Item.Settings()
                    .food(new FoodComponent.Builder().nutrition(2).saturationModifier(0.3f).snack().build())));

    // Rift Core
    public static final Item RIFT_CORE = register("rift_core",
            new Item(new Item.Settings().rarity(Rarity.EPIC).fireproof()));

    // ---- Guide Book ----
    public static final Item VOID_ECHO_JOURNAL = register("void_echo_journal",
            new VoidEchoJournalItem());

    // ---- End-Game Items ----
    public static final Item ECHO_AMULET = register("echo_amulet",
            new EchoAmuletItem());

    public static final Item ECHO_TOME = register("echo_tome",
            new EchoTomeItem());

    public static final Item VOID_CATALYST = register("void_catalyst",
            new Item(new Item.Settings().rarity(Rarity.RARE).fireproof().maxCount(1)));

    // ---- Tools / Weapons ----
    public static final Item VOID_SWORD = register("void_sword",
            new VoidSwordItem(ModToolMaterials.VOID,
                    new Item.Settings().rarity(Rarity.EPIC).fireproof()));

    public static final Item VOID_BOW = register("void_bow",
            new VoidBowItem(new Item.Settings().maxDamage(500).rarity(Rarity.EPIC).fireproof()));

    public static final Item VOID_STAFF = register("void_staff",
            new VoidStaffItem(new Item.Settings().maxDamage(250).rarity(Rarity.EPIC).fireproof()));

    // ---- Crystal Tools (mid-tier) ----
    public static final Item CRYSTAL_SWORD = register("crystal_sword",
            new SwordItem(ModToolMaterials.CRYSTAL,
                    new Item.Settings().rarity(Rarity.UNCOMMON).maxDamage(500)));

    public static final Item CRYSTAL_PICKAXE = register("crystal_pickaxe",
            new PickaxeItem(ModToolMaterials.CRYSTAL,
                    new Item.Settings().rarity(Rarity.UNCOMMON).maxDamage(500)));

    public static final Item CRYSTAL_AXE = register("crystal_axe",
            new AxeItem(ModToolMaterials.CRYSTAL,
                    new Item.Settings().rarity(Rarity.UNCOMMON).maxDamage(500)));

    public static final Item CRYSTAL_SHOVEL = register("crystal_shovel",
            new ShovelItem(ModToolMaterials.CRYSTAL,
                    new Item.Settings().rarity(Rarity.UNCOMMON).maxDamage(500)));

    public static final Item CRYSTAL_HOE = register("crystal_hoe",
            new HoeItem(ModToolMaterials.CRYSTAL,
                    new Item.Settings().rarity(Rarity.UNCOMMON).maxDamage(500)));

    // ---- Void Fishing ----
    public static final Item CRYSTAL_LURE = register("crystal_lure",
            new Item(new Item.Settings().maxDamage(128).rarity(Rarity.UNCOMMON)));

    public static final Item VOID_CARP = register("void_carp",
            new Item(new Item.Settings().food(
                    new FoodComponent.Builder().nutrition(3).saturationModifier(0.6f).build())));

    public static final Item COOKED_VOID_CARP = register("cooked_void_carp",
            new Item(new Item.Settings().food(
                    new FoodComponent.Builder().nutrition(5).saturationModifier(1.2f).build())));

    public static final Item CRYSTAL_RAY = register("crystal_ray",
            new Item(new Item.Settings().food(
                    new FoodComponent.Builder().nutrition(2).saturationModifier(0.4f)
                            .statusEffect(new StatusEffectInstance(
                                    StatusEffects.REGENERATION, 600, 0), 1.0f)
                            .build())));

    public static final Item VOID_CRAB_SHELL = register("void_crab_shell",
            new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));

    public static final Item RESONANT_CRYSTAL = register("resonant_crystal",
            new Item(new Item.Settings().rarity(Rarity.RARE).fireproof()));

    public static final Item AEROLITH_FRAGMENT = register("aerolith_fragment",
            new Item(new Item.Settings().rarity(Rarity.RARE)));

    // ---- Armour ----
    public static final Item VOID_HELMET = register("void_helmet",
            new VoidArmorItem(VoidArmorMaterial.VOID_ALLOY, ArmorItem.Type.HELMET,
                    new Item.Settings().rarity(Rarity.EPIC).fireproof()));

    public static final Item VOID_CHESTPLATE = register("void_chestplate",
            new VoidArmorItem(VoidArmorMaterial.VOID_ALLOY, ArmorItem.Type.CHESTPLATE,
                    new Item.Settings().rarity(Rarity.EPIC).fireproof()));

    public static final Item VOID_LEGGINGS = register("void_leggings",
            new VoidArmorItem(VoidArmorMaterial.VOID_ALLOY, ArmorItem.Type.LEGGINGS,
                    new Item.Settings().rarity(Rarity.EPIC).fireproof()));

    public static final Item VOID_BOOTS = register("void_boots",
            new VoidArmorItem(VoidArmorMaterial.VOID_ALLOY, ArmorItem.Type.BOOTS,
                    new Item.Settings().rarity(Rarity.EPIC).fireproof()));

    // ---- Spawn Eggs ----
    public static final Item VOID_WORM_SPAWN_EGG = register("void_worm_spawn_egg",
            new SpawnEggItem(ModEntities.VOID_WORM, 0x2D004D, 0x7B2D8E,
                    new Item.Settings().rarity(Rarity.EPIC)));

    public static final Item CRYSTAL_WRAITH_SPAWN_EGG = register("crystal_wraith_spawn_egg",
            new SpawnEggItem(ModEntities.CRYSTAL_WRAITH, 0x88DDFF, 0xFFFFFF,
                    new Item.Settings().rarity(Rarity.EPIC)));

    public static final Item SHARD_GUARD_SPAWN_EGG = register("shard_guard_spawn_egg",
            new SpawnEggItem(ModEntities.SHARD_GUARD, 0x666666, 0x9933CC,
                    new Item.Settings().rarity(Rarity.EPIC)));

    public static final Item VOID_STALKER_SPAWN_EGG = register("void_stalker_spawn_egg",
            new SpawnEggItem(ModEntities.VOID_STALKER, 0x111111, 0x4A0080,
                    new Item.Settings().rarity(Rarity.EPIC)));

    public static final Item ECHO_WARDEN_SPAWN_EGG = register("echo_warden_spawn_egg",
            new SpawnEggItem(ModEntities.ECHO_WARDEN, 0x004466, 0x00AACC,
                    new Item.Settings().rarity(Rarity.EPIC)));

    // ---- F11: Crystal Sprite ----
    public static final Item CRYSTAL_SPRITE_SPAWN_EGG = register("crystal_sprite_spawn_egg",
            new SpawnEggItem(ModEntities.CRYSTAL_SPRITE, 0x88DDFF, 0xCCEEFF,
                    new Item.Settings().rarity(Rarity.UNCOMMON)));

    // ---- B2: Crystal Guardian ----
    public static final Item CRYSTAL_GUARDIAN_SPAWN_EGG = register("crystal_guardian_spawn_egg",
            new SpawnEggItem(ModEntities.CRYSTAL_GUARDIAN, 0x00DDDD, 0xFFDD00,
                    new Item.Settings().rarity(Rarity.EPIC)));

    // ---- B3: Void Shade ----
    public static final Item VOID_SHADE_SPAWN_EGG = register("void_shade_spawn_egg",
            new SpawnEggItem(ModEntities.VOID_SHADE, 0x2D004D, 0x7B2D8E,
                    new Item.Settings().rarity(Rarity.EPIC)));

    // ---- F12: Rift Fragment ----
    public static final Item RIFT_FRAGMENT = register("rift_fragment",
            new RiftFragmentItem(new Item.Settings().rarity(Rarity.RARE).fireproof()));

    // ---- Helper ----
    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(VoidEcho.MOD_ID, name), item);
    }

    public static void init() {
        // static fields initialise on class load
    }

    // ---- Custom item subclass with glow + tooltip support ----
    private static class TooltippedItem extends Item {
        private final List<Text> tooltipLines;
        private final boolean glowing;

        public TooltippedItem(Settings settings, Text tooltip, boolean glowing) {
            super(settings);
            this.tooltipLines = List.of(tooltip);
            this.glowing = glowing;
        }

        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
            tooltip.addAll(tooltipLines);
        }

        @Override
        public boolean hasGlint(ItemStack stack) {
            return glowing || super.hasGlint(stack);
        }
    }
}
