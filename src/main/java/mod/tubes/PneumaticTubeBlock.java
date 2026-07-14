package mod.tubes;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PneumaticTubeBlock extends Block implements EntityBlock {

  private static final BooleanProperty UP = BooleanProperty.create("up");
  private static final BooleanProperty DOWN = BooleanProperty.create("down");
  private static final BooleanProperty NORTH = BooleanProperty.create("north");
  private static final BooleanProperty SOUTH = BooleanProperty.create("south");
  private static final BooleanProperty EAST = BooleanProperty.create("east");
  private static final BooleanProperty WEST = BooleanProperty.create("west");

  public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = new HashMap<>() {{
    put(Direction.UP, UP);
    put(Direction.DOWN, DOWN);
    put(Direction.NORTH, NORTH);
    put(Direction.SOUTH, SOUTH);
    put(Direction.EAST, EAST);
    put(Direction.WEST, WEST);
  }};

  private final VoxelShape[] shapes;

  public PneumaticTubeBlock(Identifier registryName) {
    super(
            Properties.of().setId(ResourceKey.create(Registries.BLOCK, registryName)).noOcclusion()
    );

    this.registerDefaultState(
            this.getStateDefinition().any()
                    .setValue(UP, false)
                    .setValue(DOWN, false)
                    .setValue(NORTH, false)
                    .setValue(SOUTH, false)
                    .setValue(EAST, false)
                    .setValue(WEST, false)
    );
    this.shapes = initShapes();
  }

  @Override
  protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (blockEntity instanceof PneumaticTubeBlockEntity pneumaticTubeBlockEntity) {

      pneumaticTubeBlockEntity.addTubeItem(new PneumaticTubeItem(itemStack.copy(), 0.5F));
    }

    return InteractionResult.PASS;
  }

  private VoxelShape[] initShapes() {
    float pixelWidth = 1.0f / 16.0f;
    float min = pixelWidth * 4;
    float max = pixelWidth * 12;

    VoxelShape[] result = new VoxelShape[7];
    VoxelShape baseShape = Shapes.box(min, min, min, max, max, max);
    result[0] = baseShape;

    for (Direction direction : Direction.values()) {

      float minX = Math.min(min, direction.getStepX() + 1);
      float minY = Math.min(min, direction.getStepY() + 1);
      float minZ = Math.min(min, direction.getStepZ() + 1);
      float maxX = Math.max(max, direction.getStepX());
      float maxY = Math.max(max, direction.getStepY());
      float maxZ = Math.max(max, direction.getStepZ());

      VoxelShape directionalShape = Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
      result[direction.ordinal() + 1] = directionalShape;
    }

    return result;
  }

  private BlockState getConnectedBlockState(Level level, BlockPos pos) {
    BlockState state = this.defaultBlockState();

    for (Direction direction : Direction.values()) {
      BlockPos offsetPos = pos.relative(direction);

      BlockEntity offsetBlockEntity = level.getBlockEntity(offsetPos);

      if (offsetBlockEntity instanceof PneumaticTubeBlockEntity || offsetBlockEntity instanceof Container) {
        state = state.setValue(PROPERTY_BY_DIRECTION.get(direction), Boolean.TRUE);
      } else {
        state.setValue(PROPERTY_BY_DIRECTION.get(direction), Boolean.FALSE);
      }
    }

    return state;
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {

    VoxelShape shape = shapes[0];

    for (Direction direction : Direction.values()) {
      if (state.getValue(PROPERTY_BY_DIRECTION.get(direction))) {
        shape = Shapes.or(shape, shapes[direction.ordinal() + 1]);
      }
    }

    return shape;
  }

  @Override
  protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @org.jspecify.annotations.Nullable Orientation orientation, boolean movedByPiston) {
    super.neighborChanged(state, level, pos, block, orientation, movedByPiston);

    BlockState newState = getConnectedBlockState(level, pos);
    level.setBlock(pos, newState, 2);
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    Level level = context.getLevel();
    BlockPos pos = context.getClickedPos();

    return getConnectedBlockState(level, pos);
  }

  @Override
  public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest, FluidState fluid) {

    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (blockEntity instanceof PneumaticTubeBlockEntity pneumaticTubeBlockEntity) {

      for (PneumaticTubeItem pneumaticTubeItem : pneumaticTubeBlockEntity.getTubeItems()) {
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), pneumaticTubeItem.getStack());
      }
    }

    return super.onDestroyedByPlayer(state, level, pos, player, toolStack, willHarvest, fluid);
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new PneumaticTubeBlockEntity(pos, state);
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
    return type == PneumaticTubes.PNEUMATIC_TUBE_BLOCK_ENTITY.get() ? PneumaticTubeBlockEntity::tick : null;
  }
}
