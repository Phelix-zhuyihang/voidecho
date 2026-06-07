package com.example.voidecho.entity;

import com.example.voidecho.effect.ModEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import com.example.voidecho.item.ModItems;

/**
 * A projectile fired by the Void Staff.
 * Rendered as a flying item with a purple crystal texture.
 * Deals 8 damage and applies Void Touched effect on hit.
 */
public class VoidBoltEntity extends ProjectileEntity implements FlyingItemEntity {

    public VoidBoltEntity(EntityType<? extends VoidBoltEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * Constructs a VoidBolt fired by the given owner with the specified velocity.
     * Used by VoidStaffItem to create and launch the bolt.
     */
    public VoidBoltEntity(EntityType<? extends VoidBoltEntity> type, LivingEntity owner,
                          double vx, double vy, double vz, World world) {
        super(type, world);
        this.setOwner(owner);
        this.setPosition(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        this.setVelocity(vx, vy, vz);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        // No tracked data required for this entity
    }

    @Override
    public void tick() {
        super.tick();

        // Spawn trail particles on the client
        if (this.getWorld().isClient) {
            this.getWorld().addParticle(ParticleTypes.DRAGON_BREATH,
                    this.getX(), this.getY(), this.getZ(),
                    0.0, 0.0, 0.0);
        }

        // Despawn after 5 seconds (100 ticks at 20 ticks/sec)
        if (this.age > 100) {
            this.discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity target = entityHitResult.getEntity();
        if (target == null) return;

        // Deal 8 damage as thrown projectile damage
        target.damage(this.getDamageSources().thrown(this, this.getOwner()), 8.0f);

        // Apply Void Touched effect for 5 seconds (100 ticks)
        if (target instanceof LivingEntity livingEntity) {
            livingEntity.addStatusEffect(
                    new StatusEffectInstance(ModEffects.VOID_TOUCHED, 100, 0),
                    this.getOwner());
        }

        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        this.discard();
    }

    @Override
    public ItemStack getStack() {
        // Return a crystal shard for the flying item renderer
        return new ItemStack(ModItems.CRYSTAL_SHARD);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        // No custom persistent data
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        // No custom persistent data
    }
}
