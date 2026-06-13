package com.example.voidecho.entity.mob;

import com.example.voidecho.ModSoundEvents;
import com.example.voidecho.entity.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import com.example.voidecho.item.ModItems;

import java.util.EnumSet;

public class CrystalWraithEntity extends FlyingEntity {
    private int diveCooldown = 0;
    private boolean isDiving = false;
    private int heightCheckCounter = 0;

    public boolean isDiving() {
        return isDiving;
    }
    private int diveTimer = 0;
    private Vec3d retreatTarget = null;

    public CrystalWraithEntity(EntityType<? extends CrystalWraithEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 8;
    }

    @SuppressWarnings("unused")
    public static boolean canSpawn(EntityType<CrystalWraithEntity> type, ServerWorldAccess world,
                                    SpawnReason reason, BlockPos pos, Random random) {
        return world.getBaseLightLevel(pos, 0) <= 7
                && world.getBlockState(pos.down()).isSolid()
                && MobEntity.canMobSpawn(type, world, reason, pos, random);
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.4);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new CrystalWraithDiveGoal(this));
        this.goalSelector.add(3, new CrystalWraithFloatGoal(this));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
        this.goalSelector.add(5, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new CrystalWraithRevengeGoal(this));
    }

    // Custom revenge targeting compatible with FlyingEntity (which can't use RevengeGoal)
    private static class CrystalWraithRevengeGoal extends Goal {
        private final CrystalWraithEntity wraith;

        public CrystalWraithRevengeGoal(CrystalWraithEntity wraith) {
            this.wraith = wraith;
            this.setControls(EnumSet.of(Control.TARGET));
        }

        @Override
        public boolean canStart() {
            LivingEntity attacker = wraith.getAttacker();
            return attacker != null && attacker.isAlive()
                    && !(attacker instanceof PlayerEntity); // Players handled by ActiveTargetGoal
        }

        @Override
        public void start() {
            wraith.setTarget(wraith.getAttacker());
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient) {
            // Spawn ghostly particles
            if (this.random.nextFloat() < 0.3f) {
                this.getWorld().addParticle(
                        ParticleTypes.MYCELIUM,
                        this.getX() + (this.random.nextDouble() - 0.5) * 1.5,
                        this.getY() + this.random.nextDouble() * 1.5,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 1.5,
                        0, 0, 0
                );
            }
            return;
        }

        if (this.diveCooldown > 0) {
            this.diveCooldown--;
        }

        if (this.isDiving) {
            this.diveTimer--;
            if (this.diveTimer <= 0) {
                this.isDiving = false;
                this.setNoGravity(true);
            }
        }

        // Maintain height advantage (check every 5 ticks)
        this.heightCheckCounter++;
        if (this.heightCheckCounter % 5 == 0) {
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive() && !this.isDiving) {
                double targetY = target.getY() + 3.0 + this.random.nextDouble() * 2.0;
                if (this.getY() < targetY - 1.0) {
                    this.setVelocity(this.getVelocity().add(0, 0.05, 0));
                } else if (this.getY() > targetY + 3.0) {
                    this.setVelocity(this.getVelocity().add(0, -0.03, 0));
                }
            }
        }
    }

    private void startDive() {
        this.isDiving = true;
        this.diveTimer = 15; // 0.75 seconds dive duration
        this.setNoGravity(false);

        LivingEntity target = this.getTarget();
        if (target != null) {
            Vec3d diveVec = target.getPos().subtract(this.getPos()).normalize().multiply(1.5);
            this.setVelocity(diveVec.x, diveVec.y - 0.3, diveVec.z);
            this.velocityDirty = true;

            if (this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                        ParticleTypes.PORTAL,
                        this.getX(), this.getY(), this.getZ(),
                        10, 0.3, 0.3, 0.3, 0.05
                );
            }
            this.playSound(ModSoundEvents.ENTITY_CRYSTAL_WRAITH_HURT, 0.8f, 1.2f);
        }
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false; // No fall damage for flying mob
    }

    /**
     * Dive-bomb goal: the wraith flies at the target and deals contact damage
     * when it gets close enough.  The dive ends on contact, timeout, or target loss.
     */
    private static class CrystalWraithDiveGoal extends Goal {
        private final CrystalWraithEntity wraith;

        public CrystalWraithDiveGoal(CrystalWraithEntity wraith) {
            this.wraith = wraith;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (wraith.diveCooldown > 0) return false;
            LivingEntity target = wraith.getTarget();
            if (target == null || !target.isAlive()) return false;
            double dist = wraith.squaredDistanceTo(target);
            return dist <= 64.0 && dist >= 4.0;
        }

        @Override
        public void start() {
            wraith.startDive();
        }

        @Override
        public boolean shouldContinue() {
            return wraith.isDiving && wraith.diveTimer > 0;
        }

        @Override
        public void tick() {
            LivingEntity target = wraith.getTarget();
            if (target == null || !target.isAlive()) {
                endDive();
                return;
            }
            double dist = wraith.squaredDistanceTo(target);
            if (dist < 2.25) { // 1.5 blocks — dive hit
                target.damage(wraith.getDamageSources().mobAttack(wraith),
                        (float) wraith.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
                endDive();
            }
        }

        @Override
        public void stop() {
            endDive();
        }

        private void endDive() {
            wraith.isDiving = false;
            wraith.diveTimer = 0;
            wraith.setNoGravity(true);
            wraith.diveCooldown = 60 + wraith.random.nextInt(60); // 3-6 seconds cooldown
        }
    }

    private static class CrystalWraithFloatGoal extends Goal {
        private final CrystalWraithEntity wraith;

        public CrystalWraithFloatGoal(CrystalWraithEntity wraith) {
            this.wraith = wraith;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return wraith.getTarget() == null || !wraith.getTarget().isAlive();
        }

        @Override
        public void tick() {
            // Float around aimlessly
            if (wraith.random.nextInt(40) == 0) {
                double x = wraith.getX() + (wraith.random.nextDouble() - 0.5) * 8.0;
                double y = wraith.getY() + (wraith.random.nextDouble() - 0.5) * 4.0 + 5.0;
                double z = wraith.getZ() + (wraith.random.nextDouble() - 0.5) * 8.0;
                wraith.getMoveControl().moveTo(x, Math.max(y, 3.0), z, 0.3);
            }
        }
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean allowDrops) {
        super.dropEquipment(world, source, allowDrops);
        int count = this.random.nextInt(4); // 0-3
        for (int i = 0; i < count; i++) {
            this.dropItem(ModItems.CRYSTAL_SHARD);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("DiveCooldown", this.diveCooldown);
        nbt.putBoolean("IsDiving", this.isDiving);
        nbt.putInt("DiveTimer", this.diveTimer);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.diveCooldown = nbt.getInt("DiveCooldown");
        this.isDiving = nbt.getBoolean("IsDiving");
        this.diveTimer = nbt.getInt("DiveTimer");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundEvents.ENTITY_CRYSTAL_WRAITH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSoundEvents.ENTITY_CRYSTAL_WRAITH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundEvents.ENTITY_CRYSTAL_WRAITH_DEATH;
    }
}
