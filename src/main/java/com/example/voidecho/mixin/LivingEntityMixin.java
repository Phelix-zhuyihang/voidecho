package com.example.voidecho.mixin;

import com.example.voidecho.config.ModConfig;
import com.example.voidecho.effect.ModEffects;
import com.example.voidecho.world.dimension.ModDimensions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 1000)
public abstract class LivingEntityMixin extends Entity {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract ItemStack getEquippedStack(net.minecraft.entity.EquipmentSlot slot);

    @Shadow
    public abstract boolean addStatusEffect(StatusEffectInstance effect);

    private int countVoidAlloyPieces() {
        if (!((Object) this instanceof PlayerEntity player)) {
            return 0;
        }
        int pieces = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.getItem() instanceof com.example.voidecho.item.armor.VoidArmorItem) {
                pieces++;
            }
        }
        return pieces;
    }

    /**
     * Apply Void Alloy armor set bonuses: speed per piece, full set immunity to fall/void damage.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void voidEcho$applyArmorSetBonus(CallbackInfo ci) {
        if (!((Object) this instanceof PlayerEntity player)) {
            return;
        }

        // Count void alloy armor pieces
        int pieces = countVoidAlloyPieces();

        // Full set bonus: handle fall damage immunity
        if (pieces >= 4) {
            if (player.fallDistance > 2.0f) {
                StatusEffectInstance existing = player.getStatusEffect(StatusEffects.SLOW_FALLING);
                if (existing == null || existing.getDuration() <= 30) {
                    player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SLOW_FALLING, 60, 0,
                        false, false, true
                    ));
                }
            }
            StatusEffectInstance fireRes = player.getStatusEffect(StatusEffects.FIRE_RESISTANCE);
            if (fireRes == null || fireRes.getDuration() <= 30) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.FIRE_RESISTANCE, 100, 0,
                    false, false, true
                ));
            }
        }
    }

    /**
     * Cancel fall damage when wearing full Void Alloy set.
     */
    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void voidEcho$cancelFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof PlayerEntity player)) {
            return;
        }

        int pieces = countVoidAlloyPieces();

        if (pieces >= 4) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Cancel void damage (falling out of world) when wearing full set.
     */
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void voidEcho$cancelVoidDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof PlayerEntity player)) {
            return;
        }
        if (!source.isOf(DamageTypes.OUT_OF_WORLD)) {
            return;
        }

        int pieces = countVoidAlloyPieces();

        if (pieces >= 4) {
            // Instead of dying, teleport to the world surface.
            // Ensure we teleport well above the minimum world height to avoid
            // re-triggering void damage and creating an infinite loop.
            BlockPos safePos = player.getWorld().getTopPosition(
                Heightmap.Type.WORLD_SURFACE, player.getBlockPos());
            int minSafeY = player.getWorld().getBottomY() + 10;
            int targetY = Math.max(safePos.getY() + 2, minSafeY);
            player.teleport(player.getX(), targetY, player.getZ(), false);
            // Grant brief invulnerability window and slow falling
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 40, 4, // 2 seconds of high resistance
                false, false, true
            ));
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOW_FALLING, 200, 0,
                false, false, true
            ));
            cir.setReturnValue(false);
        }
    }

    /**
     * Echo Resonance: +20% damage taken when target has the effect.
     * Void dimension difficulty scaling.
     *
     * IMPORTANT: This @ModifyVariable (priority 1000) and EnchantmentEffectMixin's
     * @ModifyVariable (priority 999) both modify the first float argument of
     * damage(). Do NOT add a third modifier without auditing the interaction.
     */
    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float voidEcho$modifyIncomingDamage(float amount, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;

        // If this is void damage and player has full void set, cancelVoidDamage
        // (below) will handle it — skip scaling to avoid wasted computation.
        if (source.isOf(DamageTypes.OUT_OF_WORLD) && self instanceof PlayerEntity) {
            if (countVoidAlloyPieces() >= 4) return amount;
        }

        float modified = amount;
        // Echo Resonance: +20% damage
        if (self.hasStatusEffect(ModEffects.ECHO_RESONANCE)) {
            modified *= 1.2f;
        }
        // Void dimension difficulty scaling
        if (self.getWorld().getRegistryKey().equals(ModDimensions.VOIDS_END_LEVEL_KEY)) {
            modified *= (float) ModConfig.getInstance().voidDimensionDifficulty;
        }
        return modified;
    }

    /**
     * Void Forge Echo Guard (echo branch tier 2): When damaged, 10% chance to absorb damage and heal.
     * Void Forge Rift Upgrade on armor: When damaged, small chance to damage attacker back.
     */
    @Inject(method = "damage", at = @At("TAIL"))
    private void voidEcho$riftUpgradeArmor(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (amount <= 0 || this.getWorld().isClient) return;
        LivingEntity self = (LivingEntity) (Object) this;

        // Check all armor for rift upgrade
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armorStack = self.getEquippedStack(slot);
            if (armorStack.isEmpty() || !armorStack.contains(DataComponentTypes.CUSTOM_DATA)) continue;
            net.minecraft.nbt.NbtCompound armorNbt = armorStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();

            // Echo Guard: 10% chance to absorb damage and heal
            if (armorNbt.getBoolean("void_echo:echo_guard") && self.getRandom().nextFloat() < 0.10f) {
                self.heal(amount * 0.5f);
                if (self.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL,
                        self.getX(), self.getY() + 0.5, self.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
                }
                self.getWorld().playSound(null, self.getX(), self.getY(), self.getZ(),
                    SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, net.minecraft.sound.SoundCategory.PLAYERS, 0.5f, 2.0f);
                break;
            }

            if (armorNbt.getBoolean("void_echo:rift_upgrade")) {
                Entity attacker = source.getAttacker();
                if (attacker instanceof LivingEntity && self.getRandom().nextFloat() < 0.2f) {
                    attacker.damage(self.getDamageSources().thrown(self, self), 2.0f);
                    if (self.getWorld() instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(ParticleTypes.PORTAL,
                                attacker.getX(), attacker.getY() + 1.0, attacker.getZ(),
                                8, 0.3, 0.3, 0.3, 0.1);
                    }
                }
                break; // Only check once
            }
        }
    }

}
