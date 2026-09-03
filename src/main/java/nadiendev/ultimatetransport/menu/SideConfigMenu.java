package nadiendev.ultimatetransport.menu;

import nadiendev.ultimatetransport.registry.UTMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class SideConfigMenu extends AbstractContainerMenu {

    public static final int WIDTH = 156;
    public static final int HEIGHT = 135;

    private final Player owner;
    private final BlockPos pos;

    public SideConfigMenu(int id, Inventory inventory, BlockPos pos) {
        super(UTMenus.SIDE_CONFIG.get(), id);
        this.owner = inventory.player;
        this.pos = pos;
    }

    public SideConfigMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos());
    }

    public BlockPos pos() {
        return pos;
    }

    @Nullable
    public BlockEntity target() {
        BlockEntity blockEntity = owner.level().getBlockEntity(pos);
        return SideConfigTarget.supports(blockEntity) ? blockEntity : null;
    }

    @Override
    public boolean stillValid(Player player) {
        return target() != null
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
