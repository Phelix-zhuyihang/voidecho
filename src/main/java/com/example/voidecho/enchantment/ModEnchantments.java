package com.example.voidecho.enchantment;

import com.example.voidecho.VoidEcho;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEnchantments {
    public static final RegistryKey<Enchantment> VOID_AFFINITY = of("void_affinity");
    public static final RegistryKey<Enchantment> ECHO_PULSE = of("echo_pulse");
    public static final RegistryKey<Enchantment> PHASE_WALKER = of("phase_walker");
    public static final RegistryKey<Enchantment> VOID_LEECH = of("void_leech");
    public static final RegistryKey<Enchantment> CRYSTAL_SHIELD = of("crystal_shield");

    private static RegistryKey<Enchantment> of(String name) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(VoidEcho.MOD_ID, name));
    }

    public static void init() {
        // Forces static initialisation so all RegistryKeys are created.
    }
}
