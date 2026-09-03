package nadiendev.ultimatetransport.compat.flux;

import nadiendev.ultimatetransport.cell.CellSideMode;
import nadiendev.ultimatetransport.cell.EnergyCellBlockEntity;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

/**
 * A cell as Flux Networks sees it. Flux speaks in longs, so a tier 8 cell reports its real
 * sixteen-billion-FE capacity here instead of the int-clamped figure the Forge Energy view has to give.
 */
public record FluxCellEnergy(EnergyCellBlockEntity cell, @Nullable Direction side) implements IFNEnergyStorage {

    private CellSideMode mode() {
        return side == null ? CellSideMode.BOTH : cell.side(side);
    }

    @Override
    public long receiveEnergyL(long amount, boolean simulate) {
        return mode().input() ? cell.receive(amount, simulate) : 0L;
    }

    @Override
    public long extractEnergyL(long amount, boolean simulate) {
        return mode().output() ? cell.extract(amount, simulate) : 0L;
    }

    @Override
    public long getEnergyStoredL() {
        return cell.stored();
    }

    @Override
    public long getMaxEnergyStoredL() {
        return cell.capacity();
    }

    @Override
    public boolean canExtract() {
        return mode().output();
    }

    @Override
    public boolean canReceive() {
        return mode().input();
    }
}
