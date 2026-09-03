package nadiendev.ultimatetransport.tube;

import java.util.List;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import nadiendev.ultimatetransport.registry.UTBlocks;
import nadiendev.ultimatetransport.registry.UTItems;
import nadiendev.ultimatetransport.tube.block.BlockStation;
import nadiendev.ultimatetransport.tube.block.BlockStationHorizontal;
import nadiendev.ultimatetransport.tube.block.BlockTube;
import nadiendev.ultimatetransport.tube.item.ItemStation;
import nadiendev.ultimatetransport.tube.item.ItemTube;

public final class TubeRegistration {

    private static BlockBehaviour.Properties stone() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(5.0F)
                .noOcclusion()
                .dynamicShape();
    }

    public static final DeferredBlock<BlockTube> TUBE =
            UTBlocks.BLOCKS.registerBlock("tube", BlockTube::new, stone());
    public static final DeferredBlock<BlockStation> STATION =
            UTBlocks.BLOCKS.registerBlock("station", BlockStation::new, stone());
    public static final DeferredBlock<BlockStationHorizontal> STATION_HORIZONTAL =
            UTBlocks.BLOCKS.registerBlock("station_horizontal", BlockStationHorizontal::new, stone().noLootTable());

    public static final DeferredItem<ItemTube> TUBE_ITEM = UTItems.ITEMS.registerItem("tube",
            p -> new ItemTube(TUBE.get(), p, ItemTube.UNDIRECTED), new Item.Properties());

    public static final DeferredItem<ItemTube> TUBE_DOWN = directed("tube_down", 0);
    public static final DeferredItem<ItemTube> TUBE_UP = directed("tube_up", 1);
    public static final DeferredItem<ItemTube> TUBE_NORTH = directed("tube_north", 2);
    public static final DeferredItem<ItemTube> TUBE_SOUTH = directed("tube_south", 3);
    public static final DeferredItem<ItemTube> TUBE_EAST = directed("tube_east", 4);
    public static final DeferredItem<ItemTube> TUBE_WEST = directed("tube_west", 5);

    public static final List<DeferredItem<ItemTube>> DIRECTED_TUBES =
            List.of(TUBE_DOWN, TUBE_UP, TUBE_NORTH, TUBE_SOUTH, TUBE_EAST, TUBE_WEST);

    public static final DeferredItem<ItemStation> STATION_ITEM = UTItems.ITEMS.registerItem("station",
            p -> new ItemStation(STATION.get(), p), new Item.Properties());

    private static DeferredItem<ItemTube> directed(String name, int direction) {
        return UTItems.ITEMS.registerItem(name, p -> new ItemTube(TUBE.get(), p, direction), new Item.Properties());
    }

    private TubeRegistration() {
    }

    public static void touch() {
    }
}
