package nadiendev.ultimatetransport.datagen.providers;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.registry.UTItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.minecraft.data.tags.ItemTagsProvider;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * The configurator answers to the same tags Mekanism's does, so any machine that asks for a wrench
 * by tag rather than by item takes it: {@code mekanism:configurators} is itself just a pointer at
 * {@code c:tools/wrench}, which is where the real membership lives.
 */
public class UTItemTagsProvider extends ItemTagsProvider {

    private static final TagKey<Item> TOOLS = tag("c", "tools");
    private static final TagKey<Item> WRENCH = tag("c", "tools/wrench");
    private static final TagKey<Item> CONFIGURATORS = tag("mekanism", "configurators");
    private static final TagKey<Item> ULTIMINE = tag("ftbultimine", "included_tools");

    public UTItemTagsProvider(PackOutput output,
                              CompletableFuture<HolderLookup.Provider> registries,
                              CompletableFuture<TagLookup<net.minecraft.world.level.block.Block>> blocks,
                              @Nullable ExistingFileHelper helper) {
        super(output, registries, blocks, UltimateTransport.MODID, helper);
    }

    private static TagKey<Item> tag(String namespace, String path) {
        return TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(TOOLS).add(UTItems.CONFIGURATOR.get());
        tag(WRENCH).add(UTItems.CONFIGURATOR.get());
        tag(CONFIGURATORS).addOptional(UTItems.CONFIGURATOR.getId());
        tag(ULTIMINE).addOptional(UTItems.CONFIGURATOR.getId());
    }
}
