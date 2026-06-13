package com.example.voidecho.block;

import com.example.voidecho.ModSoundEvents;
import com.example.voidecho.VoidEcho;
import com.example.voidecho.block.ModBlocks;
import com.example.voidecho.item.ModItems;
import com.example.voidecho.world.dimension.ModDimensions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

public class VoidPortalFrameBlock extends Block {
    public VoidPortalFrameBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        // F4: Return from void dimension (no key needed)
        if (!world.isClient && world.getRegistryKey().equals(ModDimensions.VOIDS_END_LEVEL_KEY)) {
            if (player.getServer() != null) {
                ServerWorld overworld = player.getServer().getOverworld();
                BlockPos returnPos = PortalStorage.get(overworld).getReturnPosition(
                        player.getUuid(), overworld.getSpawnPos());
                Vec3d safePos = new Vec3d(returnPos.getX() + 0.5, returnPos.getY() + 1.0, returnPos.getZ() + 0.5);
                player.teleportTo(new TeleportTarget(overworld, safePos,
                        Vec3d.ZERO, player.getYaw(), player.getPitch(), TeleportTarget.NO_OP));
                PortalStorage.get(overworld).clearReturnPosition(player.getUuid());
                player.sendMessage(Text.translatable("message.void_echo.return_to_overworld"), true);
            }
            return ActionResult.SUCCESS;
        }

        // Check both hands for void key, preferring main hand
        ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack offHand = player.getStackInHand(Hand.OFF_HAND);
        ItemStack held = mainHand.isOf(ModItems.VOID_KEY) ? mainHand
                : offHand.isOf(ModItems.VOID_KEY) ? offHand : ItemStack.EMPTY;

        if (!held.isEmpty()) {
            if (!world.isClient) {
                // Play activation sound
                world.playSound(null, pos, ModSoundEvents.BLOCK_PORTAL_ACTIVATE,
                        SoundCategory.BLOCKS, 1.0f, 1.0f);

                // Teleport to the void dimension
                if (player.getServer() != null) {
                    ServerWorld overworld = player.getServer().getOverworld();
                    RegistryKey<World> voidDimKey = RegistryKey.of(
                            RegistryKeys.WORLD, Identifier.of("void_echo", "voids_end")
                    );
                    ServerWorld targetWorld =
                            player.getServer().getWorld(voidDimKey);
                    if (targetWorld != null) {
                        // F4: Store portal position for return (in OVERWORLD PersistentState,
                        // since return is read from overworld in the return path)
                        PortalStorage.get(overworld).setReturnPosition(player.getUuid(), pos);

                        // Consume the key on successful teleport
                        if (!player.getAbilities().creativeMode) {
                            held.decrement(1);
                        }

                        // F3: Safe position lookup
                        BlockPos safePos = targetWorld.getTopPosition(
                                Heightmap.Type.MOTION_BLOCKING,
                                new BlockPos((int) player.getX(), 0, (int) player.getZ())
                        );
                        Vec3d targetPos = new Vec3d(
                                safePos.getX() + 0.5,
                                safePos.getY() + 1.0,
                                safePos.getZ() + 0.5
                        );

                        player.teleportTo(new TeleportTarget(
                                targetWorld,
                                targetPos,
                                player.getVelocity(),
                                player.getYaw(),
                                player.getPitch(),
                                TeleportTarget.NO_OP
                        ));
                        player.sendMessage(Text.translatable("message.void_echo.portal_activated"), true);

                        // Ensure a return portal exists near the destination;
                        // scan offsets if the primary position is obstructed.
                        int[][] portalOffsets = {{5,0}, {-5,0}, {0,5}, {0,-5}, {3,3}, {-3,3}, {3,-3}, {-3,-3}, {7,0}, {0,7}};
                        boolean portalPlaced = false;
                        for (int[] off : portalOffsets) {
                            BlockPos candidate = safePos.add(off[0], 0, off[1]);
                            if (targetWorld.getBlockState(candidate).isAir()) {
                                targetWorld.setBlockState(candidate, ModBlocks.VOID_PORTAL_FRAME.getDefaultState(), 3);
                                portalPlaced = true;
                                break;
                            }
                        }
                        if (!portalPlaced) {
                            VoidEcho.LOGGER.warn("Could not place return portal near {}", safePos);
                        }
                    } else {
                        VoidEcho.LOGGER.error("Void dimension 'voids_end' not found for teleport");
                    }
                }
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
