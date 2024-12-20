package slimeknights.mantle.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import slimeknights.mantle.block.entity.MantleSignBlockEntity;

public class MantleStandingSignBlock extends SignBlock {
    public MantleStandingSignBlock(Settings props, WoodType type) {
        super(props, type);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new MantleSignBlockEntity(pPos, pState);
    }
}
