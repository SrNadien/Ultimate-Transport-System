package nadiendev.ultimatetransport.client;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.item.ConfiguratorItem;
import nadiendev.ultimatetransport.registry.UTBlockEntities;
import nadiendev.ultimatetransport.tube.TubeConfig;
import nadiendev.ultimatetransport.tube.TubeRegistration;
import nadiendev.ultimatetransport.tube.client.TubeModelLoader;
import nadiendev.ultimatetransport.registry.UTBlocks;
import nadiendev.ultimatetransport.registry.UTItems;
import nadiendev.ultimatetransport.registry.UTMenus;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = UltimateTransport.MODID, value = Dist.CLIENT)
public final class UTClient {

    private UTClient() {
    }

    @SubscribeEvent
    public static void registerTubeGeometry(ModelEvent.RegisterGeometryLoaders event) {
        event.register(UltimateTransport.id("tube"), TubeModelLoader.TUBE);
        event.register(UltimateTransport.id("station"), TubeModelLoader.STATION);
    }

    @SubscribeEvent
    public static void tubeRenderLayers(FMLClientSetupEvent event) {
        RenderType pass = TubeConfig.RENDER_PASS.get() == TubeConfig.RenderPass.CUTOUT
                ? RenderType.cutoutMipped()
                : RenderType.translucent();
        ItemBlockRenderTypes.setRenderLayer(TubeRegistration.TUBE.get(), pass);
        ItemBlockRenderTypes.setRenderLayer(TubeRegistration.STATION.get(), pass);
        ItemBlockRenderTypes.setRenderLayer(TubeRegistration.STATION_HORIZONTAL.get(), pass);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(UTMenus.CABLE.get(), CableScreen::new);
        event.register(UTMenus.FILTER.get(), FilterScreen::new);
        event.register(UTMenus.CELL.get(), CellScreen::new);
        event.register(UTMenus.GENERATOR.get(), GeneratorScreen::new);
        event.register(UTMenus.SIDE_CONFIG.get(), SideConfigScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(UTBlockEntities.CABLE.get(), CableRenderer::new);
    }

    @SubscribeEvent
    public static void tintConfigurator(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> layer == 0 ? ConfiguratorItem.mode(stack).tint() : 0xFFFFFFFF,
                UTItems.CONFIGURATOR.get());
    }

    /** The face collars are not part of any block state, so they are baked as standalone models. */
    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(CableRenderer.EXTRACT);
    }

    /** Every baked cable state is wrapped so it can draw the facades its block entity reports. */
    @SubscribeEvent
    public static void wrapCableModels(ModelEvent.ModifyBakingResult event) {
        // Panels are cached by covering block; a rebake can hand back different sprites for the same
        // block, so the cache goes with it.
        FacadeCableModel.clearCache();
        for (var cable : UTBlocks.cables()) {
            for (BlockState state : cable.get().getStateDefinition().getPossibleStates()) {
                ModelResourceLocation key = BlockModelShaper.stateToModelLocation(state);
                BakedModel model = event.getModels().get(key);
                if (model != null && !(model instanceof FacadeCableModel)) {
                    event.getModels().put(key, new FacadeCableModel(model));
                }
            }
        }
    }
}
