package nadiendev.ultimatetransport.menu;

import nadiendev.ultimatetransport.cable.CableBlock;
import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.registry.UTMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

/** The face's own screen: modes, the upgrade slot and the list of filter rules. */
public class CableMenu extends AbstractContainerMenu implements CableSideMenu {

    public static final int WIDTH = 176;
    public static final int HEIGHT = 216;

    private final CableBlockEntity cable;
    private final Direction side;
    private final TransferType cargo;

    public CableMenu(int id, Inventory inventory, CableBlockEntity cable, Direction side, TransferType cargo) {
        super(UTMenus.CABLE.get(), id);
        this.cable = cable;
        this.side = side;
        this.cargo = cargo;

        addSlot(new SlotItemHandler(new SideUpgradeHandler(cable, side, cargo), 0, 9, 102));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 131 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 189));
        }
    }

    public CableMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, resolve(inventory, buffer.readBlockPos()),
                Direction.values()[buffer.readByte()], TransferType.values()[buffer.readByte()]);

    }
    static CableBlockEntity resolve(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof CableBlockEntity cable) {
            return cable;
        }
        throw new IllegalStateException("No cable at " + pos);
    }

    @Override
    public CableBlockEntity cable() {
        return cable;
    }

    @Override
    public TransferType cargo() {
        return cargo;
    }

    @Override
    public Direction side() {
        return side;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockPos pos = cable.getBlockPos();
        return !cable.isRemoved()
                && player.level().getBlockState(pos).getBlock() instanceof CableBlock
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

        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
            int inventoryEnd = 1 + 27;
            if (index < inventoryEnd) {
                if (!moveItemStackTo(stack, inventoryEnd, slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 1, inventoryEnd, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }
}
