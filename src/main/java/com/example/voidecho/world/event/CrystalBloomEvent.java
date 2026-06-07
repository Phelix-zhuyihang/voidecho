package com.example.voidecho.world.event;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.block.ModBlocks;
import com.example.voidecho.world.biome.ModBiomes;
import com.example.voidecho.world.dimension.ModDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

public class CrystalBloomEvent {
    private static int tickCounter = 0;
    private static final int BLOOM_INTERVAL = 120000; // 5 MC days

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getRegistryKey().equals(ModDimensions.VOIDS_END_LEVEL_KEY)) {
                tickCounter++;
                if (tickCounter >= BLOOM_INTERVAL) {
                    tickCounter = 0;
                    VoidEcho.LOGGER.info("Crystal Bloom event triggered in the Void's End!");
                    triggerBloom((ServerWorld) world);
                }
            }
        });
    }

    private static void triggerBloom(ServerWorld world) {
        // Find all players in crystal_forest biome
        for (ServerPlayerEntity player : world.getPlayers()) {
            BlockPos playerPos = player.getBlockPos();
            var biome = world.getBiome(playerPos);
            if (biome.matchesKey(ModBiomes.CRYSTAL_FOREST)) {
                // Spawn crystal blooms around the player
                for (int i = 0; i < 10; i++) {
                    BlockPos pos = playerPos.add(
                        world.random.nextBetween(-8, 8),
                        world.random.nextBetween(-2, 2),
                        world.random.nextBetween(-8, 8)
                    );
                    BlockPos groundPos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, pos).down();
                    BlockState ground = world.getBlockState(groundPos);
                    if (ground.isOf(ModBlocks.VOID_GRASS_BLOCK) || ground.isOf(ModBlocks.VOID_DIRT)
                            || ground.isOf(ModBlocks.VOID_STONE)) {
                        BlockPos bloomPos = groundPos.up();
                        if (world.getBlockState(bloomPos).isAir()) {
                            world.setBlockState(bloomPos, ModBlocks.CRYSTAL_BLOOM.getDefaultState(), 3);
                        }
                    }
                }
                player.sendMessage(Text.translatable("message.void_echo.crystal_bloom"), true);
            }
        }
    }
}
