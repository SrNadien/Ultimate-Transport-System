package nadiendev.ultimatetransport.compat.enderio;

import com.enderio.enderio.api.EnderIOCapabilities;
import com.enderio.enderio.api.conduits.facade.ConduitFacadeProvider;
import com.enderio.enderio.api.conduits.facade.FacadeType;
import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.facade.FacadeItem;
import nadiendev.ultimatetransport.facade.FacadeSupport;
import nadiendev.ultimatetransport.registry.UTItems;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class EnderIOCompat {
    private EnderIOCompat() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(EnderIOCapabilities.CONDUIT_FACADE_PROVIDER,
                (stack, context) -> new FacadeBridge(FacadeItem.facadeOf(stack)),
                UTItems.FACADE.get());
        FacadeSupport.setForeignReader(stack -> {
            ConduitFacadeProvider provider = stack.getCapability(EnderIOCapabilities.CONDUIT_FACADE_PROVIDER);
            return provider != null && provider.isValid() && provider.block() != null
                    ? provider.block().defaultBlockState()
                    : null;
        });
        UltimateTransport.LOGGER.info("Ender IO found: facades work both ways");
    }

    private record FacadeBridge(BlockState painted) implements ConduitFacadeProvider {
        @Override
        public boolean isValid() {
            return painted != null;
        }

        @Override
        public Block block() {
            return painted == null ? null : painted.getBlock();
        }

        @Override
        public FacadeType type() {
            return FacadeType.BASIC;
        }
    }
}
