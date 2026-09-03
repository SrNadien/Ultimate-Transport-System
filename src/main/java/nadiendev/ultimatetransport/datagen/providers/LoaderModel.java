package nadiendev.ultimatetransport.datagen.providers;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class LoaderModel<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {

    public LoaderModel(ResourceLocation loader, T parent, ExistingFileHelper helper) {
        super(loader, parent, helper, false);
    }
}
