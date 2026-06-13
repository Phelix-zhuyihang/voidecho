package com.example.voidecho.entity.boss;

import com.example.voidecho.ModParticleTypes;
import com.example.voidecho.ModSoundEvents;
import com.example.voidecho.config.ModConfig;
import com.example.voidecho.effect.ModEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import com.example.voidecho.item.ModItems;

import java.util.EnumSet;
import java.util.List;

public class EchoWardenEntity extends HostileEntity {
    private final ServerBossBar bossBar;
    private int blinkStrikeCooldown = 0;
    private int timeSlowCooldown = 0;
    private int slashCooldown = 0;
    private int slashChargeTime = 0;
    private boolean isSlashCharging = false;
    private int teleportCooldown = 0;
    private int abilityTickCounter = 0;
    // Multi-phase fields
    private int currentPhase = 1;
    private boolean phaseTwoTriggered = false;
    private boolean phaseThreeTriggered = false;
    private int echoWaveCooldown = 0;
    private int voidDrainCooldown = 0;
    private int voidNovaCooldown = 0;
    private int voidNovaChargeTime = 0;
    private boolean isVoidNovaCharging = false;

    private static final int BLINK_STRIKE_COOLDOWN_TICKS = 400;   // 20 seconds
    private static final int TIME_SLOW_COOLDOWN_TICKS = 600; // 30 seconds
    private static final int SLASH_COOLDOWN_TICKS = 500;    // 25 seconds
    private static final int SLASH_CHARGE_TICKS = 40;       // 2 seconds
    private static final int TELEPORT_COOLDOWN_TICKS = 60;  // 3 seconds
    private static final float SLASH_DAMAGE = 20.0f;
    private static final float SLASH_RADIUS = 20.0f;
    private static final float TIME_SLOW_RADIUS = 15.0f;
    private static final float MELEE_DAMAGE = 15.0f;
    private static final float MELEE_RANGE = 5.0f;
    // Phase 2/3 ability constants
    private static final int ECHO_WAVE_COOLDOWN_TICKS = 300;
    private static final float ECHO_WAVE_DAMAGE = 15.0f;
    private static final float ECHO_WAVE_RANGE = 8.0f;
    private static final int VOID_DRAIN_COOLDOWN_TICKS = 400;
    private static final float VOID_DRAIN_RADIUS = 16.0f;
    private static final int VOID_NOVA_COOLDOWN_TICKS = 500;
    private static final int VOID_NOVA_CHARGE_TICKS = 60;
    private static final float VOID_NOVA_DAMAGE = 25.0f;
    private static final float VOID_NOVA_RADIUS = 12.0f;

    public EchoWardenEntity(EntityType<? extends EchoWardenEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 1000;
        this.bossBar = new ServerBossBar(
                Text.translatable("entity.void_echo.echo_warden"),
                BossBar.Color.RED,
                BossBar.Style.PROGRESS
        );
        this.bossBar.setDarkenSky(true);
        this.bossBar.setThickenFog(true);
        var stepAttr = this.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT);
        if (stepAttr != null) stepAttr.setBaseValue(1.5);
    }

    @SuppressWarnings("unused")
    public static boolean canSpawn(EntityType<EchoWardenEntity> type, ServerWorldAccess world,
                                    SpawnReason reason, BlockPos pos, Random random) {
        return HostileEntity.canSpawnInDark(type, world, reason, pos, random);
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 500.0 * ModConfig.getInstance().bossHealthMultiplier)
                .add(EntityAttributes.GENERIC_ARMOR, 8.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 15.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 1.0)
                .add(EntityAttributes.GENERIC_STEP_HEIGHT, 1.5);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new EchoWardenSlashGoal(this));
        this.goalSelector.add(3, new EchoWardenBlinkStrikeGoal(this));
        this.goalSelector.add(4, new EchoWardenMeleeGoal(this));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 16.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new RevengeGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) return;

        this.abilityTickCounter++;

        // Check phase transitions
        checkPhaseTransition();

        // Update cooldowns
        if (blinkStrikeCooldown > 0) blinkStrikeCooldown--;
        if (timeSlowCooldown > 0) timeSlowCooldown--;
        if (slashCooldown > 0) slashCooldown--;
        if (teleportCooldown > 0) teleportCooldown--;
        if (echoWaveCooldown > 0) echoWaveCooldown--;
        if (voidDrainCooldown > 0) voidDrainCooldown--;
        if (voidNovaCooldown > 0) voidNovaCooldown--;

        // Handle slash charging
        if (isSlashCharging) {
            slashChargeTime--;
            if (slashChargeTime <= 0) {
                executeFullScreenSlash();
                isSlashCharging = false;
                slashCooldown = SLASH_COOLDOWN_TICKS;
            }
        }

        // Handle void nova charging (phase 3)
        if (isVoidNovaCharging) {
            voidNovaChargeTime--;
            if (voidNovaChargeTime <= 0) {
                executeVoidNova();
                isVoidNovaCharging = false;
                voidNovaCooldown = VOID_NOVA_COOLDOWN_TICKS;
            }
        }

        // Phase 2: Echo Wave (triggered from tick for proximity)
        if (currentPhase >= 2 && echoWaveCooldown <= 0 && abilityTickCounter % 30 == 0) {
            LivingEntity target = this.getTarget();
            if (target != null && this.distanceTo(target) <= ECHO_WAVE_RANGE && this.random.nextFloat() < 0.2f) {
                activateEchoWave();
                echoWaveCooldown = ECHO_WAVE_COOLDOWN_TICKS;
            }
        }

        // Phase 3: Void Drain (triggered from tick)
        if (currentPhase >= 3 && voidDrainCooldown <= 0 && abilityTickCounter % 40 == 0) {
            LivingEntity target = this.getTarget();
            if (target != null && this.distanceTo(target) <= VOID_DRAIN_RADIUS) {
                activateVoidDrain();
                voidDrainCooldown = VOID_DRAIN_COOLDOWN_TICKS;
            }
        }

        // Phase 3: Void Nova (triggered from tick)
        if (currentPhase >= 3 && voidNovaCooldown <= 0 && !isVoidNovaCharging && abilityTickCounter % 50 == 0) {
            LivingEntity target = this.getTarget();
            if (target != null && this.distanceTo(target) <= 32.0 && this.random.nextFloat() < 0.15f) {
                startVoidNovaCharge();
            }
        }

        // Time slow field check
        if (timeSlowCooldown <= 0 && abilityTickCounter % 20 == 0) {
            LivingEntity target = this.getTarget();
            if (target != null && this.distanceTo(target) <= TIME_SLOW_RADIUS) {
                activateTimeSlow();
            }
        }

        // Passive teleport (gated only by teleport cooldown, not blink strike)
        if (teleportCooldown <= 0 && abilityTickCounter % 40 == 0) {
            LivingEntity target = this.getTarget();
            if (target != null && this.distanceTo(target) <= 32.0 && this.random.nextFloat() < 0.3f) {
                teleportNearTarget(target);
                teleportCooldown = TELEPORT_COOLDOWN_TICKS;
            }
        }

        // Update boss bar
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
    }

    private void teleportNearTarget(LivingEntity target) {
        if (this.getWorld().isClient) return;
        ServerWorld serverWorld = (ServerWorld) this.getWorld();

        double angle = this.random.nextDouble() * Math.PI * 2;
        double offsetX = Math.cos(angle) * (3.0 + this.random.nextDouble() * 3.0);
        double offsetZ = Math.sin(angle) * (3.0 + this.random.nextDouble() * 3.0);
        double x = target.getX() + offsetX;
        double z = target.getZ() + offsetZ;

        BlockPos surfacePos = serverWorld.getTopPosition(
                net.minecraft.world.Heightmap.Type.WORLD_SURFACE,
                BlockPos.ofFloored(x, target.getY(), z)
        );

        // Particles at old position
        serverWorld.spawnParticles(
                ParticleTypes.REVERSE_PORTAL,
                this.getX(), this.getY() + 2.0, this.getZ(),
                20, 0.5, 1.0, 0.5, 0.1
        );

        this.teleport(surfacePos.getX() + 0.5, surfacePos.getY(), surfacePos.getZ() + 0.5, false);

        // Particles at new position
        serverWorld.spawnParticles(
                ParticleTypes.REVERSE_PORTAL,
                this.getX(), this.getY() + 2.0, this.getZ(),
                20, 0.5, 1.0, 0.5, 0.1
        );

        this.playSound(ModSoundEvents.ENTITY_ECHO_WARDEN_TIME_SLOW, 1.0f, 0.7f);
    }

    private void activateBlinkStrike() {
        if (this.getWorld().isClient) return;
        ServerWorld serverWorld = (ServerWorld) this.getWorld();

        sendMessageToAllPlayers("message.void_echo.warden_blink_strike");

        // Teleport rapidly between 3 positions attacking from each
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;

        for (int i = 0; i < 3; i++) {
            double angle = (Math.PI * 2 * i) / 3 + this.random.nextDouble();
            double x = target.getX() + Math.cos(angle) * 4.0;
            double z = target.getZ() + Math.sin(angle) * 4.0;
            BlockPos surfacePos = serverWorld.getTopPosition(
                    net.minecraft.world.Heightmap.Type.WORLD_SURFACE,
                    BlockPos.ofFloored(x, target.getY(), z)
            );

            this.teleport(surfacePos.getX() + 0.5, surfacePos.getY(), surfacePos.getZ() + 0.5, false);

            // Arrival particles
            serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                    this.getX(), this.getY() + 2.0, this.getZ(),
                    15, 0.5, 1.0, 0.5, 0.1);
            // Arrival burst
            serverWorld.spawnParticles(ModParticleTypes.VOID_BURST,
                    this.getX(), this.getY() + 2.0, this.getZ(),
                    30, 1.0, 1.5, 1.0, 0.12);

            // Attack from each position if close enough
            if (this.distanceTo(target) <= MELEE_RANGE) {
                target.damage(target.getDamageSources().mobAttack(this), MELEE_DAMAGE);
            }

            this.playSound(ModSoundEvents.ENTITY_ECHO_WARDEN_TIME_SLOW, 1.0f, 1.2f);
        }

        this.blinkStrikeCooldown = BLINK_STRIKE_COOLDOWN_TICKS;
    }

    private void activateTimeSlow() {
        if (this.getWorld().isClient) return;
        ServerWorld serverWorld = (ServerWorld) this.getWorld();

        sendMessageToAllPlayers("message.void_echo.warden_time_slow");

        // Apply effects to all players within range
        for (PlayerEntity player : serverWorld.getPlayers()) {
            if (player.isAlive() && player.distanceTo(this) <= TIME_SLOW_RADIUS) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SLOWNESS, 100, 4, false, true
                ));
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.MINING_FATIGUE, 100, 2, false, true
                ));
                player.addStatusEffect(new StatusEffectInstance(
                        ModEffects.ECHO_RESONANCE, 100, 0, false, true
                ));
            }
        }

        // Visual effect
        serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL,
                this.getX(), this.getY() + 2.0, this.getZ(),
                50, TIME_SLOW_RADIUS, 3.0, TIME_SLOW_RADIUS, 0.5);
        // Suspended ambient particles — "time freeze" visual
        serverWorld.spawnParticles(ModParticleTypes.VOID_AMBIENT,
                this.getX(), this.getY() + 1.0, this.getZ(),
                30, TIME_SLOW_RADIUS * 0.7, 4.0, TIME_SLOW_RADIUS * 0.7, 0);

        this.playSound(ModSoundEvents.ENTITY_ECHO_WARDEN_TIME_SLOW, 1.0f, 0.6f);
        this.timeSlowCooldown = TIME_SLOW_COOLDOWN_TICKS;
    }

    private void startSlashCharge() {
        if (this.getWorld().isClient) return;
        ServerWorld serverWorld = (ServerWorld) this.getWorld();

        sendMessageToAllPlayers("message.void_echo.warden_slash_charge");

        this.isSlashCharging = true;
        this.slashChargeTime = SLASH_CHARGE_TICKS;
        this.navigation.stop();

        // Charge visual buildup
        serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL,
                this.getX(), this.getY() + 2.0, this.getZ(),
                30, 1.0, 3.0, 1.0, 0.2);
        // Sword arc — beam particles in a horizontal fan ahead of the boss
        for (int i = -15; i <= 15; i++) {
            double arcAngle = Math.toRadians(i * 6);
            serverWorld.spawnParticles(ModParticleTypes.VOID_BEAM,
                    this.getX() + Math.sin(arcAngle) * 3.0,
                    this.getY() + 2.0,
                    this.getZ() + Math.cos(arcAngle) * 3.0,
                    2, 0.2, 0.5, 0.2, 0);
        }
        serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM,
                this.getX(), this.getY() + 2.0, this.getZ(),
                10, 0.5, 2.0, 0.5, 0.05);

        this.playSound(ModSoundEvents.ENTITY_ECHO_WARDEN_SLASH, 1.5f, 0.7f);
    }

    private void executeFullScreenSlash() {
        if (this.getWorld().isClient) return;
        ServerWorld serverWorld = (ServerWorld) this.getWorld();

        sendMessageToAllPlayers("message.void_echo.warden_slash");

        // Massive particle burst
        serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM,
                this.getX(), this.getY() + 2.0, this.getZ(),
                60, SLASH_RADIUS, 4.0, SLASH_RADIUS, 0.3);
        // Slash debris — burst fragments flying along the slash direction
        serverWorld.spawnParticles(ModParticleTypes.VOID_BURST,
                this.getX(), this.getY() + 2.0, this.getZ(),
                50, SLASH_RADIUS * 0.6, 2.0, SLASH_RADIUS * 0.6, 0.25);
        serverWorld.spawnParticles(ParticleTypes.FLASH,
                this.getX(), this.getY() + 2.0, this.getZ(),
                1, 0, 0, 0, 0);

        // Damage ALL players within radius regardless of line of sight
        for (PlayerEntity player : serverWorld.getPlayers()) {
            if (player.isAlive() && !player.isSpectator() && !player.isCreative()
                    && player.distanceTo(this) <= SLASH_RADIUS) {
                player.damage(player.getDamageSources().mobAttack(this), SLASH_DAMAGE);
                player.takeKnockback(3.0, player.getX() - this.getX(), player.getZ() - this.getZ());
                player.addStatusEffect(new StatusEffectInstance(
                        ModEffects.VOID_TOUCHED, 100, 1, false, true
                ));
            }
        }

        // Screen shake-like effect through player hurt animation
        this.playSound(ModSoundEvents.ENTITY_ECHO_WARDEN_SLASH, 2.0f, 0.8f);
        this.playSound(ModSoundEvents.ENTITY_ECHO_WARDEN_SLASH, 2.0f, 0.5f);
    }

    // ---- Phase Transition & Multi-Phase Skills ----

    private void checkPhaseTransition() {
        float hpRatio = this.getHealth() / this.getMaxHealth();
        if (!phaseTwoTriggered && hpRatio <= 0.5f) {
            currentPhase = 2;
            phaseTwoTriggered = true;
            applyPhaseAttributes();
            sendMessageToAllPlayers("message.void_echo.warden_phase_two");
            this.playSound(ModSoundEvents.ENTITY_ECHO_WARDEN_SLASH, 1.5f, 0.6f);
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL,
                    this.getX(), this.getY() + 2.0, this.getZ(), 80, 3.0, 3.0, 3.0, 0.3);
                serverWorld.spawnParticles(ModParticleTypes.VOID_BURST,
                    this.getX(), this.getY() + 2.0, this.getZ(), 40, 2.0, 2.0, 2.0, 0.2);
            }
        }
        if (!phaseThreeTriggered && hpRatio <= 0.25f) {
            currentPhase = 3;
            phaseThreeTriggered = true;
            applyPhaseAttributes();
            sendMessageToAllPlayers("message.void_echo.warden_phase_three");
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
                    this.getX(), this.getY() + 2.0, this.getZ(), 3, 0, 0, 0, 0);
                serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM,
                    this.getX(), this.getY() + 2.0, this.getZ(), 40, 4.0, 4.0, 4.0, 0.3);
            }
            this.playSound(ModSoundEvents.ENTITY_ECHO_WARDEN_SLASH, 2.0f, 0.4f);
        }
    }

    private void applyPhaseAttributes() {
        if (currentPhase >= 3) {
            // Phase 3: +30% move speed
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                .setBaseValue(0.25 * 1.3);
        }
    }

    private void activateEchoWave() {
        if (this.getWorld().isClient) return;
        ServerWorld serverWorld = (ServerWorld) this.getWorld();
        sendMessageToAllPlayers("message.void_echo.warden_echo_wave");

        // Fan-shaped knockback + damage
        for (PlayerEntity player : serverWorld.getPlayers()) {
            if (player.isAlive() && !player.isSpectator() && !player.isCreative()
                    && player.distanceTo(this) <= ECHO_WAVE_RANGE) {
                // Check if player is in front (within ~120° cone)
                Vec3d toPlayer = player.getPos().subtract(this.getPos()).normalize();
                Vec3d facing = this.getRotationVec(1.0f);
                if (toPlayer.dotProduct(facing) > 0.3) { // ~72° half-angle
                    player.damage(player.getDamageSources().mobAttack(this), ECHO_WAVE_DAMAGE);
                    player.takeKnockback(2.5, player.getX() - this.getX(), player.getZ() - this.getZ());
                    player.addStatusEffect(new StatusEffectInstance(
                        ModEffects.VOID_TOUCHED, 80, 0, false, true));
                }
            }
        }

        // Wave particle ring
        for (int i = 0; i < 60; i++) {
            double angle = Math.PI * 2 * i / 60;
            double x = this.getX() + Math.cos(angle) * ECHO_WAVE_RANGE;
            double z = this.getZ() + Math.sin(angle) * ECHO_WAVE_RANGE;
            serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, x, this.getY() + 1.5, z,
                1, 0, 0, 0, 0);
        }
        this.playSound(ModSoundEvents.ENTITY_ECHO_WARDEN_SLASH, 1.5f, 1.0f);
    }

    private void activateVoidDrain() {
        if (this.getWorld().isClient) return;
        ServerWorld serverWorld = (ServerWorld) this.getWorld();
        sendMessageToAllPlayers("message.void_echo.warden_void_drain");

        int drained = 0;
        for (PlayerEntity player : serverWorld.getPlayers()) {
            if (player.isAlive() && !player.isSpectator() && !player.isCreative()
                    && player.distanceTo(this) <= VOID_DRAIN_RADIUS) {
                player.damage(player.getDamageSources().magic(), 1.0f);
                drained++;
                // Drain particles from player to boss
                serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    5, 0.3, 0.5, 0.3, 0.05);
                serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL,
                    this.getX(), this.getY() + 2.0, this.getZ(),
                    3, 0.5, 1.0, 0.5, 0.05);
                // Particle flow line from player to boss
                Vec3d from = player.getPos().add(0, 1, 0);
                Vec3d to = this.getPos().add(0, 2, 0);
                Vec3d dir = to.subtract(from);
                double len = dir.length();
                dir = dir.normalize();
                for (double d = 0; d < len; d += 0.4) {
                    Vec3d pt = from.add(dir.multiply(d));
                    serverWorld.spawnParticles(ModParticleTypes.VOID_AMBIENT,
                        pt.x, pt.y, pt.z, 1, 0.05, 0.05, 0.05, 0);
                }
            }
        }
        // Heal 2 HP per player drained
        if (drained > 0) {
            this.heal(drained * 2.0f);
            // Drain success burst at boss position
            serverWorld.spawnParticles(ModParticleTypes.VOID_BURST,
                    this.getX(), this.getY() + 2.0, this.getZ(),
                    15 * drained, 1.0, 1.5, 1.0, 0.1);
        }
        this.playSound(ModSoundEvents.ENTITY_ECHO_WARDEN_TIME_SLOW, 1.0f, 0.5f);
    }

    private void startVoidNovaCharge() {
        if (this.getWorld().isClient) return;
        ServerWorld serverWorld = (ServerWorld) this.getWorld();
        sendMessageToAllPlayers("message.void_echo.warden_void_nova_charge");

        this.isVoidNovaCharging = true;
        this.voidNovaChargeTime = VOID_NOVA_CHARGE_TICKS;
        this.navigation.stop();

        // Charge particles — inward spiral
        for (int i = 0; i < 60; i++) {
            double angle = (i / 60.0) * Math.PI * 6;
            double radius = 5.0 - (i / 60.0) * 4.0;
            serverWorld.spawnParticles(ModParticleTypes.VOID_AMBIENT,
                this.getX() + Math.cos(angle) * radius,
                this.getY() + 1.0 + (i % 3) * 0.8,
                this.getZ() + Math.sin(angle) * radius,
                1, 0, 0, 0, 0);
        }
        serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL,
            this.getX(), this.getY() + 1.0, this.getZ(),
            30, 1.5, 3.0, 1.5, 0.1);
        this.playSound(ModSoundEvents.ENTITY_ECHO_WARDEN_SLASH, 1.0f, 0.3f);
    }

    private void executeVoidNova() {
        if (this.getWorld().isClient) return;
        ServerWorld serverWorld = (ServerWorld) this.getWorld();
        sendMessageToAllPlayers("message.void_echo.warden_void_nova");

        // Explosion particles
        serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
            this.getX(), this.getY() + 2.0, this.getZ(), 1, 0, 0, 0, 0);
        serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM,
            this.getX(), this.getY() + 2.0, this.getZ(),
            80, VOID_NOVA_RADIUS, 4.0, VOID_NOVA_RADIUS, 0.4);
        // Hemisphere burst — void energy expanding outward
        serverWorld.spawnParticles(ModParticleTypes.VOID_BURST,
            this.getX(), this.getY() + 2.0, this.getZ(),
            100, VOID_NOVA_RADIUS * 0.8, 5.0, VOID_NOVA_RADIUS * 0.8, 0.3);
        // Lingering ground fog — void energy residue
        serverWorld.spawnParticles(ModParticleTypes.VOID_AMBIENT,
            this.getX(), this.getY() + 0.1, this.getZ(),
            50, VOID_NOVA_RADIUS, 0.2, VOID_NOVA_RADIUS, 0.01);

        // Damage all players in radius
        for (PlayerEntity player : serverWorld.getPlayers()) {
            if (player.isAlive() && !player.isSpectator() && !player.isCreative()
                    && player.distanceTo(this) <= VOID_NOVA_RADIUS) {
                player.damage(player.getDamageSources().mobAttack(this), VOID_NOVA_DAMAGE);
                player.takeKnockback(4.0, player.getX() - this.getX(), player.getZ() - this.getZ());
                player.addStatusEffect(new StatusEffectInstance(
                    ModEffects.VOID_TOUCHED, 160, 1, false, true));
            }
        }
        this.playSound(ModSoundEvents.ENTITY_ECHO_WARDEN_SLASH, 2.0f, 0.3f);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
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

    private void sendMessageToAllPlayers(String translationKey) {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            Text message = Text.translatable(translationKey);
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                if (!player.isSpectator()) {
                    player.sendMessage(message, true);
                }
            }
        }
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean allowDrops) {
        super.dropEquipment(world, source, allowDrops);
        // Echo core (guaranteed)
        this.dropItem(ModItems.ECHO_CORE);
        // Void alloy ingots (2-5)
        int alloyCount = 2 + this.random.nextInt(4);
        for (int i = 0; i < alloyCount; i++) {
            this.dropItem(ModItems.VOID_ALLOY_INGOT);
        }
        // Crystal shards (5-12)
        int shardCount = 5 + this.random.nextInt(8);
        for (int i = 0; i < shardCount; i++) {
            this.dropItem(ModItems.CRYSTAL_SHARD);
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            Text message = Text.translatable("message.void_echo.warden_defeated");
            Text ending = Text.translatable("message.void_echo.warden_ending");
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                if (player.distanceTo(this) <= 64.0) {
                    player.sendMessage(message, false);
                    player.sendMessage(ending, false);
                }
            }

            // Dramatic death effects
            serverWorld.spawnParticles(
                    ParticleTypes.EXPLOSION_EMITTER,
                    this.getX(), this.getY() + 2.0, this.getZ(),
                    2, 0, 0, 0, 0
            );
            serverWorld.spawnParticles(
                    ParticleTypes.SCULK_SOUL,
                    this.getX(), this.getY() + 1.0, this.getZ(),
                    150, 5.0, 3.0, 5.0, 0.4
            );
            serverWorld.spawnParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    this.getX(), this.getY() + 1.0, this.getZ(),
                    100, 3.0, 3.0, 3.0, 0.3
            );

            this.playSound(ModSoundEvents.ENTITY_ECHO_WARDEN_DEATH, 2.0f, 0.5f);
        }

        this.bossBar.clearPlayers();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("blinkStrikeCooldown", blinkStrikeCooldown);
        nbt.putInt("timeSlowCooldown", timeSlowCooldown);
        nbt.putInt("slashCooldown", slashCooldown);
        nbt.putInt("passiveTeleportCooldown", teleportCooldown);
        nbt.putInt("abilityTickCounter", abilityTickCounter);
        nbt.putInt("currentPhase", currentPhase);
        nbt.putBoolean("phaseTwoTriggered", phaseTwoTriggered);
        nbt.putBoolean("phaseThreeTriggered", phaseThreeTriggered);
        nbt.putInt("echoWaveCooldown", echoWaveCooldown);
        nbt.putInt("voidDrainCooldown", voidDrainCooldown);
        nbt.putInt("voidNovaCooldown", voidNovaCooldown);
        nbt.putBoolean("isSlashCharging", isSlashCharging);
        nbt.putInt("slashChargeTime", slashChargeTime);
        nbt.putBoolean("isVoidNovaCharging", isVoidNovaCharging);
        nbt.putInt("voidNovaChargeTime", voidNovaChargeTime);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        blinkStrikeCooldown = nbt.getInt("blinkStrikeCooldown");
        timeSlowCooldown = nbt.getInt("timeSlowCooldown");
        slashCooldown = nbt.getInt("slashCooldown");
        teleportCooldown = nbt.getInt("passiveTeleportCooldown");
        abilityTickCounter = nbt.getInt("abilityTickCounter");
        currentPhase = nbt.getInt("currentPhase");
        if (currentPhase == 0) currentPhase = 1;
        phaseTwoTriggered = nbt.getBoolean("phaseTwoTriggered");
        phaseThreeTriggered = nbt.getBoolean("phaseThreeTriggered");
        echoWaveCooldown = nbt.getInt("echoWaveCooldown");
        voidDrainCooldown = nbt.getInt("voidDrainCooldown");
        voidNovaCooldown = nbt.getInt("voidNovaCooldown");
        isSlashCharging = nbt.getBoolean("isSlashCharging");
        slashChargeTime = nbt.getInt("slashChargeTime");
        isVoidNovaCharging = nbt.getBoolean("isVoidNovaCharging");
        voidNovaChargeTime = nbt.getInt("voidNovaChargeTime");
        applyPhaseAttributes();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundEvents.ENTITY_ECHO_WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSoundEvents.ENTITY_ECHO_WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundEvents.ENTITY_ECHO_WARDEN_DEATH;
    }

    // ---- Custom AI Goals ----

    private static class EchoWardenSlashGoal extends Goal {
        private final EchoWardenEntity warden;

        public EchoWardenSlashGoal(EchoWardenEntity warden) {
            this.warden = warden;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (warden.isSlashCharging) return false;
            if (warden.slashCooldown > 0) return false;
            LivingEntity target = warden.getTarget();
            if (target == null || !target.isAlive()) return false;
            return warden.distanceTo(target) <= 32.0;
        }

        @Override
        public void start() {
            warden.startSlashCharge();
        }

        @Override
        public boolean shouldContinue() {
            return warden.isSlashCharging;
        }
    }

    private static class EchoWardenBlinkStrikeGoal extends Goal {
        private final EchoWardenEntity warden;

        public EchoWardenBlinkStrikeGoal(EchoWardenEntity warden) {
            this.warden = warden;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (warden.blinkStrikeCooldown > 0) return false;
            if (warden.isSlashCharging) return false;
            LivingEntity target = warden.getTarget();
            if (target == null || !target.isAlive()) return false;
            return warden.distanceTo(target) <= 32.0;
        }

        @Override
        public void start() {
            warden.activateBlinkStrike();
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }
    }

    private static class EchoWardenMeleeGoal extends MeleeAttackGoal {
        private final EchoWardenEntity warden;

        public EchoWardenMeleeGoal(EchoWardenEntity warden) {
            super(warden, 1.0, false);
            this.warden = warden;
        }

        @Override
        public boolean canStart() {
            if (warden.isSlashCharging) return false;
            return super.canStart();
        }

        @Override
        protected boolean canAttack(LivingEntity target) {
            return this.mob.squaredDistanceTo(target) <= 25.0;
        }
    }
}
