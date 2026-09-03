package nadiendev.ultimatetransport.menu;

import nadiendev.ultimatetransport.cell.EnergyCellBlock;
import nadiendev.ultimatetransport.cell.EnergyCellBlockEntity;
import nadiendev.ultimatetransport.registry.UTMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

/** The cell's own screen. It holds no items: everything on it is a readout or a face setting. */
public class CellMenu extends AbstractContainerMenu {

    public static final int WIDTH = 176;
    public static final int HEIGHT = 222;

    private final EnergyCellBlockEntity cell;

    public CellMenu(int id, Inventory inventory, EnergyCellBlockEntity cell) {
        super(UTMenus.CELL.get(), id);
        this.cell = cell;

        addSlot(new SlotItemHandler(cell.slots(), EnergyCellBlockEntity.CHARGE_SLOT, 133, 18));
        addSlot(new SlotItemHandler(cell.slots(), EnergyCellBlockEntity.DISCHARGE_SLOT, 151, 18));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 140 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 198));
        }
    }

    public CellMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, resolve(inventory, buffer.readBlockPos()));
    }

    private static EnergyCellBlockEntity resolve(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof EnergyCellBlockEntity cell) {
            return cell;
        }
        throw new IllegalStateException("No energy cell at " + pos);
    }

    public EnergyCellBlockEntity cell() {
        return cell;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockPos pos = cell.getBlockPos();
        return !cell.isRemoved()
                && player.level().getBlockState(pos).getBlock() instanceof EnergyCellBlock
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 2) {
            if (!moveItemStackTo(stack, 2, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 2, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }
}
