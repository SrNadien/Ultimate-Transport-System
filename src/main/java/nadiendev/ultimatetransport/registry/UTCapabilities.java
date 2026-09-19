package nadiendev.ultimatetransport.registry;

import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.cable.io.NetworkEnergy;
import nadiendev.ultimatetransport.cable.io.NetworkFluid;
import nadiendev.ultimatetransport.cable.io.NetworkItems;
import nadiendev.ultimatetransport.cell.CellEnergy;
import nadiendev.ultimatetransport.cell.CellItemEnergy;
import nadiendev.ultimatetransport.cell.EnergyCellBlockEntity;
import nadiendev.ultimatetransport.compat.ModIds;
import nadiendev.ultimatetransport.compat.ars.ArsNouveauCompat;
import nadiendev.ultimatetransport.compat.enderio.EnderIOCompat;
import nadiendev.ultimatetransport.compat.flux.FluxCompat;
import nadiendev.ultimatetransport.compat.mekanism.MekanismCompat;
import nadiendev.ultimatetransport.generator.GeneratorBlockEntity;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;


public final class UTCapabilities {

    private UTCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        for (var cable : UTBlocks.cables()) {
            event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                    (level, pos, state, blockEntity, side) -> accepts(blockEntity, side, TransferType.ENERGY)
                            ? new NetworkEnergy((CableBlockEntity) blockEntity, side)
                            : null,
                    cable.get());

            event.registerBlock(Capabilities.FluidHandler.BLOCK,
                    (level, pos, state, blockEntity, side) -> accepts(blockEntity, side, TransferType.FLUID)
                            ? new NetworkFluid((CableBlockEntity) blockEntity, side)
                            : null,
                    cable.get());

            event.registerBlock(Capabilities.ItemHandler.BLOCK,
                    (level, pos, state, blockEntity, side) -> accepts(blockEntity, side, TransferType.ITEM)
                            ? new NetworkItems((CableBlockEntity) blockEntity, side)
                            : null,
                    cable.get());
        }

        for (var cell : UTBlocks.cells()) {
            event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                    (level, pos, state, blockEntity, side) -> blockEntity instanceof EnergyCellBlockEntity storage
                            ? new CellEnergy(storage, side)
                            : null,
                    cell.get());
        }

        for (var generator : UTBlocks.generators()) {
            event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                    (level, pos, state, blockEntity, side) -> blockEntity instanceof GeneratorBlockEntity source
                            && (side == null || source.side(side).output())
                            ? source.energy()
                            : null,
                    generator.get());

            event.registerBlock(Capabilities.ItemHandler.BLOCK,
                    (level, pos, state, blockEntity, side) -> blockEntity instanceof GeneratorBlockEntity source
                            && source.type().consumesItems()
                            ? source.slots()
                            : null,
                    generator.get());
        }

        for (var entry : UTItems.CELLS.entrySet()) {
            event.registerItem(Capabilities.EnergyStorage.ITEM,
                    (stack, context) -> new CellItemEnergy(stack, entry.getKey()),
                    entry.getValue().get());
        }

        if (ModIds.loaded(ModIds.MEKANISM)) {
            MekanismCompat.install();
            MekanismCompat.registerCapabilities(event);
        }
        if (ModIds.loaded(ModIds.ARS_NOUVEAU)) {
            ArsNouveauCompat.install();
            ArsNouveauCompat.registerCapabilities(event);
        }
        if (ModIds.loaded(ModIds.ENDER_IO)) {
            EnderIOCompat.registerCapabilities(event);
        }
        if (ModIds.loaded(ModIds.FLUX_NETWORKS)) {
            FluxCompat.registerCapabilities(event);
        }
    }


    private static boolean accepts(Object blockEntity, Direction side, TransferType content) {
        return side != null
                && blockEntity instanceof CableBlockEntity cable
                && cable.type().carries(content)
                && (cable.cableNeighbour(side) || !cable.idle(side));
    }
}
