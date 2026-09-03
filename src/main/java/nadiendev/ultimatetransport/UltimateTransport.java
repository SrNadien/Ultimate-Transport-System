package nadiendev.ultimatetransport;

import com.mojang.logging.LogUtils;
import nadiendev.ultimatetransport.registry.UTBlockEntities;
import nadiendev.ultimatetransport.registry.UTBlocks;
import nadiendev.ultimatetransport.registry.UTCapabilities;
import nadiendev.ultimatetransport.registry.UTCreativeTabs;
import nadiendev.ultimatetransport.registry.UTDataComponents;
import nadiendev.ultimatetransport.registry.UTItems;
import nadiendev.ultimatetransport.compat.ModIds;
import nadiendev.ultimatetransport.compat.excavation.OreExcavationCompat;
import nadiendev.ultimatetransport.config.UTClientConfig;
import nadiendev.ultimatetransport.config.UTServerConfig;
import nadiendev.ultimatetransport.registry.UTMenus;
import nadiendev.ultimatetransport.registry.UTRecipes;
import nadiendev.ultimatetransport.tube.TubeConfig;
import nadiendev.ultimatetransport.tube.TubeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(UltimateTransport.MODID)
public class UltimateTransport {

    public static final String MODID = "ultimatetransport";
    public static final Logger LOGGER = LogUtils.getLogger();

    public UltimateTransport(IEventBus modBus, ModContainer container) {
        UTBlocks.BLOCKS.register(modBus);
        UTItems.ITEMS.register(modBus);
        UTBlockEntities.BLOCK_ENTITIES.register(modBus);
        UTCreativeTabs.TABS.register(modBus);
        UTMenus.MENUS.register(modBus);
        UTDataComponents.COMPONENTS.register(modBus);
        UTRecipes.TYPES.register(modBus);
        UTRecipes.SERIALIZERS.register(modBus);

        modBus.addListener(UTCapabilities::register);
        modBus.addListener(TubeConfig::onLoad);
        modBus.addListener(TubeConfig::onReload);
        container.registerConfig(ModConfig.Type.COMMON, TubeConfig.SPEC);
        TubeRegistration.touch();

        container.registerConfig(ModConfig.Type.CLIENT, UTClientConfig.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, UTServerConfig.SPEC);

        if (ModIds.loaded(ModIds.ORE_EXCAVATION)) {
            OreExcavationCompat.install(NeoForge.EVENT_BUS);
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
