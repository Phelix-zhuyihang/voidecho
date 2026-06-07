package com.example.voidecho.entity.mob;

import com.example.voidecho.entity.ModEntities;
import com.example.voidecho.entity.VoidBoltEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;

public class CrystalGuardianEntity extends FlyingEntity {
    private static final int ABSORPTION_COOLDOWN_TICKS = 600; // 30 seconds
    private int absorptionCooldown = 0;
    private int shootCooldown = 0;
    private PlayerEntity owner;

    public CrystalGuardianEntity(EntityType<? extends CrystalGuardianEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 0;
        this.setPersistent();
    }

    public void setOwner(PlayerEntity owner) {
        this.owner = owner;
    }

    public PlayerEntity getGuardianOwner() {
        return this.owner;
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return FlyingEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.4)
                .add(EntityAttributes.GENERIC_ARMOR, 4.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new CrystalGuardianFollowOwnerGoal(this));
        this.goalSelector.add(3, new CrystalGuardianShootGoal(this));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
        this.goalSelector.add(5, new LookAroundGoal(this));

        this.targetSelector.add(1, new CrystalGuardianProtectOwnerGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient) {
            // Glowing particles
            if (this.random.nextFloat() < 0.3f) {
                this.getWorld().addParticle(
                        ParticleTypes.END_ROD,
                        this.getX() + (this.random.nextDouble() - 0.5) * 1.0,
                        this.getY() + this.random.nextDouble() * 1.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 1.0,
                        0, 0, 0
                );
            }
            return;
        }

        // Cooldown ticks
        if (absorptionCooldown > 0) absorptionCooldown--;
        if (shootCooldown > 0) shootCooldown--;

        // Despawn if owner is dead or missing
        if (this.owner == null || !this.owner.isAlive()) {
            this.discard();
            return;
        }

        // Teleport to owner if too far
        if (this.squaredDistanceTo(this.owner) > 256.0) {
            this.teleport(this.owner.getX(), this.owner.getY() + 1.0, this.owner.getZ(), false);
        }
    }

    public void onOwnerDamaged(LivingEntity attacker) {
        if (absorptionCooldown > 0) return;

        // Apply Absorption I to owner for 20 seconds (400 ticks)
        this.owner.addStatusEffect(new StatusEffectInstance(
                StatusEffects.ABSORPTION, 400, 0, false, false, true));

        absorptionCooldown = ABSORPTION_COOLDOWN_TICKS;

        // Particles around owner
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                    ParticleTypes.HEART,
                    this.owner.getX(), this.owner.getY() + 1.0, this.owner.getZ(),
                    10, 0.5, 0.5, 0.5, 0.1);
        }

        this.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 0.6f, 1.2f);
    }

    private void shootAtTarget(LivingEntity target) {
        if (this.getWorld().isClient) return;

        Vec3d dir = target.getPos().add(0, target.getHeight() / 2, 0)
                .subtract(this.getPos().add(0, this.getEyeHeight(this.getPose()), 0))
                .normalize();

        VoidBoltEntity bolt = new VoidBoltEntity(
                ModEntities.VOID_BOLT,
                this,
                dir.x * 1.5, dir.y * 1.5, dir.z * 1.5,
                this.getWorld()
        );

        bolt.setPosition(this.getX(), this.getY() + this.getEyeHeight(this.getPose()), this.getZ());
        this.getWorld().spawnEntity(bolt);
        shootCooldown = 40; // 2 seconds
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean canImmediatelyDespawn(double distance) {
        return false;
    }

    // ---- AI Goals ----

    private static class CrystalGuardianFollowOwnerGoal extends Goal {
        private final CrystalGuardianEntity guardian;

        public CrystalGuardianFollowOwnerGoal(CrystalGuardianEntity guardian) {
            this.guardian = guardian;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return guardian.owner != null && guardian.owner.isAlive()
                    && guardian.squaredDistanceTo(guardian.owner) > 9.0;
        }

        @Override
        public void tick() {
            if (guardian.owner != null) {
                double dist = guardian.squaredDistanceTo(guardian.owner);
                if (dist > 16.0) {
                    guardian.getMoveControl().moveTo(
                            guardian.owner.getX(),
                            guardian.owner.getY() + 2.0,
                            guardian.owner.getZ(),
                            0.5);
                }
            }
        }
    }

    private static class CrystalGuardianShootGoal extends Goal {
        private final CrystalGuardianEntity guardian;
        private LivingEntity target;

        public CrystalGuardianShootGoal(CrystalGuardianEntity guardian) {
            this.guardian = guardian;
            this.setControls(EnumSet.of(Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (guardian.shootCooldown > 0) return false;
            if (guardian.owner == null || !guardian.owner.isAlive()) return false;
            // Find the nearest hostile mob within 12 blocks
            target = null;
            double closestDist = Double.MAX_VALUE;
            for (LivingEntity e : guardian.getWorld().getEntitiesByClass(
                    LivingEntity.class,
                    guardian.owner.getBoundingBox().expand(12.0),
                    entity -> entity.isAlive() && entity != guardian.owner
            )) {
                if (e instanceof net.minecraft.entity.mob.HostileEntity) {
                    double d = guardian.squaredDistanceTo(e);
                    if (d < closestDist) {
                        closestDist = d;
                        target = e;
                    }
                }
            }
            return target != null;
        }

        @Override
        public void start() {
            if (target != null) {
                guardian.shootAtTarget(target);
            }
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }
    }

    private static class CrystalGuardianProtectOwnerGoal extends Goal {
        private final CrystalGuardianEntity guardian;
        private LivingEntity attacker;

        public CrystalGuardianProtectOwnerGoal(CrystalGuardianEntity guardian) {
            this.guardian = guardian;
            this.setControls(EnumSet.of(Control.TARGET));
        }

        @Override
        public boolean canStart() {
            if (guardian.owner == null || !guardian.owner.isAlive()) return false;
            attacker = guardian.owner.getAttacker();
            if (attacker == null || !attacker.isAlive()) return false;
            return guardian.distanceTo(attacker) <= 16.0;
        }

        @Override
        public void start() {
            if (attacker != null) {
                guardian.setTarget(attacker);
                guardian.onOwnerDamaged(attacker);
            }
        }
    }
}
