package nadiendev.ultimatetransport.compat.mekanism;

import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.cable.CableNetwork;
import nadiendev.ultimatetransport.cable.SideConfig;
import nadiendev.ultimatetransport.compat.ChemicalBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The gas cable, expressed entirely in Mekanism's chemical API. Nothing outside this package mentions
 * a Mekanism type, so with the mod absent the cable simply never finds anything to move.
 */
public class MekanismChemicalBridge extends ChemicalBridge {

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public boolean present(Level level, BlockPos pos, Direction face) {
        return level.isLoaded(pos) && level.getCapability(Capabilities.CHEMICAL.block(), pos, face) != null;
    }

    @Override
    public void extract(CableBlockEntity cable, Direction side, SideConfig config) {
        Level level = cable.getLevel();
        if (level == null) {
            return;
        }
        IChemicalHandler source = level.getCapability(Capabilities.CHEMICAL.block(),
                cable.getBlockPos().relative(side), side.getOpposite());
        if (source == null) {
            return;
        }
        long remaining = config.tier().gasRate();
        for (int tank = 0; tank < source.getChemicalTanks() && remaining > 0; tank++) {
            ChemicalStack inTank = source.getChemicalInTank(tank);
            if (inTank.isEmpty() || !allows(config, inTank)) {
                continue;
            }
            ChemicalStack drained = source.extractChemical(inTank.copyWithAmount(remaining), Action.SIMULATE);
            if (drained.isEmpty()) {
                continue;
            }
            long moved = push(cable, side, config, drained, false);
            if (moved > 0) {
                source.extractChemical(drained.copyWithAmount(moved), Action.EXECUTE);
                remaining -= moved;
            }
        }
    }

    /** Spreads a chemical over the network's insert faces, mirroring how fluids are spread. */
    public long push(CableBlockEntity cable, Direction side, SideConfig config, ChemicalStack stack, boolean simulate) {
        Level level = cable.getLevel();
        if (level == null || stack.isEmpty()) {
            return 0;
        }
        List<CableNetwork.Target> targets = cable.orderedTargets(side, config);
        if (targets.isEmpty()) {
            return 0;
        }
        Action action = simulate ? Action.SIMULATE : Action.EXECUTE;
        long remaining = stack.getAmount();

        for (CableNetwork.Target target : targets) {
            if (remaining <= 0) {
                break;
            }
            IChemicalHandler destination = level.getCapability(Capabilities.CHEMICAL.block(),
                    target.destination(), target.face());
            if (destination == null) {
                continue;
            }
            ChemicalStack offered = stack.copyWithAmount(remaining);
            ChemicalStack left = destination.insertChemical(offered, action);
            remaining = left.isEmpty() ? 0 : left.getAmount();
        }
        return stack.getAmount() - remaining;
    }

    /** A gas rule is a chemical id or a chemical tag; chemicals carry no components to narrow by. */
    public boolean allows(SideConfig config, ChemicalStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return config.filter().allowsChemical(stack.getChemical().getRegistryName(),
                tag -> stack.getTags().anyMatch(key -> key.location().equals(tag)));
    }

    /** Reads whatever chemical the item held in a filter slot contains, for the quick-fill shortcut. */
    @Nullable
    public static ChemicalStack contained(ItemStack stack) {
        IChemicalHandler handler = Capabilities.CHEMICAL.getCapability(stack);
        if (handler == null) {
            return null;
        }
        for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
            ChemicalStack held = handler.getChemicalInTank(tank);
            if (!held.isEmpty()) {
                return held;
            }
        }
        return null;
    }
}
