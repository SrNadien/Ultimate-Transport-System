package nadiendev.ultimatetransport.compat.excavation;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.registry.UTTags;
import net.neoforged.bus.api.IEventBus;
import oreexcavation.events.EventExcavateRequest;

public final class OreExcavationCompat {
    private OreExcavationCompat() {
    }

    public static void install(IEventBus gameBus) {
        gameBus.addListener(OreExcavationCompat::onRequest);
        UltimateTransport.LOGGER.info("Ore Excavation found: machines excluded from chain mining");
    }

    private static void onRequest(EventExcavateRequest event) {
        if (event.state.is(UTTags.MACHINES)) {
            event.setCanceled(true);
        }
    }
}
