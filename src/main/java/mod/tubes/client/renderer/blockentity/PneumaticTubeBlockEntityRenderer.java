package mod.tubes.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.tubes.client.renderer.blockentity.state.PneumaticTubeBlockEntityRenderState;
import mod.tubes.tube.PneumaticTubeItem;
import mod.tubes.block.entity.PneumaticTubeBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class PneumaticTubeBlockEntityRenderer implements BlockEntityRenderer<PneumaticTubeBlockEntity, PneumaticTubeBlockEntityRenderState> {

  private final ItemModelResolver itemModelResolver;

  public PneumaticTubeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    this.itemModelResolver = context.itemModelResolver();
  }

  @Override
  public PneumaticTubeBlockEntityRenderState createRenderState() {
    return new PneumaticTubeBlockEntityRenderState();
  }

  @Override
  public void extractRenderState(PneumaticTubeBlockEntity blockEntity, PneumaticTubeBlockEntityRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

    state.setPartialTicks(partialTicks);
    state.setBlockEntity(blockEntity);

    for (PneumaticTubeItem pneumaticTubeItem : blockEntity.getTubeItems()) {
      PneumaticTubeBlockEntityRenderState.TubeItemStackRenderState renderState = new PneumaticTubeBlockEntityRenderState.TubeItemStackRenderState(pneumaticTubeItem);

      ItemStack itemStack = pneumaticTubeItem.getStack();

      itemModelResolver.updateForTopItem(renderState, itemStack, ItemDisplayContext.NONE, blockEntity.getLevel(), null, 0);

      state.addItemRenderState(renderState);
    }
  }

  @Override
  public void submit(PneumaticTubeBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {

    for (PneumaticTubeBlockEntityRenderState.TubeItemStackRenderState itemStackRenderState : state.getItemStackRenderStateList()) {
      poseStack.pushPose();

      Direction direction = itemStackRenderState.getTubeItem().getCurrentDirection();
      float lastProgress = itemStackRenderState.getTubeItem().getLastProgress();
      float progress = itemStackRenderState.getTubeItem().getProgress();
      float interpolatedProgress = Mth.lerp(state.getPartialTicks(), lastProgress, progress);

      float x = 0.5f;
      if (direction.getStepX() == -1) {
        x = 1.0f + (direction.getStepX() * interpolatedProgress);
      } else if (direction.getStepX() == 1) {
        x = (direction.getStepX() * interpolatedProgress);
      }

      float y = 0.5f;
      if (direction.getStepY() == -1) {
        y = 1.0f + (direction.getStepY() * interpolatedProgress);
      } else if (direction.getStepY() == 1) {
        y = (direction.getStepY() * interpolatedProgress);
      }

      float z = 0.5f;
      if (direction.getStepZ() == -1) {
        z = 1.0f + (direction.getStepZ() * interpolatedProgress);
      } else if (direction.getStepZ() == 1) {
        z = (direction.getStepZ() * interpolatedProgress);
      }

      poseStack.translate(x, y, z);
      poseStack.scale(0.3f, 0.3f, 0.3f);
      itemStackRenderState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

      poseStack.popPose();
    }
  }
}
