package nadiendev.ultimatetransport.item;

import nadiendev.ultimatetransport.cable.CableBlock;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.ConnectionMode;
import nadiendev.ultimatetransport.cell.EnergyCellBlock;
import nadiendev.ultimatetransport.facade.FacadeItem;
import nadiendev.ultimatetransport.generator.GeneratorBlock;
import nadiendev.ultimatetransport.menu.SideConfigMenu;
import nadiendev.ultimatetransport.menu.SideConfigTarget;
import nadiendev.ultimatetransport.registry.UTDataComponents;
import nadiendev.ultimatetransport.registry.UTItems;
import nadiendev.ultimatetransport.tube.block.BlockStation;
import nadiendev.ultimatetransport.tube.block.BlockStationHorizontal;
import nadiendev.ultimatetransport.tube.block.BlockTube;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.items.IItemHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The interaction follows Mekanism's configurator, which is MIT licensed: a plain click on a machine
 * reads the face back, a sneaking click steps it, rotate points the block at the face you clicked,
 * and the wrench turns or takes the block depending on whether you are sneaking.
 */
public class ConfiguratorItem extends Item {

    public ConfiguratorItem(Properties properties) {
        super(properties);
    }

    public static ConfiguratorMode mode(ItemStack stack) {
        return stack.getOrDefault(UTDataComponents.CONFIGURATOR_MODE.get(), ConfiguratorMode.CONFIGURATE_ITEM);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            ConfiguratorMode next = mode(stack).next();
            stack.set(UTDataComponents.CONFIGURATOR_MODE.get(), next);
            level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundSource.PLAYERS, 0.4F, 1.0F + next.ordinal() * 0.12F);
            player.displayClientMessage(Component.translatable(
                    "message.ultimatetransport.configurator_mode", next.label()), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return act(context);
    }

    /**
     * The whole interaction, reachable from the interact event as well as from the item. Machines that
     * take wrenches by tag will happily rotate themselves the moment they see one, so the event cancels
     * the click outright and calls straight in here rather than trusting the block to stand aside.
     */
    public static InteractionResult act(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ConfiguratorMode mode = mode(context.getItemInHand());

        if (mode.configurating()) {
            if (blockEntity instanceof CableBlockEntity cable) {
                return cable(context, level, state, cable, player, mode);
            }
            return configurate(level, pos, side, blockEntity, player, mode);
        }
        if (mode == ConfiguratorMode.EMPTY) {
            return empty(level, pos, side, blockEntity);
        }
        if (mode == ConfiguratorMode.ROTATE) {
            return rotate(level, pos, state, side, player);
        }
        if (mode == ConfiguratorMode.WRENCH) {
            if (!player.isShiftKeyDown()) {
                return rotate(level, pos, state, side, player);
            }
            return dismantlable(level, pos, state) ? dismantle(level, pos, state, player) : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    /**
     * What another mod's wrench does to one of our blocks: a click steps the clicked face, a sneaking
     * click takes the block back with everything in it. Only our own blocks answer, so a wrench keeps
     * behaving normally everywhere else.
     */
    public static InteractionResult wrenchOurs(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (player == null || !isOurs(state)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            return dismantlable(level, pos, state) ? dismantle(level, pos, state, player) : InteractionResult.PASS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CableBlockEntity cable) {
            return wrenchCable(context, level, state, cable, player);
        }
        if (blockEntity != null && SideConfigTarget.supports(blockEntity)) {
            return configurate(level, pos, context.getClickedFace(), blockEntity, player,
                    ConfiguratorMode.CONFIGURATE_ENERGY);
        }
        return rotate(level, pos, state, context.getClickedFace(), player);
    }

    /**
     * A foreign wrench has no mode dial, so on a universal cable it moves every cargo at once rather
     * than silently editing one of the five. The mod's own configurator, and the cable's own screen,
     * are still the way to set them apart.
     */
    private static InteractionResult wrenchCable(UseOnContext context, Level level, BlockState state,
                                                 CableBlockEntity cable, Player player) {
        if (cable.type() != TransferType.UNIVERSAL) {
            return cable(context, level, state, cable, player, ConfiguratorMode.CONFIGURATE_ITEM);
        }
        BlockPos pos = context.getClickedPos();
        Direction side = CableBlock.pickSide(state, pos, context.getClickLocation(), context.getClickedFace());
        if (!cable.container(side)) {
            return cable(context, level, state, cable, player, ConfiguratorMode.CONFIGURATE_ITEM);
        }
        ConnectionMode next = cable.config(side, TransferType.ENERGY).mode().next();
        for (TransferType cargo : cable.type().carried()) {
            cable.setSideMode(side, cargo, next);
        }
        cable.onConfigChanged();
        level.playSound(null, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.6F,
                next == ConnectionMode.EXTRACT ? 1.4F : 0.8F);
        announce(player, side, next.translationKey(), next.colour());
        return InteractionResult.CONSUME;
    }

    /** A click steps the face forward and a sneaking click steps it back, either way saying where it landed. */
    private static InteractionResult configurate(Level level, BlockPos pos, Direction side,
                                          @Nullable BlockEntity blockEntity, Player player, ConfiguratorMode mode) {
        if (blockEntity == null || !SideConfigTarget.supports(blockEntity)) {
            return InteractionResult.PASS;
        }
        TransferType cargo = mode.cargo();
        if (!SideConfigTarget.enabled(blockEntity, side, cargo)) {
            return InteractionResult.PASS;
        }
        if (!SideConfigTarget.handles(blockEntity, cargo)) {
            player.displayClientMessage(Component.translatable(
                            "message.ultimatetransport.configurator_unsupported", mode.label())
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.CONSUME;
        }
        List<SideConfigTarget.Option> options = SideConfigTarget.options(blockEntity, cargo);
        if (options.isEmpty()) {
            return InteractionResult.PASS;
        }
        SideConfigTarget.cycle(blockEntity, side, cargo, !player.isShiftKeyDown());
        level.playSound(null, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.6F,
                player.isShiftKeyDown() ? 0.8F : 1.2F);
        int index = SideConfigTarget.index(blockEntity, side, cargo);
        if (index < options.size()) {
            announce(player, side, options.get(index).translationKey(), options.get(index).colour());
        }
        return InteractionResult.CONSUME;
    }

    /** Cables answer a plain click the way Mekanism's transmitters do, without asking for a sneak. */
    private static InteractionResult cable(UseOnContext context, Level level, BlockState state,
                                    CableBlockEntity cable, Player player, ConfiguratorMode mode) {
        BlockPos pos = context.getClickedPos();
        Direction side = CableBlock.pickSide(state, pos, context.getClickLocation(), context.getClickedFace());

        BlockState facade = cable.facade(side);
        if (facade != null) {
            cable.setFacade(side, null);
            ItemStack recovered = FacadeItem.of(UTItems.FACADE.get(), facade);
            if (!player.addItem(recovered)) {
                Block.popResource(level, pos, recovered);
            }
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.7F, 1.0F);
            return InteractionResult.CONSUME;
        }

        if (level.getBlockEntity(pos.relative(side)) instanceof CableBlockEntity) {
            boolean severed = !cable.severed(side);
            cable.setSevered(side, severed);
            level.playSound(null, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.6F, severed ? 0.7F : 1.5F);
            player.displayClientMessage(Component.translatable("message.ultimatetransport.side_mode",
                    Component.translatable("ultimatetransport.direction." + side.getSerializedName()),
                    Component.translatable(severed
                                    ? "ultimatetransport.link.severed"
                                    : "ultimatetransport.link.joined")
                            .withStyle(style -> style.withColor(severed ? 0xE04C4C : 0x4CAF50))), true);
            return InteractionResult.CONSUME;
        }
        if (!cable.container(side)) {
            return InteractionResult.PASS;
        }

        TransferType cargo = mode.cargo() != null && cable.type().carries(mode.cargo())
                ? mode.cargo()
                : cable.primary();
        ConnectionMode next = cable.config(side, cargo).mode().next();
        cable.setSideMode(side, cargo, next);
        cable.onConfigChanged();

        level.playSound(null, pos, SoundEvents.COMPARATOR_CLICK,
                SoundSource.BLOCKS, 0.6F, next == ConnectionMode.EXTRACT ? 1.4F : 0.8F);
        announce(player, side, next.translationKey(), next.colour());
        return InteractionResult.CONSUME;
    }

    /** Empty mode drops whatever the block is holding out of the face you clicked. */
    private static InteractionResult empty(Level level, BlockPos pos, Direction side, @Nullable BlockEntity blockEntity) {
        if (blockEntity == null) {
            return InteractionResult.PASS;
        }
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        if (handler == null) {
            handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        }
        if (handler == null) {
            return InteractionResult.PASS;
        }
        boolean dropped = false;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack held = handler.getStackInSlot(slot);
            if (held.isEmpty()) {
                continue;
            }
            ItemStack taken = handler.extractItem(slot, held.getCount(), false);
            if (!taken.isEmpty()) {
                Block.popResourceFromFace(level, pos, side, taken);
                dropped = true;
            }
        }
        if (!dropped) {
            return InteractionResult.PASS;
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.7F, 1.0F);
        return InteractionResult.CONSUME;
    }

    /** Points the block at the face you clicked, or away from it when sneaking. */
    private static InteractionResult rotate(Level level, BlockPos pos, BlockState state, Direction side, Player player) {
        Direction wanted = player.isShiftKeyDown() ? side.getOpposite() : side;
        for (DirectionProperty property : new DirectionProperty[]{
                BlockStateProperties.FACING, BlockStateProperties.HORIZONTAL_FACING}) {
            if (!state.hasProperty(property) || !property.getPossibleValues().contains(wanted)) {
                continue;
            }
            if (state.getValue(property) == wanted) {
                return InteractionResult.CONSUME;
            }
            level.setBlock(pos, state.setValue(property, wanted), 3);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 0.7F, 1.0F);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private static void announce(Player player, Direction side, String key, int colour) {
        player.displayClientMessage(Component.translatable("message.ultimatetransport.side_mode",
                Component.translatable("ultimatetransport.direction." + side.getSerializedName()),
                Component.translatable(key).withStyle(style -> style.withColor(colour))), true);
    }

    /** Whether a block at that spot is one of ours, for deciding if a foreign wrench should act. */
    public static boolean ourBlock(net.minecraft.world.level.LevelAccessor level, BlockPos pos) {
        return isOurs(level.getBlockState(pos));
    }

    private static boolean isOurs(BlockState state) {
        return state.getBlock() instanceof CableBlock
                || state.getBlock() instanceof EnergyCellBlock
                || state.getBlock() instanceof GeneratorBlock
                || state.getBlock() instanceof BlockTube
                || state.getBlock() instanceof BlockStation
                || state.getBlock() instanceof BlockStationHorizontal;
    }

    private static boolean dismantlable(Level level, BlockPos pos, BlockState state) {
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        return isOurs(state) || state.hasBlockEntity();
    }

    /**
     * Takes the block back into the hand, with everything it was carrying: a cable keeps its upgrades
     * and facades, a cell its charge, a pipe whatever was in flight.
     */
    private static InteractionResult dismantle(Level level, BlockPos pos, BlockState state, Player player) {
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.PASS;
        }
        if (!player.mayBuild() || !level.mayInteract(player, pos)) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer breaker
                && CommonHooks.fireBlockBreak(level, breaker.gameMode.getGameModeForPlayer(), breaker, pos, state)
                .isCanceled()) {
            return InteractionResult.PASS;
        }
        List<ItemStack> drops = Block.getDrops(state, server, pos, level.getBlockEntity(pos), player, ItemStack.EMPTY);
        level.removeBlock(pos, false);
        for (ItemStack drop : drops) {
            if (!player.addItem(drop)) {
                Block.popResource(level, pos, drop);
            }
        }
        level.playSound(null, pos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 0.8F, 1.1F);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ultimatetransport.configurator.mode", mode(stack).label())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ultimatetransport.configurator").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ultimatetransport.configurator.sneak").withStyle(ChatFormatting.DARK_GRAY));
    }
}
