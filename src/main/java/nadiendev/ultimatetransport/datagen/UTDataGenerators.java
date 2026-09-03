package nadiendev.ultimatetransport.datagen;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.datagen.providers.UTBlockLoot;
import nadiendev.ultimatetransport.datagen.providers.UTBlockStateProvider;
import nadiendev.ultimatetransport.datagen.providers.UTBlockTagsProvider;
import nadiendev.ultimatetransport.datagen.providers.UTItemTagsProvider;
import nadiendev.ultimatetransport.datagen.providers.UTItemModelProvider;
import nadiendev.ultimatetransport.datagen.providers.UTLanguageProvider;
import nadiendev.ultimatetransport.datagen.providers.UTRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = UltimateTransport.MODID)
public final class UTDataGenerators {

    private UTDataGenerators() {
    }

    @SubscribeEvent
    public static void gather(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new UTBlockStateProvider(output, helper));
        generator.addProvider(event.includeClient(), new UTItemModelProvider(output, helper));
        generator.addProvider(event.includeClient(), new UTLanguageProvider.English(output));
        generator.addProvider(event.includeClient(), new UTLanguageProvider.Spanish(output));

        generator.addProvider(event.includeServer(), new UTRecipeProvider(output, registries));
        UTBlockTagsProvider blockTags = new UTBlockTagsProvider(output, registries, helper);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(),
                new UTItemTagsProvider(output, registries, blockTags.contentsGetter(), helper));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(UTBlockLoot::new, LootContextParamSets.BLOCK)),
                registries));
    }
}
