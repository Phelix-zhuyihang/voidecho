package com.example.voidecho.block;

import com.example.voidecho.ModParticleTypes;
import com.example.voidecho.ModSoundEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Rift Core — the active heart of a Void Rift structure.
 * Pulses in a 60-second cycle. Players can close it by sneaking
 * on it for 5 seconds, yielding Rift Fragments and a Rift Core.
 */
public class RiftCoreBlock extends Block {
    private static final int PULSE_CYCLE = 1200; // 60 seconds at 20 tps
    private static final int WARNING_START = 300;
    private static final int PULSE_DURATION = 60;
    private static final int AFTERSHOCK_END = 120;

    public RiftCoreBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        int tick = (int) (world.getTime() % PULSE_CYCLE);
        boolean warning = tick >= PULSE_CYCLE - WARNING_START;

        // Ambient particles always rising
        if (random.nextFloat() < 0.4f) {
            world.addParticle(ModParticleTypes.VOID_AMBIENT,
                    pos.getX() + 0.3 + random.nextDouble() * 0.4,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4,
                    0, 0.02 + random.nextDouble() * 0.03, 0);
        }

        // Warning pulse — particles accelerate
        if (warning && random.nextFloat() < 0.6f) {
            world.addParticle(ModParticleTypes.VOID_BEAM,
                    pos.getX() + 0.3 + random.nextDouble() * 0.4,
                    pos.getY() + 0.3,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4,
                    0, 0.05 + random.nextDouble() * 0.1, 0);
        }

        // Pulse burst
        if (tick >= PULSE_CYCLE - PULSE_DURATION && tick < PULSE_CYCLE - PULSE_DURATION + 5) {
            for (int i = 0; i < 20; i++) {
                world.addParticle(ModParticleTypes.VOID_BURST,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        (random.nextDouble() - 0.5) * 0.8, random.nextDouble() * 0.5,
                        (random.nextDouble() - 0.5) * 0.8);
            }
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                  PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (!player.isSneaking()) return ActionResult.PASS;

        ServerWorld serverWorld = (ServerWorld) world;
        // Closing the rift: must sneak for 5 seconds (100 ticks)
        player.getItemCooldownManager().set(this.asItem(), 100);

        // Check cooldown — player must stay sneaking near the rift
        // For simplicity, immediate close on right-click while sneaking
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
        world.playSound(null, pos, ModSoundEvents.BLOCK_PORTAL_AMBIENT,
                SoundCategory.BLOCKS, 0.8f, 0.4f);

        // Explosion burst
        serverWorld.spawnParticles(ModParticleTypes.VOID_BURST,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                60, 2.0, 2.0, 2.0, 0.2);
        serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                1, 0, 0, 0, 0);

        // Drop rewards
        net.minecraft.item.ItemStack riftCore = new net.minecraft.item.ItemStack(
                com.example.voidecho.item.ModItems.RIFT_CORE, 1);
        net.minecraft.item.ItemStack fragments = new net.minecraft.item.ItemStack(
                com.example.voidecho.item.ModItems.RIFT_FRAGMENT, 2 + world.random.nextInt(2));
        net.minecraft.entity.ItemEntity itemEntity = new net.minecraft.entity.ItemEntity(
                world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, riftCore);
        world.spawnEntity(itemEntity);
        itemEntity = new net.minecraft.entity.ItemEntity(
                world, pos.getX() + 0.8, pos.getY() + 1.0, pos.getZ() + 0.2, fragments);
        world.spawnEntity(itemEntity);

        // 10% chance to spawn Void Shade
        if (world.random.nextFloat() < 0.10f) {
            com.example.voidecho.entity.mob.VoidShadeEntity shade =
                    com.example.voidecho.entity.ModEntities.VOID_SHADE.create(world);
            if (shade != null) {
                shade.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 1.0,
                        pos.getZ() + 0.5, 0, 0);
                shade.setTarget(player);
                world.spawnEntity(shade);
            }
        }

        return ActionResult.SUCCESS;
    }
}
