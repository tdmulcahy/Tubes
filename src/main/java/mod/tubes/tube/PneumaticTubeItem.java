package mod.tubes.tube;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.Optional;

//TODO: Do we need to sync to the client?
public class PneumaticTubeItem implements ValueIOSerializable {

  private static long NEXT_ID = 0;

  private long id;

  private ItemStack stack;

  private float progress;
  private float lastProgress;

  private boolean hasPassedCenter;

  private Direction currentDirection;
  private DyeColor color;

  public PneumaticTubeItem(ItemStack itemStack) {
    this(NEXT_ID++, itemStack);
  }

  public PneumaticTubeItem(long id,ItemStack stack) {
    this.id = id;
    this.stack = stack;

    hasPassedCenter = false;
    currentDirection = Direction.UP;
  }

  public void update() {
    lastProgress = progress;
    progress += 0.02F;
  }

  public void resetProgress() {
    lastProgress = 0;
    progress = 0;

    hasPassedCenter = false;
  }

  @Override
  public void serialize(ValueOutput output) {
    output.putLong("Id", id);
    output.putFloat("Progress", progress);
    output.store("Direction", Direction.CODEC, currentDirection);
    output.putBoolean("HasPassedCenter", hasPassedCenter);

    NonNullList<ItemStack> items = NonNullList.create();
    items.add(stack);

    ContainerHelper.saveAllItems(output, items);
  }

  @Override
  public void deserialize(ValueInput input) {
    id = input.getLongOr("Id", -1);

    if (NEXT_ID < id) NEXT_ID = id++;

    progress = input.getFloatOr("Progress", 0f);
    Optional<Direction> direction = input.read("Direction", Direction.CODEC);
    direction.ifPresent(value -> currentDirection = value);
    hasPassedCenter = input.getBooleanOr("HasPassedCenter", false);

    NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);
    ContainerHelper.loadAllItems(input, stacks);

    if (!stacks.isEmpty())
      stack = stacks.getFirst();
  }

  public long getId() {
    return id;
  }

  public boolean hasReachedHalfway() {
    return progress >= 0.5;
  }

  public boolean hasReachedEnd() {
    return progress >= 1.0;
  }

  public void setHasPassedCenter(boolean hasPassedCenter) {
    this.hasPassedCenter = hasPassedCenter;
  }

  public void setCurrentDirection(Direction currentDirection) {
    this.currentDirection = currentDirection;
  }

  public void setProgress(float progress) {
    this.progress = progress;
  }

  public float getProgress() {
    return progress;
  }

  public float getLastProgress() {
    return lastProgress;
  }

  public ItemStack getStack() {
    return stack;
  }

  public boolean hasPassedCenter() {
    return hasPassedCenter;
  }

  public Direction getCurrentDirection() {
    return currentDirection;
  }
}
