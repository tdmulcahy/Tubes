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
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.util.Optional;

public class PneumaticItemSyncPayload implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<PneumaticItemSyncPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(PneumaticTubes.MODID, "pneumatic_tube_item_sync_payload"));

  public static final StreamCodec<ByteBuf, PneumaticItemSyncPayload> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_INT, PneumaticItemSyncPayload::getX,
          ByteBufCodecs.VAR_INT, PneumaticItemSyncPayload::getY,
          ByteBufCodecs.VAR_INT, PneumaticItemSyncPayload::getZ,
          ByteBufCodecs.LONG, PneumaticItemSyncPayload::getId,
          ByteBufCodecs.FLOAT,  PneumaticItemSyncPayload::getProgress,
          ByteBufCodecs.VAR_INT, PneumaticItemSyncPayload::getCurrentDirection,

          PneumaticItemSyncPayload::new
  );

  private final int x;
  private final int y;
  private final int z;

  private final long id;

  private final float progress;

  private final int currentDirection;

  public PneumaticItemSyncPayload(int x, int y, int z, long id, float progress, int currentDirection) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.id = id;
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

  public static class PneumaticItemSyncPayloadClientHandler {

    public static void handleDataOnMain(final PneumaticItemSyncPayload data, final IPayloadContext context) {

      context.enqueueWork(() -> {

        BlockPos pos = new BlockPos(data.getX(), data.getY(), data.getZ());

        try (Level level = context.player().level()) {

          if (level.getBlockEntity(pos) instanceof PneumaticTubeBlockEntity blockEntity) {

            Optional<PneumaticTubeItem> item = blockEntity.getTubeItems().stream().filter(ti -> ti.getId() == data.getId()).findFirst();
            if (item.isPresent()) {
              item.get().setProgress(data.getProgress());
              item.get().setCurrentDirection(Direction.from3DDataValue(data.getCurrentDirection()));
            }
          }
        } catch (IOException e) {

        }
      });
    }
  }

  public static class PneumaticItemSyncPayloadServerHandler {

    public static void handleDataOnNetwork(final PneumaticItemSyncPayload data, final IPayloadContext context) {

    }
  }
}
