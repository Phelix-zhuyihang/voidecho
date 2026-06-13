package com.example.voidecho.entity.mob;

import com.example.voidecho.entity.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import com.example.voidecho.ModSoundEvents;
import com.example.voidecho.item.ModItems;

import java.util.EnumSet;

public class ShardGuardEntity extends HostileEntity {
    private int chargeCooldown = 0;
    private boolean isCharging = false;
    private int chargeTimer = 0;

    public ShardGuardEntity(EntityType<? extends ShardGuardEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 10;
    }

    @SuppressWarnings("unused")
    public static boolean canSpawn(EntityType<ShardGuardEntity> type, ServerWorldAccess world,
                                    SpawnReason reason, BlockPos pos, Random random) {
        return HostileEntity.canSpawnInDark(type, world, reason, pos, random);
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0)
                .add(EntityAttributes.GENERIC_ARMOR, 4.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.GENERIC_STEP_HEIGHT, 1.5);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new ShardGuardChargeGoal(this));
        this.goalSelector.add(3, new MeleeAttackGoal(this, 0.8, false));
        this.goalSelector.add(4, new WanderAroundGoal(this, 0.4));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 10.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new RevengeGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient) {
            return;
        }

        if (this.chargeCooldown > 0) {
            this.chargeCooldown--;
        }

        if (this.isCharging) {
            this.chargeTimer--;
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                // Charge towards target with high speed
                this.getNavigation().startMovingTo(target, 1.5);
                // Deal damage on contact
                if (this.distanceTo(target) < 2.0f) {
                    target.damage(target.getDamageSources().mobAttack(this), 12.0f);
                    target.takeKnockback(1.5, target.getX() - this.getX(), target.getZ() - this.getZ());
                    this.chargeCooldown = 100 + this.random.nextInt(60);
                    this.isCharging = false;
                }
            } else {
                this.isCharging = false;
            }
            if (this.chargeTimer <= 0) {
                this.isCharging = false;
                this.chargeCooldown = 100 + this.random.nextInt(60); // 5-8 seconds cooldown
            }
        }
    }

    @Override
    public void takeKnockback(double strength, double x, double z) {
        // Immune to knockback
    }

    private void startCharge() {
        this.isCharging = true;
        this.chargeTimer = 30; // 1.5 seconds charge
        this.playSound(ModSoundEvents.ENTITY_SHARD_GUARD_HURT, 1.0f, 0.8f);
    }

    private static class ShardGuardChargeGoal extends Goal {
        private final ShardGuardEntity guard;

        public ShardGuardChargeGoal(ShardGuardEntity guard) {
            this.guard = guard;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (guard.isCharging) return false;
            if (guard.chargeCooldown > 0) {
                return false;
            }
            LivingEntity target = guard.getTarget();
            if (target == null || !target.isAlive()) return false;
            return guard.squaredDistanceTo(target) <= 64.0; // 8 blocks
        }

        @Override
        public void start() {
            guard.startCharge();
        }

        @Override
        public boolean shouldContinue() {
            return guard.isCharging;
        }
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean allowDrops) {
        super.dropEquipment(world, source, allowDrops);
        // Void alloy ingot (10% chance)
        if (this.random.nextFloat() < 0.1f) {
            this.dropItem(ModItems.VOID_ALLOY_INGOT);
        }
        // Crystal shards (1-3)
        int shardCount = 1 + this.random.nextInt(3);
        for (int i = 0; i < shardCount; i++) {
            this.dropItem(ModItems.CRYSTAL_SHARD);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("ChargeCooldown", this.chargeCooldown);
        nbt.putBoolean("IsCharging", this.isCharging);
        nbt.putInt("ChargeTimer", this.chargeTimer);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.chargeCooldown = nbt.getInt("ChargeCooldown");
        this.isCharging = nbt.getBoolean("IsCharging");
        this.chargeTimer = nbt.getInt("ChargeTimer");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundEvents.ENTITY_SHARD_GUARD_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSoundEvents.ENTITY_SHARD_GUARD_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundEvents.ENTITY_SHARD_GUARD_DEATH;
    }
}
