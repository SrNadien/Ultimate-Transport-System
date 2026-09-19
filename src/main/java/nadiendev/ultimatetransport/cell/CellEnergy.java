package nadiendev.ultimatetransport.cell;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/**
 * One face of a cell seen through the energy capability. A bank holds more than an int can carry, so
 * once it does, both figures are reported as the same share of {@link Integer#MAX_VALUE} that the
 * real ones are of each other. A meter reading them sees the right proportion rather than a bar that
 * sits full; the real numbers are in the tooltip.
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
        return scale(cell.stored());
    }

    @Override
    public int getMaxEnergyStored() {
        return scale(cell.capacity());
    }

    private int scale(long value) {
        long room = cell.capacity();
        if (room <= 0L) {
            return 0;
        }
        if (room <= Integer.MAX_VALUE) {
            return (int) Math.max(Math.min(value, room), 0L);
        }
        double share = Math.max(Math.min((double) value / room, 1.0), 0.0);
        return (int) Math.round(share * Integer.MAX_VALUE);
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
