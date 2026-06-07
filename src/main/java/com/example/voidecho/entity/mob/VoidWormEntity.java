package com.example.voidecho.entity.mob;

import com.example.voidecho.ModSoundEvents;
import com.example.voidecho.entity.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import com.example.voidecho.item.ModItems;

import java.util.EnumSet;

public class VoidWormEntity extends HostileEntity {
    private int burrowCooldown = 0;
    private boolean isBurrowed = false;
    private int burrowTimer = 0;

    public VoidWormEntity(EntityType<? extends VoidWormEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 5;
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0)
                .add(EntityAttributes.GENERIC_STEP_HEIGHT, 1.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new VoidWormBurrowGoal(this));
        this.goalSelector.add(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(4, new WanderAroundGoal(this, 0.8));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
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

        if (this.isBurrowed) {
            this.burrowTimer--;
            if (this.burrowTimer <= 0) {
                emerge();
            }
        }
    }

    private void startBurrow() {
        if (this.getWorld().isClient) return;
        this.isBurrowed = true;
        this.burrowTimer = 40; // 2 seconds
        this.setInvisible(true);
        this.setInvulnerable(true);
        this.navigation.stop();

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                    ParticleTypes.MYCELIUM,
                    this.getX(), this.getY(), this.getZ(),
                    20, 0.5, 0.3, 0.5, 0.1
            );
        }
        this.playSound(ModSoundEvents.ENTITY_VOID_WORM_HURT, 0.8f, 0.6f);
    }

    private void emerge() {
        if (this.getWorld().isClient) return;
        this.isBurrowed = false;
        this.setInvisible(false);
        this.setInvulnerable(false);

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && this.squaredDistanceTo(target) <= 256.0) {
            // Teleport near the target
            double angle = this.random.nextDouble() * Math.PI * 2;
            double offsetX = Math.cos(angle) * (1.5 + this.random.nextDouble() * 1.5);
            double offsetZ = Math.sin(angle) * (1.5 + this.random.nextDouble() * 1.5);
            double x = target.getX() + offsetX;
            double z = target.getZ() + offsetZ;
            double y = target.getY();

            // Find surface Y
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                var blockPos = serverWorld.getTopPosition(
                        net.minecraft.world.Heightmap.Type.WORLD_SURFACE,
                        net.minecraft.util.math.BlockPos.ofFloored(x, y, z)
                );
                this.teleport(x, blockPos.getY(), z, false);
                serverWorld.spawnParticles(
                        ParticleTypes.MYCELIUM,
                        this.getX(), this.getY(), this.getZ(),
                        20, 0.5, 0.3, 0.5, 0.1
                );
            } else {
                this.teleport(x, y, z, false);
            }
        }

        this.playSound(ModSoundEvents.ENTITY_VOID_WORM_HURT, 0.8f, 1.0f);
        this.burrowCooldown = 160 + this.random.nextInt(80); // 8-12 seconds
    }

    private static class VoidWormBurrowGoal extends Goal {
        private final VoidWormEntity worm;

        public VoidWormBurrowGoal(VoidWormEntity worm) {
            this.worm = worm;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (worm.isBurrowed) return false;
            if (worm.burrowCooldown > 0) {
                return false;
            }
            LivingEntity target = worm.getTarget();
            if (target == null || !target.isAlive()) return false;
            return worm.squaredDistanceTo(target) <= 256.0; // 16 blocks
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }

        @Override
        public void start() {
            worm.startBurrow();
        }
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean allowDrops) {
        super.dropEquipment(world, source, allowDrops);
        int count = this.random.nextInt(3); // 0-2
        for (int i = 0; i < count; i++) {
            this.dropItem(ModItems.CRYSTAL_SHARD);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundEvents.ENTITY_VOID_WORM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSoundEvents.ENTITY_VOID_WORM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundEvents.ENTITY_VOID_WORM_DEATH;
    }
}
