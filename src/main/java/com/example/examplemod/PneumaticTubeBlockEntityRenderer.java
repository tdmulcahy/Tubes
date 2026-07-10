package com.example.examplemod;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
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
    // PneumaticTube pt = be.getPneumaticTube();

    poseStack.pushPose();
    // poseStack.translate(0.5, 0.5, 0.5);

//    for (TubeItem item : pt.getItems()) {
//      float x = Mth.lerp(partialTicks, item.getLastProgress(), item.getProgress());
//      poseStack.translate(x, 0, 0);
//      Minecraft.getInstance().getItemRenderer().renderStatic(item.getStack(), ItemTransforms.TransformType.GROUND, combinedLight, combinedOverlay, stack, bufferSource, 0);
//    }

    for (PneumaticTubeBlockEntityRenderState.TubeItemStackRenderState itemStackRenderState : state.getItemStackRenderStateList()) {
      poseStack.pushPose();

      Direction direction = itemStackRenderState.getTubeItem().getCurrentDirection();
      float lastProgress = itemStackRenderState.getTubeItem().getLastProgress();
      float progress = itemStackRenderState.getTubeItem().getProgress();
      float lerp = Mth.lerp(state.getPartialTicks(), lastProgress, progress);

      float x = 0.5f;
      if (direction.getStepX() == -1) {
        x = 1.0f + (direction.getStepX() * lerp);
      } else if (direction.getStepX() == 1) {
        x = (direction.getStepX() * lerp);
      }

      float y = 0.5f;
      if (direction.getStepY() == -1) {
        y = 1.0f + (direction.getStepY() * lerp);
      } else if (direction.getStepY() == 1) {
        y = (direction.getStepY() * lerp);
      }

      float z = 0.5f;
      if (direction.getStepZ() == -1) {
        z = 1.0f + (direction.getStepZ() * lerp);
      } else if (direction.getStepZ() == 1) {
        z = (direction.getStepZ() * lerp);
      }

      poseStack.translate(x, y, z);
      poseStack.scale(0.3f, 0.3f, 0.3f);
      itemStackRenderState.submit(poseStack, submitNodeCollector, state.lightCoords, 0, 0);

      poseStack.popPose();

    }

    poseStack.popPose();
  }
}
