package nadiendev.ultimatetransport.compat.mekanism;

import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.compat.ChemicalBridge;
import nadiendev.ultimatetransport.registry.UTBlocks;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * The only place that decides whether Mekanism is there. Every method here touches Mekanism classes,
 * so callers must check {@link nadiendev.ultimatetransport.compat.ModIds#MEKANISM} first; the JVM then
 * never loads these classes on an installation without the mod.
 */
public final class MekanismCompat {

    private MekanismCompat() {
    }

    public static void install() {
        ChemicalBridge.set(new MekanismChemicalBridge());
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (var cable : UTBlocks.cables()) {
            event.registerBlock(mekanism.common.capabilities.Capabilities.CHEMICAL.block(),
                    (level, pos, state, blockEntity, side) -> side != null
                            && blockEntity instanceof CableBlockEntity handler
                            && handler.type().carries(TransferType.GAS)
                            && handler.linked(side)
                            ? new NetworkChemical((CableBlockEntity) blockEntity, side)
                            : null,
                    cable.get());
        }
    }
}
