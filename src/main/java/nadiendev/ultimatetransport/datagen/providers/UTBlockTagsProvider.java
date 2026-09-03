package nadiendev.ultimatetransport.datagen.providers;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.registry.UTBlocks;
import nadiendev.ultimatetransport.registry.UTTags;
import nadiendev.ultimatetransport.tube.TubeRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class UTBlockTagsProvider extends BlockTagsProvider {

    public UTBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                               @Nullable ExistingFileHelper helper) {
        super(output, registries, UltimateTransport.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        // The optional cables go in by id: without their mod they are never registered, and a hard
        // tag entry naming a missing block fails the whole tag.
        UTBlocks.cables().forEach(holder -> {
            if (holder == UTBlocks.GAS_CABLE || holder == UTBlocks.SOURCE_CABLE) {
                pickaxe.addOptional(holder.getId());
            } else {
                pickaxe.add(holder.get());
            }
        });
        UTBlocks.cells().forEach(holder -> pickaxe.add(holder.get()));
        UTBlocks.generators().forEach(holder -> pickaxe.add(holder.get()));
        pickaxe.add(TubeRegistration.TUBE.get());
        pickaxe.add(TubeRegistration.STATION.get());
        pickaxe.add(TubeRegistration.STATION_HORIZONTAL.get());

        var machines = tag(UTTags.MACHINES);
        UTBlocks.cells().forEach(holder -> machines.add(holder.get()));
        UTBlocks.generators().forEach(holder -> machines.add(holder.get()));

        var stone = tag(BlockTags.NEEDS_STONE_TOOL);
        UTBlocks.cells().forEach(holder -> stone.add(holder.get()));
        UTBlocks.generators().forEach(holder -> stone.add(holder.get()));
    }
}
