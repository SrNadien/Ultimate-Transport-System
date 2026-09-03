package nadiendev.ultimatetransport.compat.flux;

import nadiendev.ultimatetransport.cell.EnergyCellItem;
import nadiendev.ultimatetransport.cell.EnergyCellTier;
import net.minecraft.world.item.ItemStack;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

/** A cell sitting in a Flux Storage or a player's inventory, at long precision. */
public record FluxCellItemEnergy(ItemStack stack, EnergyCellTier tier) implements IFNEnergyStorage {

    @Override
    public long receiveEnergyL(long amount, boolean simulate) {
        long current = EnergyCellItem.charge(stack);
        long accepted = Math.min(Math.min(amount, tier.transfer()), tier.capacity() - current);
        if (accepted <= 0) {
            return 0L;
        }
        if (!simulate) {
            EnergyCellItem.setCharge(stack, current + accepted);
        }
        return accepted;
    }

    @Override
    public long extractEnergyL(long amount, boolean simulate) {
        long current = EnergyCellItem.charge(stack);
        long removed = Math.min(Math.min(amount, tier.transfer()), current);
        if (removed <= 0) {
            return 0L;
        }
        if (!simulate) {
            EnergyCellItem.setCharge(stack, current - removed);
        }
        return removed;
    }

    @Override
    public long getEnergyStoredL() {
        return EnergyCellItem.charge(stack);
    }

    @Override
    public long getMaxEnergyStoredL() {
        return tier.capacity();
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
