package mod.tubes;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
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

    public static final Supplier<BlockEntityType<PneumaticTubeBlockEntity>> PNEUMATIC_TUBE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "pneumatic_tube_block_entity",
            () -> new BlockEntityType<>(
                    PneumaticTubeBlockEntity::new,
                    false,
                    PNEUMATIC_TUBE_BLOCK.get()
            )
    );

    public PneumaticTubes(IEventBus modEventBus, ModContainer modContainer) {

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
