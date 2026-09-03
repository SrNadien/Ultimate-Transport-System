package nadiendev.ultimatetransport.datagen.providers;

import nadiendev.ultimatetransport.registry.UTBlocks;
import nadiendev.ultimatetransport.tube.TubeRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class UTBlockLoot extends BlockLootSubProvider {

    public UTBlockLoot(HolderLookup.Provider registries) {
        super(Set.<Item>of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    // Cables are absent on purpose: they build their own drop list in CableBlock#getDrops, so that
    // the optional gas cable needs no loot table naming an item that may never be registered.

    @Override
    protected void generate() {
        UTBlocks.cells().forEach(holder -> dropSelf(holder.get()));
        UTBlocks.generators().forEach(holder -> dropSelf(holder.get()));
        add(TubeRegistration.TUBE.get(), createSingleItemTable(TubeRegistration.TUBE_ITEM.get()));
        add(TubeRegistration.STATION.get(), createSingleItemTable(TubeRegistration.STATION_ITEM.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        List<Block> known = new ArrayList<>();
        known.addAll(Arrays.asList(UTBlocks.cellBlocks()));
        known.addAll(Arrays.asList(UTBlocks.generatorBlocks()));
        known.add(TubeRegistration.TUBE.get());
        known.add(TubeRegistration.STATION.get());
        return known;
    }
}
