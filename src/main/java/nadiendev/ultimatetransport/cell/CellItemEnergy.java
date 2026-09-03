package nadiendev.ultimatetransport.cell;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;

/** A cell carried in an inventory still works, so it can charge tools or feed a machine directly. */
public record CellItemEnergy(ItemStack stack, EnergyCellTier tier) implements IEnergyStorage {

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (tier.creative()) {
            return Math.max(maxReceive, 0);
        }
        long current = EnergyCellItem.charge(stack);
        long accepted = Math.min(Math.min(maxReceive, tier.transfer()), tier.capacity() - current);
        if (accepted <= 0) {
            return 0;
        }
        if (!simulate) {
            EnergyCellItem.setCharge(stack, current + accepted);
        }
        return (int) accepted;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (tier.creative()) {
            return Math.max(Math.min(maxExtract, tier.transfer()), 0);
        }
        long current = EnergyCellItem.charge(stack);
        long removed = Math.min(Math.min(maxExtract, tier.transfer()), current);
        if (removed <= 0) {
            return 0;
        }
        if (!simulate) {
            EnergyCellItem.setCharge(stack, current - removed);
        }
        return (int) removed;
    }

    @Override
    public int getEnergyStored() {
        return tier.creative() ? Integer.MAX_VALUE : (int) Math.min(EnergyCellItem.charge(stack), Integer.MAX_VALUE);
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) Math.min(tier.capacity(), Integer.MAX_VALUE);
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
