package nadiendev.ultimatetransport.compat.flux;

import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.cell.EnergyCellBlockEntity;
import nadiendev.ultimatetransport.registry.UTBlocks;
import nadiendev.ultimatetransport.registry.UTItems;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import sonar.fluxnetworks.api.FluxCapabilities;

/**
 * Flux Networks already reaches this mod through Forge Energy, so plugs and points work with no code
 * at all. What this adds is Flux's own long-precision interface, which matters above tier 6 where a
 * cell holds more energy than an int can express.
 */
public final class FluxCompat {

    private FluxCompat() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (var cell : UTBlocks.cells()) {
            event.registerBlock(FluxCapabilities.BLOCK,
                    (level, pos, state, blockEntity, side) -> blockEntity instanceof EnergyCellBlockEntity storage
                            ? new FluxCellEnergy(storage, side)
                            : null,
                    cell.get());
        }

        for (var entry : UTItems.CELLS.entrySet()) {
            event.registerItem(FluxCapabilities.ITEM,
                    (stack, context) -> new FluxCellItemEnergy(stack, entry.getKey()),
                    entry.getValue().get());
        }

        for (var cable : UTBlocks.cables()) {
            event.registerBlock(FluxCapabilities.BLOCK,
                    (level, pos, state, blockEntity, side) -> side != null
                            && blockEntity instanceof CableBlockEntity handler
                            && handler.type().carries(TransferType.ENERGY)
                            && handler.linked(side)
                            ? new FluxCableEnergy((CableBlockEntity) blockEntity, side)
                            : null,
                    cable.get());
        }
    }
}
