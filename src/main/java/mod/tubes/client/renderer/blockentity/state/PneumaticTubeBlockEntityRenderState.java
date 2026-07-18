package mod.tubes.client.renderer.blockentity.state;

import mod.tubes.tube.PneumaticTubeItem;
import mod.tubes.block.entity.PneumaticTubeBlockEntity;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

import java.util.ArrayList;
import java.util.List;

public class PneumaticTubeBlockEntityRenderState extends BlockEntityRenderState {

  public static class TubeItemStackRenderState extends ItemStackRenderState {

    private final PneumaticTubeItem pneumaticTubeItem;

    public TubeItemStackRenderState(PneumaticTubeItem pneumaticTubeItem) {
      this.pneumaticTubeItem = pneumaticTubeItem;
    }

    public PneumaticTubeItem getTubeItem() {
      return pneumaticTubeItem;
    }
  }

  private float partialTicks;
  private PneumaticTubeBlockEntity blockEntity;

  private final List<TubeItemStackRenderState> itemStackRenderStateList;

  public PneumaticTubeBlockEntityRenderState() {
    itemStackRenderStateList = new ArrayList<>();
  }

  public void addItemRenderState(TubeItemStackRenderState renderState) {
    itemStackRenderStateList.add(renderState);
  }

  public void setPartialTicks(float partialTicks) {
    this.partialTicks = partialTicks;
  }

  public void setBlockEntity(PneumaticTubeBlockEntity blockEntity) {
    this.blockEntity = blockEntity;
  }

  public float getPartialTicks() {
    return partialTicks;
  }

  public PneumaticTubeBlockEntity getBlockEntity() {
    return blockEntity;
  }

  public List<TubeItemStackRenderState> getItemStackRenderStateList() {
    return itemStackRenderStateList;
  }
}
