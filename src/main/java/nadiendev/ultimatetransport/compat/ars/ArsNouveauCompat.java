package nadiendev.ultimatetransport.compat.ars;

import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.compat.SourceBridge;
import nadiendev.ultimatetransport.registry.UTBlocks;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ArsNouveauCompat {
    private ArsNouveauCompat() {
    }

    public static void install() {
        SourceBridge.set(new ArsSourceBridge());
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (var cable : UTBlocks.cables()) {
            event.registerBlock(ArsCapabilities.SOURCE,
                    (level, pos, state, blockEntity, side) -> side != null
                            && blockEntity instanceof CableBlockEntity handler
                            && handler.type().carries(TransferType.SOURCE)
                            && handler.linked(side)
                            ? new NetworkSource(handler, side)
                            : null,
                    cable.get());
        }
    }
}
