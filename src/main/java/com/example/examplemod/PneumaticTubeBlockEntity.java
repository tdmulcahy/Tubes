package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.*;

public class PneumaticTubeBlockEntity extends BlockEntity {

  private final Set<PneumaticTubeItem> items;

  public PneumaticTubeBlockEntity(BlockPos pos, BlockState state) {
    super(PneumaticTubes.PNEUMATIC_TUBE_BLOCK_ENTITY.get(), pos, state);

    this.items = new HashSet<>();
  }

  public void addTubeItem(PneumaticTubeItem pneumaticTubeItem) {
    items.add(pneumaticTubeItem);

    setChanged();
  }

  private void transferToNextTube(PneumaticTubeItem pneumaticTubeItem) {

    pneumaticTubeItem.resetProgress();

    items.add(pneumaticTubeItem);

    setChanged();
  }

  private List<Direction> getAvailableDirections() {

    List<Direction> directions = new ArrayList<>();
    for (Direction direction : Direction.values()) {
      BlockPos offsetPos = getBlockPos().relative(direction);

      if (level == null) continue;

      BlockEntity offsetBlockEntity = level.getBlockEntity(offsetPos);

      if (offsetBlockEntity instanceof PneumaticTubeBlockEntity || offsetBlockEntity instanceof Container) {
        directions.add(direction);
      }
    }
    return directions;
  }

  private void update() {

    List<Direction> availableDirections = getAvailableDirections();

    // Don't update items with nowhere to go.
    if (availableDirections.isEmpty()) return;

    Iterator<PneumaticTubeItem> iterator = items.iterator();
    while (iterator.hasNext()) {
      PneumaticTubeItem item = iterator.next();
      item.update();

      // Halfway point, decide on direction.
      if (item.getProgress() >= 0.5f && !item.hasPassedCenter()) {

        // Remove the opposite direction if possible.
        if (availableDirections.size() > 1) {
          availableDirections.remove(item.getCurrentDirection().getOpposite());
        }

        Direction nextDirection = availableDirections.getFirst();

        // TODO: Combine once more complicated routing it available.
        item.setCurrentDirection(nextDirection);
        item.setHasPassedCenter(true);
      }

      // Item has come to end of tube, move to next tube.
      if (item.getProgress() >= 1.0f) {

        if (level == null) continue;

        BlockPos offsetPos = getBlockPos().relative(item.getCurrentDirection());
        BlockEntity blockEntity = level.getBlockEntity(offsetPos);

        if (blockEntity instanceof PneumaticTubeBlockEntity pneumaticTubeBlockEntity) {
          pneumaticTubeBlockEntity.transferToNextTube(item);

          iterator.remove(); // Remove current item.
          setChanged();
        }

        if (blockEntity instanceof Container container) {
          int slotCount = container.getContainerSize();

          // Place ItemStack in the first available slot in the container.
          for (int i = 0; i < slotCount; i++) {
            if (container.canPlaceItem(i, item.getStack())) {
              if (insertIntoContainer(container, item.getStack())) {
                iterator.remove();
                setChanged();
                break;
              }
            }
          }
        }
      }
    }
  }

  // TODO: Insert partial stack and leave remaining in tube.
  private boolean insertIntoContainer(Container container, ItemStack itemStack) {

    int slots = container.getContainerSize();
    for (int slot = 0; slot < slots; slot++) {

      if (!container.canPlaceItem(slot, itemStack)) continue;

      ItemStack current = container.getItem(slot);
      if (current.isEmpty()) {
        container.setItem(slot, itemStack);

        return true;
      } else {
        if (ItemStack.isSameItemSameComponents(current, itemStack)) {
          if (current.count() + itemStack.count() <= current.getMaxStackSize()) {
            current.grow(itemStack.count());

            return true;
          }
        }
      }
    }

    return false;
  }

  // Read values from the passed ValueInput here.
  @Override
  public void loadAdditional(ValueInput input) {
    super.loadAdditional(input);

    for (ValueInput childInput : input.childrenListOrEmpty("Items")) {

      PneumaticTubeItem tubeItem = null;
      childInput.readChild("Item", tubeItem);

      if (tubeItem != null) {
        items.add(tubeItem);
      }
    }
  }

  // Save values into the passed ValueOutput here.
  @Override
  public void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);

    ValueOutput.ValueOutputList list = output.childrenList("Items");

    for (PneumaticTubeItem pneumaticTubeItem : items) {
      ValueOutput childOutput = list.addChild();
      childOutput.putChild("Item", pneumaticTubeItem);
    }
  }

  public Set<PneumaticTubeItem> getTubeItems() {
    return items;
  }

  public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
    if (t instanceof PneumaticTubeBlockEntity be) be.update();
  }

  // Create an update tag here, like above.
  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    return this.saveWithoutMetadata(registries);
  }

  // Return our packet here. This method returning a non-null result tells the game to use this packet for syncing.
  @Override
  public Packet<ClientGamePacketListener> getUpdatePacket() {
    // The packet uses the CompoundTag returned by #getUpdateTag. An alternative overload of #create exists
    // that allows you to specify a custom update tag, including the ability to omit data the client might not need.
    return ClientboundBlockEntityDataPacket.create(this);
  }

  // Optionally: Run some custom logic when the packet is received.
  // The super/default implementation forwards to #loadWithComponents.
  @Override
  public void onDataPacket(Connection connection, ValueInput input) {
    super.onDataPacket(connection, input);
    // Do whatever you need to do here.
  }
}
