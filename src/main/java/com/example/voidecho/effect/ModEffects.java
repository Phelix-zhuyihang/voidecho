package com.example.voidecho.effect;

import com.example.voidecho.VoidEcho;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class ModEffects {
    public static final RegistryEntry<StatusEffect> VOID_TOUCHED = Registry.registerReference(
            Registries.STATUS_EFFECT,
            Identifier.of(VoidEcho.MOD_ID, "void_touched"),
            new StatusEffect(StatusEffectCategory.HARMFUL, 0x4B0082) {
                @Override
                public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
                    entity.damage(entity.getDamageSources().magic(), 1.0f);
                    return true;
                }

                @Override
                public boolean canApplyUpdateEffect(int duration, int amplifier) {
                    int interval = 20 >> amplifier;
                    if (interval > 0) {
                        return duration % interval == 0;
                    }
                    return true;
                }
            }
    );

    public static final RegistryEntry<StatusEffect> ECHO_RESONANCE = Registry.registerReference(
            Registries.STATUS_EFFECT,
            Identifier.of(VoidEcho.MOD_ID, "echo_resonance"),
            new StatusEffect(StatusEffectCategory.HARMFUL, 0x8B008B) {
                @Override
                public boolean canApplyUpdateEffect(int duration, int amplifier) {
                    return true;
                }

                @Override
                public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
                    // Damage multiplier is handled via mixin in LivingEntityMixin
                    // This effect marks the entity for 20% increased damage taken
                    return true;
                }
            }
    );

    public static void init() {
        // Forces static initialisation of all effects
    }

    private ModEffects() {}
}
