package mod.tubes.block.entity;

import mod.tubes.PneumaticTubes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EjectorBlockEntity extends BlockEntity {

  public EjectorBlockEntity(BlockPos pos, BlockState state) {
    super(PneumaticTubes.EJECTOR_BLOCK_ENTITY.get(), pos, state);
  }


}
