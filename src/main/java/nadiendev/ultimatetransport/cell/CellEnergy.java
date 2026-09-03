package nadiendev.ultimatetransport.cell;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/**
 * One face of a cell seen through the energy capability. Cells hold more than an int can carry, so
 * the reported figures saturate at {@link Integer#MAX_VALUE}; the real numbers are in the tooltip.
 */
public record CellEnergy(EnergyCellBlockEntity cell, @Nullable Direction side) implements IEnergyStorage {

    private CellSideMode mode() {
        return side == null ? CellSideMode.BOTH : cell.side(side);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return mode().input() ? (int) cell.receive(maxReceive, simulate) : 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return mode().output() ? (int) cell.extract(maxExtract, simulate) : 0;
    }

    @Override
    public int getEnergyStored() {
        return (int) Math.min(cell.stored(), Integer.MAX_VALUE);
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) Math.min(cell.capacity(), Integer.MAX_VALUE);
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
