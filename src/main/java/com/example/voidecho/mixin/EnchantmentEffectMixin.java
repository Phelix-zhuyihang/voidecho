package com.example.voidecho.mixin;

import com.example.voidecho.enchantment.ModEnchantments;
import com.example.voidecho.effect.ModEffects;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 999)
public abstract class EnchantmentEffectMixin extends Entity {

    private static final ThreadLocal<Boolean> ECHO_PULSE_GUARD =
            ThreadLocal.withInitial(() -> false);

    public EnchantmentEffectMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract ItemStack getEquippedStack(net.minecraft.entity.EquipmentSlot slot);

    private static int getEnchantmentLevel(LivingEntity entity, RegistryKey<Enchantment> key, ItemStack stack) {
        RegistryEntry<Enchantment> entry = entity.getWorld().getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT).getEntry(key).orElse(null);
        if (entry == null) return 0;
        return EnchantmentHelper.getLevel(entry, stack);
    }

    /**
     * Void Affinity: Bonus damage in Voids End dimension when holding a void affinity weapon.
     * Echo Pulse: When damaged, chance to emit a sonic burst from chestplate with echo_pulse.
     * Void Leech: When dealing damage, heal if target has void_touched and weapon has void_leech.
     * Crystal Shield: When hit by projectile, chance to reflect with crystal_shield chestplate.
     */
    @Inject(method = "damage", at = @At("HEAD"))
    private void voidEcho$enchantmentEffects(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        // Skip void damage — LivingEntityMixin (priority 1000) cancels it,
        // and playing effects here would show false visual/sound feedback.
        if (source.isOf(DamageTypes.OUT_OF_WORLD)) return;

        // --- Crystal Barrier (tier 3 chestplate): auto-shield every 60s ---
        if (!self.getWorld().isClient && amount > 0) {
            ItemStack chestStack = self.getEquippedStack(EquipmentSlot.CHEST);
            if (!chestStack.isEmpty() && chestStack.contains(DataComponentTypes.CUSTOM_DATA)) {
                net.minecraft.nbt.NbtCompound nbt = chestStack.getOrDefault(
                        DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
                if (nbt.getBoolean("void_echo:crystal_barrier")) {
                    long now = self.getWorld().getTime();
                    long lastShield = nbt.getLong("void_echo:barrier_cooldown");
                    if (now - lastShield >= 1200) { // 60 seconds
                        // Absorb up to 10 damage by healing before damage applies
                        float absorbed = Math.min(amount, 10.0f);
                        self.heal(absorbed);
                        nbt.putLong("void_echo:barrier_cooldown", now);
                        chestStack.apply(DataComponentTypes.CUSTOM_DATA,
                                NbtComponent.of(nbt), existing -> NbtComponent.of(nbt));
                        if (self.getWorld() instanceof ServerWorld sw) {
                            sw.spawnParticles(ParticleTypes.ENCHANT,
                                self.getX(), self.getY() + 1.0, self.getZ(),
                                20, 0.5, 1.0, 0.5, 0.1);
                        }
                        self.getWorld().playSound(null, self.getX(), self.getY(), self.getZ(),
                            SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                            SoundCategory.PLAYERS, 0.6f, 1.5f);
                    }
                }
            }
        }

        // --- Echo Pulse: When damaged, chance to unleash sonic burst ---
        if (!self.getWorld().isClient && amount > 0) {
            ItemStack chestStack = self.getEquippedStack(EquipmentSlot.CHEST);
            if (!chestStack.isEmpty()) {
                int echoPulseLevel = getEnchantmentLevel(self, ModEnchantments.ECHO_PULSE, chestStack);
                if (echoPulseLevel > 0 && self.getRandom().nextFloat() < 0.15f * echoPulseLevel) {
                    if (ECHO_PULSE_GUARD.get()) return; // Prevent infinite recursion
                    ECHO_PULSE_GUARD.set(true);
                    try {
                        Box searchBox = self.getBoundingBox().expand(3.0);
                        for (Entity nearby : self.getWorld().getOtherEntities(self, searchBox, e -> e instanceof LivingEntity && e.isAlive())) {
                            LivingEntity target = (LivingEntity) nearby;
                            target.damage(self.getDamageSources().sonicBoom(self), 4.0f * echoPulseLevel);
                        }
                        self.getWorld().playSound(null, self.getX(), self.getY(), self.getZ(),
                                SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.HOSTILE, 1.0f, 1.0f);
                        if (self.getWorld() instanceof ServerWorld serverWorld) {
                            serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM,
                                    self.getX(), self.getY() + 1.0, self.getZ(),
                                    1, 0, 0, 0, 0);
                        }
                    } finally {
                        ECHO_PULSE_GUARD.set(false);
                    }
                }
            }
        }

        // --- Crystal Shield: Reflect projectiles ---
        if (!self.getWorld().isClient && amount > 0 && source.isIn(DamageTypeTags.IS_PROJECTILE)) {
            ItemStack chestStack = self.getEquippedStack(EquipmentSlot.CHEST);
            if (!chestStack.isEmpty()) {
                int crystalShieldLevel = getEnchantmentLevel(self, ModEnchantments.CRYSTAL_SHIELD, chestStack);
                if (crystalShieldLevel > 0 && self.getRandom().nextFloat() < 0.15f * crystalShieldLevel) {
                    Entity attacker = source.getAttacker();
                    if (attacker != null) {
                        attacker.damage(self.getDamageSources().thrown(self, self), 3.0f * crystalShieldLevel);
                        if (self.getWorld() instanceof ServerWorld serverWorld) {
                            serverWorld.spawnParticles(ParticleTypes.CRIT,
                                    self.getX(), self.getY() + 1.0, self.getZ(),
                                    10, 0.5, 0.5, 0.5, 0.1);
                        }
                        self.getWorld().playSound(null, self.getX(), self.getY(), self.getZ(),
                                SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1.0f, 1.5f);
                    }
                }
            }
        }

        // The following effects apply when *this entity is the attacker*, handled in the returnable.
        // Void Leech and Void Affinity are handled in a ModifyVariable on the damage value.
    }

    /**
     * Void Leech: When dealing damage with void_leech weapon to a void_touched target, heal the attacker.
     * Void Affinity: When in voids_end dimension with void_affinity weapon, bonus damage.
     *
     * IMPORTANT: This @ModifyVariable (priority 999) and LivingEntityMixin's
     * @ModifyVariable (priority 1000) both modify the first float argument of
     * damage(). Do NOT add a third modifier without auditing the interaction.
     */
    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float voidEcho$modifyDamage(float amount, DamageSource source) {
        if (amount <= 0) return amount;

        Entity attacker = source.getAttacker();
        if (!(attacker instanceof LivingEntity livingAttacker)) return amount;

        ItemStack weapon = livingAttacker.getMainHandStack();
        if (weapon.isEmpty()) return amount;

        // Void Affinity: +2.0 * level damage in voids_end dimension
        int voidAffinityLevel = getEnchantmentLevel(livingAttacker, ModEnchantments.VOID_AFFINITY, weapon);
        if (voidAffinityLevel > 0) {
            if (livingAttacker.getWorld().getRegistryKey().equals(
                    com.example.voidecho.VoidEcho.VOIDS_END_DIMENSION_KEY)) {
                amount += 2.0f * voidAffinityLevel;
            }
        }

        // Void Forge Echo Upgrade: +2 bonus damage
        if (!weapon.contains(DataComponentTypes.CUSTOM_DATA)) return amount;
        net.minecraft.nbt.NbtCompound weaponNbt = weapon.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        if (weaponNbt.getBoolean("void_echo:echo_upgrade")) {
            amount += 2.0f;
        }
        // Echo Strike (tier 2 echo): 10% chance for 2× damage
        if (weaponNbt.getBoolean("void_echo:echo_strike") && livingAttacker.getRandom().nextFloat() < 0.10f) {
            amount *= 2.0f;
            ((LivingEntity) (Object) this).getWorld().playSound(null,
                ((LivingEntity) (Object) this).getX(), ((LivingEntity) (Object) this).getY(), ((LivingEntity) (Object) this).getZ(),
                SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 0.5f, 2.0f);
        }

        // Void Resonance (tier 3): 15% chance for AOE void pulse on attack
        if (weaponNbt.getBoolean("void_echo:void_resonance")
                && livingAttacker.getRandom().nextFloat() < 0.15f) {
            LivingEntity self = (LivingEntity) (Object) this;
            Box aoeBox = self.getBoundingBox().expand(3.0);
            for (Entity nearby : self.getWorld().getOtherEntities(self, aoeBox,
                    e -> e instanceof LivingEntity && e.isAlive())) {
                ((LivingEntity) nearby).damage(livingAttacker.getDamageSources().magic(), 4.0f);
            }
            if (self.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.SONIC_BOOM,
                    self.getX(), self.getY() + 1.0, self.getZ(),
                    10, 2.0, 1.0, 2.0, 0.1);
            }
            self.getWorld().playSound(null, self.getX(), self.getY(), self.getZ(),
                SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 0.4f, 1.8f);
        }

        return amount;
    }

    /**
     * Void Leech: Heal attacker AFTER damage is confirmed.
     * Moving this to TAIL prevents healing when damage was cancelled (e.g. shield block).
     */
    @Inject(method = "damage", at = @At("TAIL"))
    private void voidEcho$voidLeechAfterDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;

        Entity attacker = source.getAttacker();
        if (!(attacker instanceof LivingEntity livingAttacker)) return;

        ItemStack weapon = livingAttacker.getMainHandStack();
        if (weapon.isEmpty()) return;

        int voidLeechLevel = getEnchantmentLevel(livingAttacker, ModEnchantments.VOID_LEECH, weapon);
        if (voidLeechLevel > 0) {
            LivingEntity self = (LivingEntity) (Object) this;
            if (self.hasStatusEffect(ModEffects.VOID_TOUCHED)) {
                livingAttacker.heal(1.0f * voidLeechLevel);
            }
        }
    }

    /**
     * Phase Walker: When sneaking with leggings enchanted with phase_walker,
     * phase through a solid wall if there is space on the other side.
     */
    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void voidEcho$phaseWalkerCheck(CallbackInfo ci) {
        if (!((Object) this instanceof PlayerEntity player)) return;
        if (!player.isSneaking()) return;

        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        if (leggings.isEmpty()) return;

        int level = getEnchantmentLevel(player, ModEnchantments.PHASE_WALKER, leggings);
        if (level == 0) return;

        // Check cooldown
        if (player.getItemCooldownManager().isCoolingDown(leggings.getItem())) return;

        // Check block in look direction
        Vec3d look = player.getRotationVector();
        BlockPos frontPos = BlockPos.ofFloored(
            player.getX() + look.x * 1.5,
            player.getY() + player.getStandingEyeHeight(),
            player.getZ() + look.z * 1.5
        );
        BlockState frontState = player.getWorld().getBlockState(frontPos);
        BlockPos beyondPos = BlockPos.ofFloored(
            player.getX() + look.x * 3.0,
            player.getY() + player.getStandingEyeHeight(),
            player.getZ() + look.z * 3.0
        );
        BlockState beyondState = player.getWorld().getBlockState(beyondPos);

        // If front is solid and beyond is air (or not solid), phase through
        if (frontState.isSolid() && (beyondState.isAir() || !beyondState.isSolid())) {
            player.teleport(beyondPos.getX() + 0.5, beyondPos.getY(), beyondPos.getZ() + 0.5, false);
            player.getItemCooldownManager().set(leggings.getItem(), 100);
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.5f, 1.5f);
        }
    }
}
