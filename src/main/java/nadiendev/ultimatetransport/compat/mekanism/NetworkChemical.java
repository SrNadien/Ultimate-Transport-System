package nadiendev.ultimatetransport.compat.mekanism;

import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.compat.ChemicalBridge;
import net.minecraft.core.Direction;

/** What a Mekanism machine sees when it pushes gas into a gas cable. */
public record NetworkChemical(CableBlockEntity cable, Direction side) implements IChemicalHandler {

    @Override
    public int getChemicalTanks() {
        return 1;
    }

    @Override
    public ChemicalStack getChemicalInTank(int tank) {
        return ChemicalStack.EMPTY;
    }

    @Override
    public void setChemicalInTank(int tank, ChemicalStack stack) {
    }

    @Override
    public long getChemicalTankCapacity(int tank) {
        return cable.config(side, TransferType.GAS).tier().gasRate();
    }

    @Override
    public boolean isValid(int tank, ChemicalStack stack) {
        return bridge() != null && bridge().allows(cable.config(side, TransferType.GAS), stack);
    }

    @Override
    public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
        MekanismChemicalBridge bridge = bridge();
        if (bridge == null || stack.isEmpty() || !isValid(tank, stack)) {
            return stack;
        }
        long budget = Math.min(stack.getAmount(), cable.config(side, TransferType.GAS).tier().gasRate());
        long moved = bridge.push(cable, side, cable.config(side, TransferType.GAS), stack.copyWithAmount(budget), action.simulate());
        return moved >= stack.getAmount() ? ChemicalStack.EMPTY : stack.copyWithAmount(stack.getAmount() - moved);
    }

    @Override
    public ChemicalStack extractChemical(int tank, long amount, Action action) {
        return ChemicalStack.EMPTY;
    }

    private static MekanismChemicalBridge bridge() {
        return ChemicalBridge.get() instanceof MekanismChemicalBridge mekanism ? mekanism : null;
    }
}
