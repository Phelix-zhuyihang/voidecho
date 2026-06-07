package com.example.voidecho.world.event;

import com.example.voidecho.entity.ModEntities;
import com.example.voidecho.entity.mob.CrystalWraithEntity;
import com.example.voidecho.entity.mob.ShardGuardEntity;
import com.example.voidecho.entity.mob.VoidWormEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import com.example.voidecho.item.ModItems;
import com.example.voidecho.world.dimension.ModDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class VoidRiftManager {
    private static final Map<ServerWorld, List<ActiveRift>> activeRifts = new HashMap<>();
    private static final int MAX_RIFTS_PER_WORLD = 5;
    private static int checkCounter = 0;

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getRegistryKey() != ModDimensions.VOIDS_END_LEVEL_KEY) return;

            // Process existing rifts every tick
            List<ActiveRift> worldRifts = activeRifts.computeIfAbsent(world, k -> new ArrayList<>());
            processRifts(world, worldRifts);

            // Check for new rift spawns every 200 ticks (10 seconds)
            checkCounter++;
            if (checkCounter % 200 != 0) return;
            trySpawnNewRift(world, worldRifts);
        });

        // Clean up stale ServerWorld references on dimension unload
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            activeRifts.remove(world);
        });
    }

    private static void processRifts(ServerWorld world, List<ActiveRift> worldRifts) {
        Iterator<ActiveRift> iter = worldRifts.iterator();
        while (iter.hasNext()) {
            ActiveRift rift = iter.next();
            rift.ticksRemaining--;
            rift.elapsedTicks++;

            // Update stage based on elapsed time
            int prevStage = rift.stage;
            if (rift.elapsedTicks >= 1600) { // 80 seconds
                rift.stage = 3;
            } else if (rift.elapsedTicks >= 800) { // 40 seconds
                rift.stage = 2;
            }

            // Stage transition: spawn extra mobs
            if (rift.stage >= 2 && !rift.stageTwoMobsSpawned) {
                rift.stageTwoMobsSpawned = true;
                spawnStageTwoMobs(world, rift);
            }
            if (rift.stage >= 3 && !rift.stageThreeMobsSpawned) {
                rift.stageThreeMobsSpawned = true;
                spawnStageThreeMobs(world, rift);
            }

            // Particle radius grows with stage
            float particleRadius = rift.stage >= 3 ? 10.0f : (rift.stage >= 2 ? 7.0f : 5.0f);
            int particleCount = rift.stage >= 3 ? 16 : (rift.stage >= 2 ? 12 : 8);
            rift.particleTimer--;
            if (rift.particleTimer <= 0) {
                rift.particleTimer = 20;
                for (int i = 0; i < particleCount; i++) {
                    double x = rift.center.getX() + 0.5 + (world.random.nextDouble() - 0.5) * particleRadius;
                    double y = rift.center.getY() + world.random.nextDouble() * 3;
                    double z = rift.center.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * particleRadius;
                    world.spawnParticles(ParticleTypes.PORTAL, x, y, z, 1, 0, 0, 0, 0);
                }
            }

            // Stage 3: additional ambient particles
            if (rift.stage >= 3 && world.random.nextFloat() < 0.3f) {
                world.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                    rift.center.getX() + 0.5, rift.center.getY() + 1.5, rift.center.getZ() + 0.5,
                    1, 2.0, 0.5, 2.0, 0);
            }

            // Check for nearby players to build close progress
            boolean playerNearby = false;
            for (ServerPlayerEntity player : world.getPlayers()) {
                if (player.getBlockPos().isWithinDistance(rift.center, 3.0)) {
                    playerNearby = true;
                    break;
                }
            }
            if (playerNearby) {
                rift.closeProgress++;
                if (rift.closeProgress >= 60) { // 3 seconds at 20 TPS
                    closeRift(world, rift);
                    iter.remove();
                    continue;
                }
            } else {
                rift.closeProgress = 0;
            }

            // Timeout - rift closes naturally
            if (rift.ticksRemaining <= 0) {
                timeoutRift(world, rift);
                iter.remove();
            }
        }
    }

    private static void closeRift(ServerWorld world, ActiveRift rift) {
        // Restore original block
        world.setBlockState(rift.center, rift.originalState);

        // Stage-based rewards
        int fragmentCount;
        switch (rift.stage) {
            case 3:
                fragmentCount = world.random.nextBetween(4, 7);
                // Bonus: chance to drop void alloy ingot at stage 3
                if (world.random.nextFloat() < 0.3f) {
                    Block.dropStack(world, rift.center.up(), new ItemStack(ModItems.VOID_ALLOY_INGOT));
                }
                break;
            case 2:
                fragmentCount = world.random.nextBetween(2, 5);
                break;
            default:
                fragmentCount = world.random.nextBetween(1, 2);
                break;
        }
        Block.dropStack(world, rift.center.up(), new ItemStack(ModItems.RIFT_FRAGMENT, fragmentCount));

        // Apply Glowing to spawned mobs
        for (LivingEntity mob : rift.spawnedMobs) {
            if (mob != null && mob.isAlive()) {
                mob.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0));
            }
        }

        // Notify nearby players
        sendMessageToNearby(world, rift.center, Text.translatable("message.void_echo.rift_closed"));
    }

    private static void timeoutRift(ServerWorld world, ActiveRift rift) {
        // Restore original block
        world.setBlockState(rift.center, rift.originalState);

        // Despawn remaining mobs
        for (LivingEntity mob : rift.spawnedMobs) {
            if (mob != null && mob.isAlive()) {
                mob.discard();
            }
        }

        sendMessageToNearby(world, rift.center, Text.translatable("message.void_echo.rift_closed"));
    }

    private static void trySpawnNewRift(ServerWorld world, List<ActiveRift> worldRifts) {
        // Enforce global rift cap per world
        if (worldRifts.size() >= MAX_RIFTS_PER_WORLD) return;

        for (ServerPlayerEntity player : world.getPlayers()) {
            // Check if player already has a rift nearby
            boolean hasNearbyRift = false;
            for (ActiveRift rift : worldRifts) {
                if (rift.center.isWithinDistance(player.getBlockPos(), 64.0)) {
                    hasNearbyRift = true;
                    break;
                }
            }
            if (hasNearbyRift) continue;

            // 0.1% chance per check
            if (world.random.nextFloat() < 0.001f) {
                BlockPos center = player.getBlockPos().add(
                        world.random.nextBetween(-30, 30),
                        0,
                        world.random.nextBetween(-30, 30)
                );
                center = new BlockPos(center.getX(), world.getTopY(), center.getZ());
                // Find surface
                center = world.getTopPosition(
                        net.minecraft.world.Heightmap.Type.WORLD_SURFACE, center
                );

                // Save original block state and place visual marker
                BlockState originalState = world.getBlockState(center);
                world.setBlockState(center, Blocks.CRYING_OBSIDIAN.getDefaultState());

                ActiveRift newRift = new ActiveRift(center, originalState);
                worldRifts.add(newRift);

                // Spawn initial wave and track spawned mobs
                spawnRiftMobs(world, center, newRift);

                // Spawn initial portal particle burst
                for (int i = 0; i < 20; i++) {
                    double x = center.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 5;
                    double y = center.getY() + world.random.nextDouble() * 3;
                    double z = center.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 5;
                    world.spawnParticles(ParticleTypes.PORTAL, x, y, z, 1, 0, 0, 0, 0);
                }

                // Notify nearby players
                Text msg = Text.translatable("message.void_echo.rift_opened");
                player.sendMessage(msg, true);
                for (ServerPlayerEntity other : world.getPlayers()) {
                    if (other != player && other.getBlockPos().isWithinDistance(center, 64.0)) {
                        other.sendMessage(msg, true);
                    }
                }
            }
        }
    }

    private static void spawnRiftMobs(ServerWorld world, BlockPos center, ActiveRift rift) {
        // Spawn 3-5 void worms
        int wormCount = world.random.nextBetween(3, 6);
        for (int i = 0; i < wormCount; i++) {
            VoidWormEntity worm = ModEntities.VOID_WORM.create(world);
            if (worm != null) {
                worm.setPosition(
                        center.getX() + world.random.nextBetween(-5, 5),
                        center.getY(),
                        center.getZ() + world.random.nextBetween(-5, 5)
                );
                world.spawnEntity(worm);
                rift.spawnedMobs.add(worm);
            }
        }

        // Spawn 1-2 crystal wraiths
        int wraithCount = world.random.nextBetween(1, 3);
        for (int i = 0; i < wraithCount; i++) {
            CrystalWraithEntity wraith = ModEntities.CRYSTAL_WRAITH.create(world);
            if (wraith != null) {
                wraith.setPosition(
                        center.getX() + world.random.nextBetween(-5, 5),
                        center.getY() + 3,
                        center.getZ() + world.random.nextBetween(-5, 5)
                );
                world.spawnEntity(wraith);
                rift.spawnedMobs.add(wraith);
            }
        }
    }

    private static void spawnStageTwoMobs(ServerWorld world, ActiveRift rift) {
        // Add 2-3 ShardGuards
        int guardCount = world.random.nextBetween(2, 4);
        for (int i = 0; i < guardCount; i++) {
            ShardGuardEntity guard = ModEntities.SHARD_GUARD.create(world);
            if (guard != null) {
                guard.setPosition(
                    rift.center.getX() + world.random.nextBetween(-7, 7),
                    rift.center.getY(),
                    rift.center.getZ() + world.random.nextBetween(-7, 7)
                );
                world.spawnEntity(guard);
                rift.spawnedMobs.add(guard);
            }
        }
    }

    private static void spawnStageThreeMobs(ServerWorld world, ActiveRift rift) {
        // Spawn 1-2 buffed Crystal Wraiths (+50% HP/damage, glowing)
        int wraithCount = world.random.nextBetween(1, 3);
        for (int i = 0; i < wraithCount; i++) {
            CrystalWraithEntity wraith = ModEntities.CRYSTAL_WRAITH.create(world);
            if (wraith != null) {
                wraith.setPosition(
                    rift.center.getX() + world.random.nextBetween(-10, 10),
                    rift.center.getY() + 3,
                    rift.center.getZ() + world.random.nextBetween(-10, 10)
                );
                // Buffed: +50% HP
                wraith.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                    .setBaseValue(30.0);
                wraith.setHealth(30.0f);
                wraith.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 2400, 0));
                world.spawnEntity(wraith);
                rift.spawnedMobs.add(wraith);
            }
        }
    }

    private static void sendMessageToNearby(ServerWorld world, BlockPos pos, Text message) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.getBlockPos().isWithinDistance(pos, 64.0)) {
                player.sendMessage(message, true);
            }
        }
    }

    private static class ActiveRift {
        final BlockPos center;
        final BlockState originalState;
        final List<LivingEntity> spawnedMobs = new ArrayList<>();
        int ticksRemaining;
        int closeProgress;
        int particleTimer;
        int elapsedTicks;
        int stage; // 1=small, 2=medium, 3=large
        boolean stageTwoMobsSpawned;
        boolean stageThreeMobsSpawned;

        ActiveRift(BlockPos center, BlockState originalState) {
            this.center = center;
            this.originalState = originalState;
            this.ticksRemaining = 2400; // 2 minutes
            this.closeProgress = 0;
            this.particleTimer = 0;
            this.elapsedTicks = 0;
            this.stage = 1;
            this.stageTwoMobsSpawned = false;
            this.stageThreeMobsSpawned = false;
        }
    }
}
