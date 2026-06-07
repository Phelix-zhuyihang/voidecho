package com.example.voidecho.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CrystalOreBlock extends Block {
    public CrystalOreBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!world.isClient && entity instanceof PlayerEntity player) {
            StatusEffectInstance existing = player.getStatusEffect(StatusEffects.GLOWING);
            if (existing == null || existing.getDuration() <= 30) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.GLOWING, 60, 0, true, false, true
                ));
            }
        }
    }
}
