package mod.tubes.payload;

import io.netty.buffer.ByteBuf;
import mod.tubes.block.entity.PneumaticTubeBlockEntity;
import mod.tubes.tube.PneumaticTubeItem;
import mod.tubes.PneumaticTubes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.util.Optional;

public class PneumaticItemAddPayload implements CustomPacketPayload {

  public static final Type<PneumaticItemAddPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PneumaticTubes.MODID, "pneumatic_tube_item_add_payload"));

  public static final StreamCodec<ByteBuf, PneumaticItemAddPayload> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_INT, PneumaticItemAddPayload::getX,
          ByteBufCodecs.VAR_INT, PneumaticItemAddPayload::getY,
          ByteBufCodecs.VAR_INT, PneumaticItemAddPayload::getZ,
          ByteBufCodecs.LONG, PneumaticItemAddPayload::getId,
          ByteBufCodecs.fromCodec(ItemStack.CODEC), PneumaticItemAddPayload::getItemStack,
          ByteBufCodecs.FLOAT,  PneumaticItemAddPayload::getProgress,
          ByteBufCodecs.VAR_INT, PneumaticItemAddPayload::getCurrentDirection,

          PneumaticItemAddPayload::new
  );

  private final int x;
  private final int y;
  private final int z;

  private final long id;

  private final ItemStack itemStack;

  private final float progress;

  private final int currentDirection;

  public PneumaticItemAddPayload(int x, int y, int z, long id, ItemStack itemStack, float progress, int currentDirection) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.id = id;
    this.itemStack = itemStack;
    this.progress = progress;
    this.currentDirection = currentDirection;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getZ() {
    return z;
  }

  public long getId() {
    return id;
  }

  public ItemStack getItemStack() {
    return itemStack;
  }

  public float getProgress() {
    return progress;
  }

  public int getCurrentDirection() {
    return currentDirection;
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static class PneumaticItemAddPayloadClientHandler {

    public static void handleDataOnMain(final PneumaticItemAddPayload data, final IPayloadContext context) {

      context.enqueueWork(() -> {

        BlockPos pos = new BlockPos(data.getX(), data.getY(), data.getZ());

        try (Level level = context.player().level()) {

          if (level.getBlockEntity(pos) instanceof PneumaticTubeBlockEntity blockEntity) {

            Optional<PneumaticTubeItem> item = blockEntity.getTubeItems().stream().filter(ti -> ti.getId() == data.getId()).findFirst();
            if (item.isEmpty()) {

              PneumaticTubeItem newTubeItem = new PneumaticTubeItem(data.getId(), data.getItemStack());
              newTubeItem.setProgress(data.getProgress());
              newTubeItem.setCurrentDirection(Direction.from3DDataValue(data.getCurrentDirection()));

              blockEntity.addTubeItemClient(newTubeItem);
            }
          }
        } catch (IOException e) {

        }
      });
    }
  }

  public static class PneumaticItemAddPayloadServerHandler {

    public static void handleDataOnNetwork(final PneumaticItemAddPayload data, final IPayloadContext context) {

    }
  }
}
