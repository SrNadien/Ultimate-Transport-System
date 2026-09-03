package nadiendev.ultimatetransport.menu;

import nadiendev.ultimatetransport.cable.CableBlock;
import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.filter.FilterEntry;
import nadiendev.ultimatetransport.registry.UTMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** The editor for one rule. It owns no slots of its own: the rule is typed, not placed. */
public class FilterMenu extends AbstractContainerMenu implements CableSideMenu {

    public static final int WIDTH = 176;
    public static final int HEIGHT = 222;

    private final CableBlockEntity cable;
    private final Direction side;
    private final FilterEntry entry;

    private final TransferType cargo;

    @Override
    public TransferType cargo() {
        return cargo;
    }

    public FilterMenu(int id, Inventory inventory, CableBlockEntity cable, Direction side, FilterEntry entry) {
        this(id, inventory, cable, side, entry, cable.primary());
    }

    public FilterMenu(int id, Inventory inventory, CableBlockEntity cable, Direction side, FilterEntry entry,
                      TransferType cargo) {
        super(UTMenus.FILTER.get(), id);
        this.cargo = cargo;
        this.cable = cable;
        this.side = side;
        this.entry = entry;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 140 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 198));
        }
    }

    public FilterMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory,
                CableMenu.resolve(inventory, buffer.readBlockPos()),
                Direction.values()[buffer.readByte()],
                FilterEntry.STREAM_CODEC.decode(buffer));
    }

    @Override
    public CableBlockEntity cable() {
        return cable;
    }

    @Override
    public Direction side() {
        return side;
    }

    /** The working copy the screen edits; nothing is stored until Submit is pressed. */
    public FilterEntry entry() {
        return entry;
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
        return ItemStack.EMPTY;
    }
}
