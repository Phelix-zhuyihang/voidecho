package com.example.voidecho.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class VoidGrassBlock extends Block {
    public VoidGrassBlock(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }
}
