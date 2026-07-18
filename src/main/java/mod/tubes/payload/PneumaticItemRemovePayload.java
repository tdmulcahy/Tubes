package mod.tubes.payload;

import io.netty.buffer.ByteBuf;
import mod.tubes.block.entity.PneumaticTubeBlockEntity;
import mod.tubes.tube.PneumaticTubeItem;
import mod.tubes.PneumaticTubes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.util.Optional;

public class PneumaticItemRemovePayload implements CustomPacketPayload {

  public static final Type<PneumaticItemRemovePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PneumaticTubes.MODID, "pneumatic_tube_item_remove_payload"));

  public static final StreamCodec<ByteBuf, PneumaticItemRemovePayload> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_INT, PneumaticItemRemovePayload::getX,
          ByteBufCodecs.VAR_INT, PneumaticItemRemovePayload::getY,
          ByteBufCodecs.VAR_INT, PneumaticItemRemovePayload::getZ,
          ByteBufCodecs.LONG, PneumaticItemRemovePayload::getId,

          PneumaticItemRemovePayload::new
  );

  private final int x;
  private final int y;
  private final int z;

  private final long id;

  public PneumaticItemRemovePayload(int x, int y, int z, long id) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.id = id;
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

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static class PneumaticItemRemovePayloadClientHandler {

    public static void handleDataOnMain(final PneumaticItemRemovePayload data, final IPayloadContext context) {

      context.enqueueWork(() -> {

        BlockPos pos = new BlockPos(data.getX(), data.getY(), data.getZ());

        try (Level level = context.player().level()) {

          if (level.getBlockEntity(pos) instanceof PneumaticTubeBlockEntity blockEntity) {

            Optional<PneumaticTubeItem> item = blockEntity.getTubeItems().stream().filter(ti -> ti.getId() == data.getId()).findFirst();

            item.ifPresent(blockEntity::removeTubeItemClient);
          }
        } catch (IOException e) {

        }
      });
    }
  }

  public static class PneumaticItemRemovePayloadServerHandler {

    public static void handleDataOnNetwork(final PneumaticItemRemovePayload data, final IPayloadContext context) {

    }
  }
}
