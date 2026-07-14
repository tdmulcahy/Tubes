package mod.tubes;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = PneumaticTubes.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = PneumaticTubes.MODID, value = Dist.CLIENT)
public class PneumaticTubesClient {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(PneumaticTubes.PNEUMATIC_TUBE_BLOCK_ENTITY.get(),  PneumaticTubeBlockEntityRenderer::new);
    }
}
