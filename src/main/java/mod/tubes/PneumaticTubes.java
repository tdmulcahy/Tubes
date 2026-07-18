package mod.tubes;

import mod.tubes.block.EjectorBlock;
import mod.tubes.block.PneumaticTubeBlock;
import mod.tubes.block.entity.EjectorBlockEntity;
import mod.tubes.block.entity.PneumaticTubeBlockEntity;
import mod.tubes.payload.PneumaticItemAddPayload;
import mod.tubes.payload.PneumaticItemRemovePayload;
import mod.tubes.payload.PneumaticItemSyncPayload;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod(PneumaticTubes.MODID)
public class PneumaticTubes {

  public static final String MODID = "tubes";

  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

  public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PneumaticTubes.MODID);

  public static final DeferredBlock<Block> PNEUMATIC_TUBE_BLOCK = BLOCKS.register("pneumatic_tube", PneumaticTubeBlock::new);
  public static final DeferredItem<BlockItem> PNEUMATIC_TUBE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("pneumatic_tube", PNEUMATIC_TUBE_BLOCK);

  public static final DeferredBlock<Block> EJECTOR_BLOCK = BLOCKS.register("ejector", EjectorBlock::new);
  public static final DeferredItem<BlockItem> EJECTOR_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("ejector", EJECTOR_BLOCK);

  public static final Supplier<BlockEntityType<PneumaticTubeBlockEntity>> PNEUMATIC_TUBE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
          "pneumatic_tube_block_entity",
          () -> new BlockEntityType<>(
                  PneumaticTubeBlockEntity::new,
                  false,
                  PNEUMATIC_TUBE_BLOCK.get()
          )
  );

  public static final Supplier<BlockEntityType<EjectorBlockEntity>> EJECTOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
          "ejector_block_entity",
          () -> new BlockEntityType<>(
                  EjectorBlockEntity::new,
                  false,
                  EJECTOR_BLOCK.get()
          )
  );

  public PneumaticTubes(IEventBus modEventBus, ModContainer modContainer) {

    modEventBus.register(this);

    BLOCKS.register(modEventBus);
    ITEMS.register(modEventBus);
    BLOCK_ENTITY_TYPES.register(modEventBus);
  }

  @SubscribeEvent // on the mod event bus
  public void register(RegisterPayloadHandlersEvent event) {
    final PayloadRegistrar registrar = event.registrar("1").executesOn(HandlerThread.NETWORK); // All subsequent payloads will register on the network thread

    registrar.playBidirectional(
            PneumaticItemSyncPayload.TYPE,
            PneumaticItemSyncPayload.STREAM_CODEC,
            PneumaticItemSyncPayload.PneumaticItemSyncPayloadServerHandler::handleDataOnNetwork
    );

    registrar.playBidirectional(
            PneumaticItemAddPayload.TYPE,
            PneumaticItemAddPayload.STREAM_CODEC,
            PneumaticItemAddPayload.PneumaticItemAddPayloadServerHandler::handleDataOnNetwork
    );

    registrar.playBidirectional(
            PneumaticItemRemovePayload.TYPE,
            PneumaticItemRemovePayload.STREAM_CODEC,
            PneumaticItemRemovePayload.PneumaticItemRemovePayloadServerHandler::handleDataOnNetwork
    );
  }
}
