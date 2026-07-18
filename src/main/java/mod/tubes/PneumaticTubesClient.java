package mod.tubes;

import mod.tubes.client.renderer.blockentity.PneumaticTubeBlockEntityRenderer;
import mod.tubes.payload.PneumaticItemAddPayload;
import mod.tubes.payload.PneumaticItemRemovePayload;
import mod.tubes.payload.PneumaticItemSyncPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@Mod(value = PneumaticTubes.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = PneumaticTubes.MODID, value = Dist.CLIENT)
public class PneumaticTubesClient {

  @SubscribeEvent
  public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(PneumaticTubes.PNEUMATIC_TUBE_BLOCK_ENTITY.get(), PneumaticTubeBlockEntityRenderer::new);
  }

  @SubscribeEvent
  public static void register(RegisterClientPayloadHandlersEvent event) {
    event.register(
            PneumaticItemSyncPayload.TYPE,
            PneumaticItemSyncPayload.PneumaticItemSyncPayloadClientHandler::handleDataOnMain
    );

    event.register(
            PneumaticItemAddPayload.TYPE,
            PneumaticItemAddPayload.PneumaticItemAddPayloadClientHandler::handleDataOnMain
    );

    event.register(
            PneumaticItemRemovePayload.TYPE,
            PneumaticItemRemovePayload.PneumaticItemRemovePayloadClientHandler::handleDataOnMain
    );
  }
}
