package mod.tubes.block;

import mod.tubes.block.entity.EjectorBlockEntity;
import mod.tubes.block.entity.PneumaticTubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class EjectorBlock extends Block implements EntityBlock {

  private static final EnumProperty<Direction> DIRECTION = EnumProperty.create("direction", Direction.class);
  private static final BooleanProperty POWERED = BooleanProperty.create("powered");

  public EjectorBlock(Identifier registryName) {
    super(
            Properties.of().setId(ResourceKey.create(Registries.BLOCK, registryName))
    );

    this.registerDefaultState(this.getStateDefinition().any().setValue(DIRECTION, Direction.NORTH).setValue(POWERED, false));
  }

  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return this.defaultBlockState().setValue(DIRECTION, context.getClickedFace().getOpposite());
  }

  protected BlockState rotate(BlockState state, Rotation rotation) {
    return state.setValue(DIRECTION, rotation.rotate(state.getValue(DIRECTION)));
  }

  protected BlockState mirror(BlockState state, Mirror mirror) {
    return state.rotate(mirror.getRotation(state.getValue(DIRECTION)));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(DIRECTION, POWERED);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new EjectorBlockEntity(pos, state);
  }
}
