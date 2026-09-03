package nadiendev.ultimatetransport.menu;

import nadiendev.ultimatetransport.generator.GeneratorBlock;
import nadiendev.ultimatetransport.generator.GeneratorBlockEntity;
import nadiendev.ultimatetransport.registry.UTMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class GeneratorMenu extends AbstractContainerMenu {
    public static final int WIDTH = 176;
    public static final int HEIGHT = 166;

    private final GeneratorBlockEntity generator;

    public GeneratorMenu(int id, Inventory inventory, GeneratorBlockEntity generator) {
        super(UTMenus.GENERATOR.get(), id);
        this.generator = generator;

        addSlot(new SlotItemHandler(generator.slots(), GeneratorBlockEntity.FUEL_SLOT, 44, 42) {
            @Override
            public boolean isActive() {
                return generator.type().consumesItems();
            }
        });
        addSlot(new SlotItemHandler(generator.slots(), GeneratorBlockEntity.CHARGE_SLOT, 134, 42));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    public GeneratorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, resolve(inventory, buffer.readBlockPos()));
    }

    private static GeneratorBlockEntity resolve(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof GeneratorBlockEntity generator) {
            return generator;
        }
        throw new IllegalStateException("No generator at " + pos);
    }

    public GeneratorBlockEntity generator() {
        return generator;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockPos pos = generator.getBlockPos();
        return !generator.isRemoved()
                && player.level().getBlockState(pos).getBlock() instanceof GeneratorBlock
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
