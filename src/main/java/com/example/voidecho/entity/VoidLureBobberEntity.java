package com.example.voidecho.entity;

import com.example.voidecho.ModParticleTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class VoidLureBobberEntity extends Entity {
    private static final TrackedData<Integer> OWNER_ID = DataTracker.registerData(
            VoidLureBobberEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private static final int MIN_WAIT = 100; // 5 seconds at 20 tps
    private int ticksAlive = 0;

    public VoidLureBobberEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public VoidLureBobberEntity(World world, PlayerEntity owner) {
        super(ModEntities.VOID_LURE_BOBBER, world);
        this.dataTracker.set(OWNER_ID, owner.getId());
        Vec3d lookPos = owner.getCameraPosVec(1.0f).add(
                owner.getRotationVec(1.0f).multiply(4.0));
        this.setPosition(lookPos.x, owner.getEyeY() + 0.5, lookPos.z);
        this.setVelocity(owner.getRotationVec(1.0f).multiply(0.6));
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(OWNER_ID, -1);
    }

    @Override
    public void tick() {
        super.tick();
        PlayerEntity owner = getOwner();
        if (owner == null) { this.discard(); return; }

        // Stop horizontal motion after initial cast
        if (ticksAlive > 10) {
            this.setVelocity(Vec3d.ZERO);
        }

        // Hover oscillation
        if (ticksAlive > 10 && !this.getWorld().isClient) {
            this.setPosition(this.getX(),
                    this.getY() + Math.sin(ticksAlive * 0.05) * 0.03,
                    this.getZ());
        }

        // VOID_AMBIENT particles
        if (this.getWorld().isClient && ticksAlive > 10 && this.random.nextFloat() < 0.3f) {
            this.getWorld().addParticle(ModParticleTypes.VOID_AMBIENT,
                    this.getX() + this.random.nextGaussian() * 0.2,
                    this.getY() + 0.2,
                    this.getZ() + this.random.nextGaussian() * 0.2,
                    0, 0.03, 0);
        }

        // Auto-discard if owner too far or changed dimensions
        if (!this.getWorld().isClient) {
            if (owner.getWorld() != this.getWorld() ||
                    this.squaredDistanceTo(owner) > 100.0) {
                this.discard();
            }
        }

        ticksAlive++;
    }

    public void tryReelIn() {
        if (this.getWorld().isClient || ticksAlive < MIN_WAIT) {
            this.discard();
            return;
        }
        PlayerEntity owner = getOwner();
        if (owner == null) { this.discard(); return; }

        ServerWorld sw = (ServerWorld) this.getWorld();
        Identifier biomeId = sw.getBiome(this.getBlockPos()).getKey()
                .map(key -> key.getValue()).orElse(null);
        String lootPath = getLootTableForBiome(biomeId);

        RegistryKey<LootTable> lootKey = RegistryKey.of(
                RegistryKeys.LOOT_TABLE, Identifier.of("void_echo", lootPath));

        LootContextParameterSet params =
                new LootContextParameterSet.Builder(sw)
                        .add(LootContextParameters.ORIGIN,
                                this.getPos())
                        .add(LootContextParameters.THIS_ENTITY,
                                owner)
                        .luck(owner.getLuck())
                        .build(LootContextTypes.FISHING);

        LootTable table = sw.getServer()
                .getReloadableRegistries().getLootTable(lootKey);
        List<ItemStack> loot = table.generateLoot(params);

        for (ItemStack drop : loot) {
            if (!owner.getInventory().insertStack(drop)) {
                owner.dropItem(drop, false);
            }
        }

        this.discard();
    }

    private String getLootTableForBiome(Identifier biomeId) {
        if (biomeId == null) return "gameplay/fishing/void_plains";
        String p = biomeId.getPath();
        if (p.contains("crystal_forest")) return "gameplay/fishing/crystal_forest";
        if (p.contains("void_wastes")) return "gameplay/fishing/void_wastes";
        if (p.contains("crystal_caverns")) return "gameplay/fishing/crystal_caverns";
        return "gameplay/fishing/void_plains";
    }

    private PlayerEntity getOwner() {
        int id = this.dataTracker.get(OWNER_ID);
        if (id < 0) return null;
        return (PlayerEntity) this.getWorld().getEntityById(id);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.ticksAlive = nbt.getInt("ticks_alive");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("ticks_alive", ticksAlive);
    }
}
