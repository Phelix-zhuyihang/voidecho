package com.example.voidecho.item.armor;

import com.example.voidecho.item.ModItems;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class VoidArmorMaterial {
    public static final RegistryEntry<ArmorMaterial> VOID_ALLOY = Registry.registerReference(
            Registries.ARMOR_MATERIAL,
            Identifier.of("void_echo", "void_alloy"),
            new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.HELMET, 3);
                        map.put(ArmorItem.Type.CHESTPLATE, 8);
                        map.put(ArmorItem.Type.LEGGINGS, 6);
                        map.put(ArmorItem.Type.BOOTS, 3);
                    }),
                    20,
                    SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.ofItems(ModItems.VOID_ALLOY_INGOT),
                    List.of(new ArmorMaterial.Layer(Identifier.of("void_echo", "void_alloy"))),
                    3.0f,
                    0.15f
            )
    );

    public static void init() {
        // Static fields load on class initialisation.
    }
}
