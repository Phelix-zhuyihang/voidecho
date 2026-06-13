package com.example.voidecho.entity.boss;

import com.example.voidecho.ModParticleTypes;
import com.example.voidecho.ModSoundEvents;
import com.example.voidecho.config.ModConfig;
import com.example.voidecho.entity.ModEntities;
import com.example.voidecho.entity.mob.VoidWormEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import com.example.voidecho.item.ModItems;

import java.util.EnumSet;
import java.util.List;

public class VoidStalkerEntity extends HostileEntity {
    private enum BossPhase {
        STALKER,
        SUMMONER,
        ENRAGE
    }

    private final ServerBossBar bossBar;
    private BossPhase currentPhase = BossPhase.STALKER;
    private int teleportCooldown = 0;
    private int summonCooldown = 0;
    private int beamCooldown = 0;
    private int shieldRegenCooldown = 0;
    private int enrageComboCount = 0;
    private int enrageComboTimer = 0;
    private boolean hasShield = false;
    private boolean summonerPhaseTriggered = false;
    private boolean enragePhaseTriggered = false;
    private float aoeAccumulatedDamage = 0;

    public VoidStalkerEntity(EntityType<? extends VoidStalkerEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 500;
        this.bossBar = new ServerBossBar(
                Text.translatable("entity.void_echo.void_stalker"),
                BossBar.Color.PURPLE,
                BossBar.Style.NOTCHED_20
        );
        this.bossBar.setDarkenSky(true);
        this.bossBar.setThickenFog(true);
        this.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT).setBaseValue(1.5);
    }

    @SuppressWarnings("unused")
    public static boolean canSpawn(EntityType<VoidStalkerEntity> type, ServerWorldAccess world,
                                    SpawnReason reason, BlockPos pos, Random random) {
        return HostileEntity.canSpawnInDark(type, world, reason, pos, random);
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 300.0 * ModConfig.getInstance().bossHealthMultiplier)
                .add(EntityAttributes.GENERIC_ARMOR, 6.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.8)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 1.6)
                .add(EntityAttributes.GENERIC_STEP_HEIGHT, 1.5);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new VoidStalkerTeleportGoal(this));
        this.goalSelector.add(3, new VoidStalkerBeamGoal(this));
        this.goalSelector.add(4, new VoidStalkerMeleeGoal(this));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 16.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new RevengeGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) return;

        // Phase transition tracking
        float hpPercent = this.getHealth() / this.getMaxHealth();

        if (!summonerPhaseTriggered && hpPercent <= 0.6f) {
            transitionToSummoner();
        }
        if (!enragePhaseTriggered && hpPercent <= 0.3f) {
            transitionToEnrage();
        }

        // Phase-specific logic
        switch (currentPhase) {
            case STALKER -> tickStalkerPhase();
            case SUMMONER -> tickSummonerPhase();
            case ENRAGE -> tickEnragePhase();
        }

        // Update boss bar
        this.bossBar.setPercent(hpPercent);
    }

    private void tickStalkerPhase() {
        if (teleportCooldown > 0) teleportCooldown--;
    }

    private void tickSummonerPhase() {
        if (summonCooldown > 0) summonCooldown--;
        if (beamCooldown > 0) beamCooldown--;

        // Shield regeneration
        if (hasShield) {
            if (shieldRegenCooldown > 0) {
                shieldRegenCooldown--;
            } else {
                // Refresh absorption
                this.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.ABSORPTION, 600, 9, false, false
                ));
                shieldRegenCooldown = 600; // 30 seconds
            }
        }
    }

    private void tickEnragePhase() {
        // AOE pulse every 5% HP loss - tracked in damage method
        if (enrageComboTimer > 0) {
            enrageComboTimer--;
            if (enrageComboTimer % 5 == 0 && enrageComboCount < 3) {
                // Perform combo attack hit
                LivingEntity target = this.getTarget();
                if (target != null && this.distanceTo(target) < 4.0) {
                    target.damage(target.getDamageSources().mobAttack(this), 14.0f);
                    enrageComboCount++;
                }
            }
            if (enrageComboTimer <= 0) {
                enrageComboCount = 0;
                // Re-trigger combo if target is still in melee range
                LivingEntity target = this.getTarget();
                if (target != null && target.isAlive()
                        && this.distanceTo(target) < 4.0
                        && this.random.nextFloat() < 0.3f) {
                    performEnrageCombo();
                }
            }
        }
    }

    private void transitionToSummoner() {
        if (summonerPhaseTriggered) return;
        summonerPhaseTriggered = true;
        currentPhase = BossPhase.SUMMONER;
        summonCooldown = 0;
        beamCooldown = 0;
        hasShield = true;

        // Apply absorption shield
        this.addStatusEffect(new StatusEffectInstance(
                StatusEffects.ABSORPTION, 600, 9, false, false
        ));
        shieldRegenCooldown = 600;

        // Announce phase transition
        this.bossBar.setName(Text.translatable("entity.void_echo.void_stalker.summoner"));
        sendMessageToAllPlayers("message.void_echo.stalker_summon");

        // Spawn initial minions
        spawnMinions();

        this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(5.0);
        this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.35);
        this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED).setBaseValue(1.2);

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            // Layer 1: spiral-in ambient particles (anticipation, 0.3s)
            for (int i = 0; i < 40; i++) {
                double angle = (i / 40.0) * Math.PI * 4;
                double radius = 3.0 - (i / 40.0) * 2.5;
                serverWorld.spawnParticles(ModParticleTypes.VOID_AMBIENT,
                    this.getX() + Math.cos(angle) * radius,
                    this.getY() + 1.5 + (i % 3) * 0.5,
                    this.getZ() + Math.sin(angle) * radius,
                    1, 0, 0, 0, 0);
            }
            // Layer 2: burst explosion
            serverWorld.spawnParticles(ModParticleTypes.VOID_BURST,
                    this.getX(), this.getY() + 1.5, this.getZ(),
                    50, 2.0, 2.0, 2.0, 0.2);
            // Layer 3: expanding floor ring
            serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL,
                    this.getX(), this.getY() + 0.2, this.getZ(),
                    20, 5.0, 0.1, 5.0, 0.3);
        }
        this.playSound(ModSoundEvents.ENTITY_VOID_STALKER_PHASE_SHIFT, 1.0f, 0.8f);
    }

    private void transitionToEnrage() {
        if (enragePhaseTriggered) return;
        enragePhaseTriggered = true;
        currentPhase = BossPhase.ENRAGE;
        enrageComboCount = 0;
        enrageComboTimer = 0;

        // Increase stats for enrage
        this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.42);
        this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(14.0);
        this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED).setBaseValue(3.2);

        // Announce phase transition
        this.bossBar.setName(Text.translatable("entity.void_echo.void_stalker.enrage"));
        sendMessageToAllPlayers("message.void_echo.stalker_enrage");

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            // Layer 1: screen flash
            serverWorld.spawnParticles(ParticleTypes.FLASH,
                    this.getX(), this.getY() + 1.5, this.getZ(),
                    30, 2.0, 2.0, 2.0, 0.5);
            // Layer 2: burst explosion
            serverWorld.spawnParticles(ModParticleTypes.VOID_BURST,
                    this.getX(), this.getY() + 1.5, this.getZ(),
                    60, 1.5, 2.0, 1.5, 0.1);
            // Layer 3: vertical beam jets (rising energy pillars)
            for (int j = 0; j < 8; j++) {
                double angle = j * Math.PI / 4;
                double ox = this.getX() + Math.cos(angle) * 2.0;
                double oz = this.getZ() + Math.sin(angle) * 2.0;
                serverWorld.spawnParticles(ModParticleTypes.VOID_BEAM,
                        ox, this.getY() + 0.5, oz,
                        5, 0.3, 1.5, 0.3, 0.15);
            }
            // Layer 4: floor ring spread
            serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL,
                    this.getX(), this.getY() + 0.1, this.getZ(),
                    20, 4.0, 0.1, 4.0, 0.4);
        }
        this.playSound(ModSoundEvents.ENTITY_VOID_STALKER_PHASE_SHIFT, 1.0f, 0.5f);
    }

    private void triggerAoePulse() {
        if (this.getWorld().isClient) return;
        ServerWorld serverWorld = (ServerWorld) this.getWorld();

        // Layer 1: sonic wave ring
        serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM,
                this.getX(), this.getY() + 1.0, this.getZ(),
                40, 3.0, 2.0, 3.0, 0.3);
        // Layer 2: soul particle ring
        serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL,
                this.getX(), this.getY() + 1.0, this.getZ(),
                30, 5.0, 2.0, 5.0, 0.2);
        // Layer 3: void burst from epicenter
        serverWorld.spawnParticles(ModParticleTypes.VOID_BURST,
                this.getX(), this.getY() + 1.0, this.getZ(),
                30, 2.0, 1.5, 2.0, 0.15);
        // Layer 4: ground residual (void energy leaking from cracks)
        serverWorld.spawnParticles(ModParticleTypes.VOID_AMBIENT,
                this.getX(), this.getY() + 0.1, this.getZ(),
                25, 4.0, 0.1, 4.0, 0.02);

        this.playSound(ModSoundEvents.ENTITY_VOID_STALKER_SCREAM, 1.5f, 0.6f);

        // Damage all nearby players
        for (LivingEntity target : serverWorld.getEntitiesByClass(
                LivingEntity.class,
                this.getBoundingBox().expand(5.0),
                e -> e instanceof PlayerEntity && e.isAlive() && e.distanceTo(this) <= 5.0
        )) {
            target.damage(target.getDamageSources().mobAttack(this), 8.0f);
            target.takeKnockback(1.0, target.getX() - this.getX(), target.getZ() - this.getZ());
        }
    }

    private void teleportBehindPlayer(LivingEntity target) {
        if (this.getWorld().isClient) return;

        Vec3d targetPos = target.getPos();
        float yaw = target.getYaw();
        double radians = Math.toRadians(yaw);
        Vec3d behind = targetPos.add(
                Math.sin(radians) * 2.5,
                0,
                -Math.cos(radians) * 2.5
        );

        // Find a valid surface position
        ServerWorld serverWorld = (ServerWorld) this.getWorld();
        BlockPos surfacePos = serverWorld.getTopPosition(
                net.minecraft.world.Heightmap.Type.WORLD_SURFACE,
                BlockPos.ofFloored(behind.x, behind.y, behind.z)
        );
        double teleportY = Math.max(behind.y, surfacePos.getY());

        // Spawn particles at old position
        serverWorld.spawnParticles(
                ParticleTypes.PORTAL,
                this.getX(), this.getY() + 1.0, this.getZ(),
                20, 0.5, 1.0, 0.5, 0.1
        );

        this.teleport(behind.x, teleportY, behind.z, false);

        // Spawn particles at new position
        serverWorld.spawnParticles(
                ParticleTypes.PORTAL,
                this.getX(), this.getY() + 1.0, this.getZ(),
                20, 0.5, 1.0, 0.5, 0.1
        );

        this.playSound(ModSoundEvents.ENTITY_VOID_STALKER_SCREAM, 1.0f, 1.0f);
        teleportCooldown = 60 + this.random.nextInt(40); // 3-5 seconds
    }

    private void fireVoidBeam(LivingEntity target) {
        if (this.getWorld().isClient) return;
        ServerWorld serverWorld = (ServerWorld) this.getWorld();

        // Charge visual
        serverWorld.spawnParticles(
                ModParticleTypes.VOID_BEAM,
                this.getX(), this.getY() + 2.0, this.getZ(),
                15, 0.5, 0.5, 0.5, 0.05
        );

        // Line of particles from boss to target
        Vec3d start = this.getPos().add(0, 2.0, 0);
        Vec3d end = target.getPos().add(0, target.getHeight() / 2, 0);
        Vec3d dir = end.subtract(start);
        double length = dir.length();
        dir = dir.normalize();

        for (double d = 0; d < length; d += 0.5) {
            Vec3d point = start.add(dir.multiply(d));
            serverWorld.spawnParticles(
                    ModParticleTypes.VOID_BEAM,
                    point.x, point.y, point.z,
                    1, 0.1, 0.1, 0.1, 0.0
            );
        }

        // Damage target
        target.damage(target.getDamageSources().mobAttack(this), 10.0f);

        // Impact burst at hit point
        serverWorld.spawnParticles(ModParticleTypes.VOID_BURST,
                target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                20, 0.5, 0.8, 0.5, 0.1);
        // Residual void energy at impact
        serverWorld.spawnParticles(ModParticleTypes.VOID_AMBIENT,
                target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                15, 0.8, 1.0, 0.8, 0.03);

        // Apply knockback
        target.takeKnockback(1.5, target.getX() - this.getX(), target.getZ() - this.getZ());

        this.playSound(ModSoundEvents.ENTITY_VOID_STALKER_SCREAM, 1.0f, 0.7f);
        beamCooldown = 100; // 5 seconds
    }

    private void spawnMinions() {
        if (this.getWorld().isClient) return;
        ServerWorld serverWorld = (ServerWorld) this.getWorld();
        int count = 3 + this.random.nextInt(2); // 3-4

        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2 * i) / count;
            double x = this.getX() + Math.cos(angle) * 3.0;
            double z = this.getZ() + Math.sin(angle) * 3.0;

            BlockPos spawnPos = serverWorld.getTopPosition(
                    net.minecraft.world.Heightmap.Type.WORLD_SURFACE,
                    BlockPos.ofFloored(x, this.getY(), z)
            );

            VoidWormEntity worm = ModEntities.VOID_WORM.create(serverWorld);
            if (worm != null) {
                worm.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
                worm.setTarget(this.getTarget());
                serverWorld.spawnEntity(worm);
            }
        }

        // Summon visual burst
        serverWorld.spawnParticles(ModParticleTypes.VOID_BURST,
                this.getX(), this.getY() + 1.0, this.getZ(),
                30, 2.0, 1.0, 2.0, 0.1);
        this.playSound(ModSoundEvents.ENTITY_VOID_STALKER_SCREAM, 0.8f, 0.5f);

        summonCooldown = 300; // 15 seconds
    }

    private void performEnrageCombo() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;
        if (this.distanceTo(target) > 4.0) return;

        enrageComboCount = 0;
        enrageComboTimer = 15; // 0.75 seconds for 3 hits at 0.25s intervals
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (currentPhase == BossPhase.ENRAGE) {
            boolean result = super.damage(source, amount);
            if (result) {
                aoeAccumulatedDamage += amount;
                float threshold = this.getMaxHealth() * 0.05f;
                while (aoeAccumulatedDamage >= threshold) {
                    aoeAccumulatedDamage -= threshold;
                    triggerAoePulse();
                }
            }
            return result;
        }
        return super.damage(source, amount);
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        // Boss bar is updated in tick()
    }

    private void sendMessageToAllPlayers(String translationKey) {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            Text message = Text.translatable(translationKey);
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                player.sendMessage(message, true);
            }
        }
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean allowDrops) {
        super.dropEquipment(world, source, allowDrops);
        // Void heart (guaranteed)
        this.dropItem(ModItems.VOID_HEART);
        // Crystal shards (3-8)
        int shardCount = 3 + this.random.nextInt(6);
        for (int i = 0; i < shardCount; i++) {
            this.dropItem(ModItems.CRYSTAL_SHARD);
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            // Grant advancement to all nearby players
            Text message = Text.translatable("message.void_echo.stalker_defeated");
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                if (player.distanceTo(this) <= 64.0) {
                    player.sendMessage(message, false);
                }
            }

            // Death particles
            serverWorld.spawnParticles(
                    ParticleTypes.EXPLOSION_EMITTER,
                    this.getX(), this.getY() + 1.5, this.getZ(),
                    1, 0, 0, 0, 0
            );
            serverWorld.spawnParticles(
                    ParticleTypes.SCULK_SOUL,
                    this.getX(), this.getY() + 1.0, this.getZ(),
                    100, 3.0, 2.0, 3.0, 0.3
            );
        }

        this.bossBar.clearPlayers();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundEvents.ENTITY_VOID_STALKER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSoundEvents.ENTITY_VOID_STALKER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundEvents.ENTITY_VOID_STALKER_DEATH;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("currentPhase", currentPhase.name());
        nbt.putBoolean("summonerPhaseTriggered", summonerPhaseTriggered);
        nbt.putBoolean("enragePhaseTriggered", enragePhaseTriggered);
        nbt.putBoolean("hasShield", hasShield);
        nbt.putInt("teleportCooldown", teleportCooldown);
        nbt.putInt("summonCooldown", summonCooldown);
        nbt.putInt("beamCooldown", beamCooldown);
        nbt.putInt("enrageComboCount", enrageComboCount);
        nbt.putInt("enrageComboTimer", enrageComboTimer);
        nbt.putInt("shieldRegenCooldown", shieldRegenCooldown);
        nbt.putFloat("aoeAccumulatedDamage", aoeAccumulatedDamage);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("currentPhase")) {
            try {
                currentPhase = BossPhase.valueOf(nbt.getString("currentPhase"));
            } catch (IllegalArgumentException e) {
                currentPhase = BossPhase.STALKER;
            }
        }
        summonerPhaseTriggered = nbt.getBoolean("summonerPhaseTriggered");
        enragePhaseTriggered = nbt.getBoolean("enragePhaseTriggered");
        hasShield = nbt.getBoolean("hasShield");
        teleportCooldown = nbt.getInt("teleportCooldown");
        summonCooldown = nbt.getInt("summonCooldown");
        beamCooldown = nbt.getInt("beamCooldown");
        enrageComboCount = nbt.getInt("enrageComboCount");
        enrageComboTimer = nbt.getInt("enrageComboTimer");
        shieldRegenCooldown = nbt.getInt("shieldRegenCooldown");
        aoeAccumulatedDamage = nbt.getFloat("aoeAccumulatedDamage");
        applyPhaseAttributes();
    }

    private void applyPhaseAttributes() {
        switch (currentPhase) {
            case SUMMONER -> {
                var atk = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (atk != null) atk.setBaseValue(5.0);
                var spd = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                if (spd != null) spd.setBaseValue(0.35);
                var atkspd = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
                if (atkspd != null) atkspd.setBaseValue(1.2);
            }
            case ENRAGE -> {
                var atk = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (atk != null) atk.setBaseValue(14.0);
                var spd = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                if (spd != null) spd.setBaseValue(0.42);
                var atkspd = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
                if (atkspd != null) atkspd.setBaseValue(3.2);
            }
        }
    }

    // ---- Custom AI Goals ----

    private static class VoidStalkerTeleportGoal extends Goal {
        private final VoidStalkerEntity stalker;

        public VoidStalkerTeleportGoal(VoidStalkerEntity stalker) {
            this.stalker = stalker;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (stalker.currentPhase != BossPhase.STALKER) return false;
            if (stalker.teleportCooldown > 0) return false;
            LivingEntity target = stalker.getTarget();
            if (target == null || !target.isAlive()) return false;
            return stalker.squaredDistanceTo(target) <= 1024.0; // 32 blocks
        }

        @Override
        public void start() {
            LivingEntity target = stalker.getTarget();
            if (target != null) {
                stalker.teleportBehindPlayer(target);
            }
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }
    }

    private static class VoidStalkerBeamGoal extends Goal {
        private final VoidStalkerEntity stalker;
        private int chargeTime = 0;
        private boolean isCharging = false;

        public VoidStalkerBeamGoal(VoidStalkerEntity stalker) {
            this.stalker = stalker;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (stalker.currentPhase != BossPhase.SUMMONER) return false;
            if (stalker.beamCooldown > 0) return false;
            LivingEntity target = stalker.getTarget();
            if (target == null || !target.isAlive()) return false;
            return stalker.distanceTo(target) <= 20.0;
        }

        @Override
        public void start() {
            isCharging = true;
            chargeTime = 20; // 1 second charge
            stalker.navigation.stop();
            if (stalker.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                        ModParticleTypes.VOID_BEAM,
                        stalker.getX(), stalker.getY() + 2.0, stalker.getZ(),
                        10, 0.3, 0.3, 0.3, 0.02
                );
            }
        }

        @Override
        public void tick() {
            if (isCharging) {
                chargeTime--;
                if (chargeTime <= 0) {
                    LivingEntity target = stalker.getTarget();
                    if (target != null) {
                        stalker.fireVoidBeam(target);
                    }
                    isCharging = false;
                }
            }
        }

        @Override
        public boolean shouldContinue() {
            return isCharging;
        }
    }

    private static class VoidStalkerMeleeGoal extends MeleeAttackGoal {
        private final VoidStalkerEntity stalker;

        public VoidStalkerMeleeGoal(VoidStalkerEntity stalker) {
            super(stalker, 1.0, false);
            this.stalker = stalker;
        }

        @Override
        public boolean canStart() {
            return super.canStart();
        }

        @Override
        protected boolean canAttack(LivingEntity target) {
            return this.mob.squaredDistanceTo(target) <= 16.0;
        }
    }
}
