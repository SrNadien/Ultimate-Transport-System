package nadiendev.ultimatetransport.registry;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.CableBlock;
import nadiendev.ultimatetransport.cell.EnergyCellBlock;
import nadiendev.ultimatetransport.generator.GeneratorBlock;
import nadiendev.ultimatetransport.generator.GeneratorType;
import nadiendev.ultimatetransport.compat.ModIds;
import org.jetbrains.annotations.Nullable;
import nadiendev.ultimatetransport.cell.EnergyCellTier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class UTBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(UltimateTransport.MODID);

    private static final Map<TransferType, DeferredBlock<CableBlock>> CABLE_BY_TYPE = new EnumMap<>(TransferType.class);
    private static final List<DeferredBlock<CableBlock>> CABLES = new ArrayList<>();

    public static final DeferredBlock<CableBlock> ENERGY_CABLE = cable(TransferType.ENERGY);
    public static final DeferredBlock<CableBlock> FLUID_CABLE = cable(TransferType.FLUID);
    public static final DeferredBlock<CableBlock> ITEM_CABLE = cable(TransferType.ITEM);

    /**
     * The gas cable exists only where there are gases. With Mekanism absent it is never registered,
     * so it cannot be seen, given or crafted; data generation still registers it so its recipe, model
     * and translations ship in the jar and come alive the moment Mekanism is installed.
     */
    @Nullable
    public static final DeferredBlock<CableBlock> GAS_CABLE =
            ModIds.installedAtLoad(ModIds.MEKANISM) ? cable(TransferType.GAS) : null;

    @Nullable
    public static final DeferredBlock<CableBlock> SOURCE_CABLE =
            ModIds.installedAtLoad(ModIds.ARS_NOUVEAU) ? cable(TransferType.SOURCE) : null;

    public static final DeferredBlock<CableBlock> UNIVERSAL_CABLE = cable(TransferType.UNIVERSAL);

    private static final List<DeferredBlock<EnergyCellBlock>> CELLS = new ArrayList<>();

    static {
        for (EnergyCellTier tier : EnergyCellTier.VALUES) {
            CELLS.add(BLOCKS.registerBlock(tier.blockName(),
                    properties -> new EnergyCellBlock(tier, properties),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GRAY)
                            .strength(3.0F, 12.0F)
                            .sound(SoundType.METAL)
                            .lightLevel(state -> state.getValue(EnergyCellBlock.CHARGE) * 12 / EnergyCellBlock.STEPS)
                            .requiresCorrectToolForDrops()));
        }
    }

    private static final List<DeferredBlock<GeneratorBlock>> GENERATORS = new ArrayList<>();

    static {
        for (GeneratorType type : GeneratorType.VALUES) {
            GENERATORS.add(BLOCKS.registerBlock(type.blockName(),
                    properties -> new GeneratorBlock(type, properties),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GRAY)
                            .strength(3.0F, 9.0F)
                            .sound(SoundType.METAL)
                            .lightLevel(state -> state.getValue(GeneratorBlock.LIT) ? 10 : 0)
                            .requiresCorrectToolForDrops()));
        }
    }

    private UTBlocks() {
    }

    public static List<DeferredBlock<GeneratorBlock>> generators() {
        return GENERATORS;
    }

    public static Block[] generatorBlocks() {
        return GENERATORS.stream().map(DeferredBlock::get).toArray(Block[]::new);
    }

    public static List<DeferredBlock<EnergyCellBlock>> cells() {
        return CELLS;
    }

    public static Block[] cellBlocks() {
        return CELLS.stream().map(DeferredBlock::get).toArray(Block[]::new);
    }

    private static DeferredBlock<CableBlock> cable(TransferType type) {
        DeferredBlock<CableBlock> block = BLOCKS.registerBlock(type.cableName(),
                properties -> new CableBlock(type, properties),
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(0.4F)
                        .sound(SoundType.COPPER)
                        .noOcclusion()
                        .dynamicShape()
                        // Cables build their own drop list, so that an optional cable does not need a
                        // loot table pointing at an item that may not be registered.
                        .noLootTable());
        CABLE_BY_TYPE.put(type, block);
        CABLES.add(block);
        return block;
    }

    public static List<DeferredBlock<CableBlock>> cables() {
        return CABLES;
    }

    public static DeferredBlock<CableBlock> cableFor(TransferType type) {
        return CABLE_BY_TYPE.get(type);
    }

    public static Block[] cableBlocks() {
        return CABLES.stream().map(DeferredBlock::get).toArray(Block[]::new);
    }
}
