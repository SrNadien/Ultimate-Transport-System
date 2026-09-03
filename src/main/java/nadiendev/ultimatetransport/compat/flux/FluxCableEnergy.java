package nadiendev.ultimatetransport.compat.flux;

import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import net.minecraft.core.Direction;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

/**
 * Lets a Flux Point feed a cable network directly. The cable still buffers nothing: whatever the
 * network can place this tick is taken, the rest is refused.
 */
public record FluxCableEnergy(CableBlockEntity cable, Direction side) implements IFNEnergyStorage {

    @Override
    public long receiveEnergyL(long amount, boolean simulate) {
        int budget = (int) Math.min(Math.min(amount, cable.config(side, TransferType.ENERGY).tier().energyRate()), Integer.MAX_VALUE);
        return cable.pushEnergy(side, cable.config(side, TransferType.ENERGY), budget, simulate);
    }

    @Override
    public long extractEnergyL(long amount, boolean simulate) {
        return 0L;
    }

    @Override
    public long getEnergyStoredL() {
        return 0L;
    }

    @Override
    public long getMaxEnergyStoredL() {
        return cable.config(side, TransferType.ENERGY).tier().energyRate();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
