package com.example.voidecho.entity.mob;

import com.example.voidecho.ModSoundEvents;
import com.example.voidecho.entity.ModEntities;
import com.example.voidecho.entity.mob.CrystalGuardianEntity;
import com.example.voidecho.item.ModItems;
import java.util.EnumSet;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class CrystalSpriteEntity extends FlyingEntity {
    private int followTicks = 0;
    private PlayerEntity followingPlayer = null;

    public CrystalSpriteEntity(EntityType<? extends CrystalSpriteEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 3;
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 8.0)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new CrystalSpriteFollowGoal(this));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(4, new LookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        // Glowing particle effect
        if (this.getWorld().isClient && this.random.nextFloat() < 0.5f) {
            this.getWorld().addParticle(
                    ParticleTypes.END_ROD,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                    this.getY() + this.random.nextDouble() * 0.8,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.8,
                    0, 0, 0
            );
        }

        // Following behavior when tamed-like with crystal berry
        if (!this.getWorld().isClient && this.followingPlayer != null) {
            this.followTicks--;
            if (this.followTicks <= 0 || !this.followingPlayer.isAlive()
                    || this.squaredDistanceTo(this.followingPlayer) > 256.0) {
                this.followingPlayer = null;
            } else {
                // Float toward the player
                double dx = this.followingPlayer.getX() - this.getX();
                double dy = this.followingPlayer.getY() + 1.5 - this.getY();
                double dz = this.followingPlayer.getZ() - this.getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist > 2.0) {
                    this.setVelocity(this.getVelocity().add(
                            dx / dist * 0.02, dy / dist * 0.02, dz / dist * 0.02
                    ));
                }
            }
        }
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isOf(ModItems.VOID_CATALYST)) {
            if (!this.getWorld().isClient) {
                // Consume catalyst
                if (!player.getAbilities().creativeMode) stack.decrement(1);
                // Spawn guardian
                CrystalGuardianEntity guardian = ModEntities.CRYSTAL_GUARDIAN.create(this.getWorld());
                if (guardian != null) {
                    guardian.setPosition(this.getPos());
                    guardian.setOwner(player);
                    this.getWorld().spawnEntity(guardian);
                    // Sound
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.NEUTRAL, 1.0f, 1.0f);
                    // Particles
                    if (this.getWorld() instanceof ServerWorld sw) {
                        sw.spawnParticles(ParticleTypes.ENCHANT, this.getX(), this.getY() + 0.5, this.getZ(),
                            30, 0.5, 1.0, 0.5, 0.1);
                    }
                    this.discard();
                }
            }
            return ActionResult.SUCCESS;
        }
        if (stack.isOf(ModItems.CRYSTAL_BERRY)) {
            if (!this.getWorld().isClient) {
                this.followingPlayer = player;
                this.followTicks = 600; // 30 seconds
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
                // Play happy sound
                this.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.5f);
            }
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean allowDrops) {
        super.dropEquipment(world, source, allowDrops);
        int count = this.random.nextBetween(1, 2);
        for (int i = 0; i < count; i++) {
            this.dropItem(ModItems.CRYSTAL_SHARD);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLOCK_GLASS_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLOCK_GLASS_BREAK;
    }

    private static class CrystalSpriteFollowGoal extends Goal {
        private final CrystalSpriteEntity sprite;
        public CrystalSpriteFollowGoal(CrystalSpriteEntity sprite) {
            this.sprite = sprite;
            this.setControls(EnumSet.of(Control.MOVE));
        }
        @Override
        public boolean canStart() {
            return sprite.followingPlayer != null && sprite.followingPlayer.isAlive();
        }
        @Override
        public void tick() {
            if (sprite.followingPlayer != null) {
                double dist = sprite.squaredDistanceTo(sprite.followingPlayer);
                if (dist > 16.0) {
                    sprite.getMoveControl().moveTo(sprite.followingPlayer.getX(),
                        sprite.followingPlayer.getY() + 1.0, sprite.followingPlayer.getZ(), 0.5);
                }
            }
        }
    }
}
