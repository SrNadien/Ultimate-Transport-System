package nadiendev.ultimatetransport.cable.io;

import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * What a machine sees when it pushes power into a cable. There is no buffer: whatever the network can
 * place right now is accepted, and the rest is refused, so energy never sits inside a cable.
 */
public record NetworkEnergy(CableBlockEntity cable, Direction side) implements IEnergyStorage {

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int budget = Math.min(maxReceive, cable.config(side, TransferType.ENERGY).tier().energyRate());
        return cable.pushEnergy(side, cable.config(side, TransferType.ENERGY), budget, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
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
