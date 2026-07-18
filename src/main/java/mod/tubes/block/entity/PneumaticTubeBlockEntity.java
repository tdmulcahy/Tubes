package mod.tubes.block.entity;

import mod.tubes.ContainerUtils;
import mod.tubes.tube.PneumaticTubeItem;
import mod.tubes.PneumaticTubes;
import mod.tubes.payload.PneumaticItemAddPayload;
import mod.tubes.payload.PneumaticItemRemovePayload;
import mod.tubes.payload.PneumaticItemSyncPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class PneumaticTubeBlockEntity extends BlockEntity {

  private final Set<PneumaticTubeItem> items;

  public PneumaticTubeBlockEntity(BlockPos pos, BlockState state) {
    super(PneumaticTubes.PNEUMATIC_TUBE_BLOCK_ENTITY.get(), pos, state);

    this.items = new HashSet<>();
  }

  public void addTubeItem(PneumaticTubeItem tubeItem) {

    if (level != null && level.isClientSide()) return;

    items.add(tubeItem);

    sendAddTubeItem(tubeItem);

    setChanged();
  }

  public void addTubeItemAndReset(PneumaticTubeItem tubeItem) {

    if (level != null && level.isClientSide()) return;

    tubeItem.resetProgress();

    addTubeItem(tubeItem);
  }

  public void addTubeItemClient(PneumaticTubeItem tubeItem) {

    if (level != null && !level.isClientSide()) return;

    items.add(tubeItem);
  }

  public void removeTubeItemClient(PneumaticTubeItem tubeItem) {

    if (level != null && !level.isClientSide()) return;

    items.remove(tubeItem);
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

  private Optional<BlockEntity> getBlockEntityForDirection(Direction direction) {

    if (level == null) {
      return Optional.empty();
    }

    BlockPos offsetPos = getBlockPos().relative(direction);
    return Optional.ofNullable(level.getBlockEntity(offsetPos));
  }

  private void sendSyncTubeItem(PneumaticTubeItem tubeItem) {

    PacketDistributor.sendToAllPlayers(
            new PneumaticItemSyncPayload(
                    getBlockPos().getX(),
                    getBlockPos().getY(),
                    getBlockPos().getZ(),
                    tubeItem.getId(),
                    tubeItem.getProgress(),
                    tubeItem.getCurrentDirection().ordinal()
            )
    );
  }

  private void sendAddTubeItem(PneumaticTubeItem tubeItem) {

    PacketDistributor.sendToAllPlayers(
            new PneumaticItemAddPayload(
                    getBlockPos().getX(),
                    getBlockPos().getY(),
                    getBlockPos().getZ(),
                    tubeItem.getId(),
                    tubeItem.getStack(),
                    tubeItem.getProgress(),
                    tubeItem.getCurrentDirection().ordinal()
            )
    );
  }

  private void sendRemoveTubeItem(PneumaticTubeItem tubeItem) {

    if (level != null && level.isClientSide()) return;

    PacketDistributor.sendToPlayersNear(
            (ServerLevel) level,
            null,
            getBlockPos().getX(),
            getBlockPos().getY(),
            getBlockPos().getZ(),
            64,
            new PneumaticItemRemovePayload(
                    getBlockPos().getX(),
                    getBlockPos().getY(),
                    getBlockPos().getZ(),
                    tubeItem.getId()
            )
    );
  }

  private void handleItemHalfway(List<Direction> availableDirections, PneumaticTubeItem tubeItem) {

    // Remove the opposite direction if possible.
    if (availableDirections.size() > 1) {
      availableDirections.remove(tubeItem.getCurrentDirection().getOpposite());
    }

    Direction nextDirection = availableDirections.getFirst();

    // TODO: Combine once more complicated routing it available.
    tubeItem.setCurrentDirection(nextDirection);
    tubeItem.setHasPassedCenter(true);

    // sendSyncTubeItem(tubeItem);
  }

  private boolean handleItemReachedEnd(PneumaticTubeItem tubeItem) {

    Optional<BlockEntity> nextBlockEntity = getBlockEntityForDirection(tubeItem.getCurrentDirection());
    if (nextBlockEntity.isPresent()) {

      if (nextBlockEntity.get() instanceof PneumaticTubeBlockEntity tubeBlockEntity) {
        tubeBlockEntity.addTubeItemAndReset(tubeItem);
      }

      if (nextBlockEntity.get() instanceof Container container) {
        return ContainerUtils.insertStackIntoContainer(container, tubeItem.getStack());
      }
    }

    return true;
  }

  private void updateClient() {

    List<Direction> availableDirections = getAvailableDirections();

    // Don't update items with nowhere to go.
    if (availableDirections.isEmpty()) return;

    for (PneumaticTubeItem tubeItem : items) {
      tubeItem.update();

      // Halfway point, decide on direction.
      if (tubeItem.hasReachedHalfway() && !tubeItem.hasPassedCenter()) {
        handleItemHalfway(availableDirections, tubeItem);
      }
    }
  }

  private void update() {

    List<Direction> availableDirections = getAvailableDirections();

    // Don't update items with nowhere to go.
    if (availableDirections.isEmpty()) return;

    Iterator<PneumaticTubeItem> iterator = items.iterator();
    while (iterator.hasNext()) {
      PneumaticTubeItem tubeItem = iterator.next();
      tubeItem.update();

      if (level != null && level.isClientSide()) continue;

      // Halfway point, decide on direction.
      if (tubeItem.hasReachedHalfway() && !tubeItem.hasPassedCenter()) {
        handleItemHalfway(availableDirections, tubeItem);
      }

      // Item has reached the end of the tube.
      if (tubeItem.hasReachedEnd()) {
        if (handleItemReachedEnd(tubeItem)) {

          sendRemoveTubeItem(tubeItem);

          iterator.remove();

          setChanged();
        } else {

          // If the item wasn't removed then send it back.
          tubeItem.setCurrentDirection(tubeItem.getCurrentDirection().getOpposite());
          tubeItem.resetProgress();

          sendSyncTubeItem(tubeItem);
        }
      }
    }
  }

  // Read values from the passed ValueInput here.
  @Override
  public void loadAdditional(ValueInput input) {
    super.loadAdditional(input);

    for (ValueInput childInput : input.childrenListOrEmpty("Items")) {

      PneumaticTubeItem tubeItem = new PneumaticTubeItem(ItemStack.EMPTY);
      childInput.readChild("Item", tubeItem);

      if (!tubeItem.getStack().isEmpty()) {
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
    if (t instanceof PneumaticTubeBlockEntity be) {

      if (level.isClientSide()) {
        be.updateClient();
      } else {
        be.update();
      }
    }
  }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    return this.saveWithoutMetadata(registries);
  }

  @Override
  public Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public void onDataPacket(Connection connection, ValueInput input) {
    super.onDataPacket(connection, input);
  }
}
