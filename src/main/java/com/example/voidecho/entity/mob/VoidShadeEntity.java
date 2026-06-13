package com.example.voidecho.entity.mob;

import com.example.voidecho.ModParticleTypes;
import com.example.voidecho.ModSoundEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class VoidShadeEntity extends HostileEntity {
    private int teleportCooldown = 0;
    private int beamCooldown = 0;

    public VoidShadeEntity(EntityType<? extends VoidShadeEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 30;
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 150.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new VoidShadeBeamGoal(this));
        this.goalSelector.add(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(4, new WanderAroundGoal(this, 0.6));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new RevengeGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) {
            if (this.random.nextFloat() < 0.4f) {
                this.getWorld().addParticle(ModParticleTypes.VOID_AMBIENT,
                        this.getX() + (this.random.nextDouble() - 0.5) * 1.0,
                        this.getY() + this.random.nextDouble() * 2.0,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 1.0,
                        0, 0.02, 0);
            }
            return;
        }
        if (teleportCooldown > 0) teleportCooldown--;
        if (beamCooldown > 0) beamCooldown--;
    }

    private void teleportNearTarget(LivingEntity target) {
        if (teleportCooldown > 0 || this.getWorld().isClient) return;
        teleportCooldown = 80;
        ServerWorld sw = (ServerWorld) this.getWorld();

        sw.spawnParticles(ParticleTypes.PORTAL,
                this.getX(), this.getY() + 1.0, this.getZ(),
                20, 0.5, 1.0, 0.5, 0.1);

        double angle = this.random.nextDouble() * Math.PI * 2;
        double dist = 2.0 + this.random.nextDouble() * 3.0;
        double x = target.getX() + Math.cos(angle) * dist;
        double z = target.getZ() + Math.sin(angle) * dist;
        this.teleport(x, target.getY(), z, false);

        sw.spawnParticles(ParticleTypes.PORTAL,
                this.getX(), this.getY() + 1.0, this.getZ(),
                20, 0.5, 1.0, 0.5, 0.1);
    }

    private void fireBeam(LivingEntity target) {
        if (beamCooldown > 0 || this.getWorld().isClient) return;
        beamCooldown = 60;
        ServerWorld sw = (ServerWorld) this.getWorld();
        this.playSound(ModSoundEvents.ENTITY_VOID_STALKER_SCREAM, 0.5f, 1.5f);

        // Small beam
        Vec3d start = this.getPos().add(0, 1.5, 0);
        Vec3d end = target.getPos().add(0, target.getHeight() / 2, 0);
        Vec3d dir = end.subtract(start);
        double len = dir.length();
        dir = dir.normalize();
        for (double d = 0; d < len; d += 0.5) {
            Vec3d pt = start.add(dir.multiply(d));
            sw.spawnParticles(ModParticleTypes.VOID_BEAM, pt.x, pt.y, pt.z,
                    1, 0.05, 0.05, 0.05, 0);
        }
        target.damage(target.getDamageSources().mobAttack(this), 8.0f);
    }

    private static class VoidShadeBeamGoal extends Goal {
        private final VoidShadeEntity shade;

        VoidShadeBeamGoal(VoidShadeEntity shade) {
            this.shade = shade;
            this.setControls(java.util.EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = shade.getTarget();
            return target != null && target.isAlive()
                    && shade.beamCooldown <= 0
                    && shade.distanceTo(target) >= 4.0
                    && shade.distanceTo(target) <= 12.0;
        }

        @Override
        public void start() {
            LivingEntity target = shade.getTarget();
            if (target != null) {
                shade.teleportNearTarget(target);
                shade.fireBeam(target);
            }
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundEvents.ENTITY_VOID_WORM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSoundEvents.ENTITY_VOID_STALKER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundEvents.ENTITY_VOID_STALKER_DEATH;
    }
}
