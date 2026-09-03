package nadiendev.ultimatetransport.network;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.item.ConfiguratorItem;
import nadiendev.ultimatetransport.item.ConfiguratorMode;
import nadiendev.ultimatetransport.registry.UTDataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * A mode change asked for from the client: a step when the wheel is turned with the configurator in
 * hand, or an outright pick when one is chosen from the wheel menu.
 */
public record ConfiguratorModePayload(int step, int pick) implements CustomPacketPayload {

    public static final int NO_PICK = -1;

    public static final Type<ConfiguratorModePayload> TYPE = new Type<>(UltimateTransport.id("configurator_mode"));

    public static final StreamCodec<FriendlyByteBuf, ConfiguratorModePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ConfiguratorModePayload::step,
            ByteBufCodecs.VAR_INT, ConfiguratorModePayload::pick,
            ConfiguratorModePayload::new);

    public static ConfiguratorModePayload step(int step) {
        return new ConfiguratorModePayload(step, NO_PICK);
    }

    public static ConfiguratorModePayload pick(ConfiguratorMode mode) {
        return new ConfiguratorModePayload(0, mode.ordinal());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ConfiguratorModePayload payload, IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = held(player);
        if (stack.isEmpty()) {
            return;
        }
        ConfiguratorMode current = ConfiguratorItem.mode(stack);
        ConfiguratorMode next;
        if (payload.pick() >= 0 && payload.pick() < ConfiguratorMode.VALUES.length) {
            next = ConfiguratorMode.VALUES[payload.pick()];
            if (!next.available()) {
                return;
            }
        } else if (payload.step() == 0) {
            return;
        } else {
            next = payload.step() > 0 ? current.next() : current.previous();
        }
        if (next == current) {
            return;
        }
        stack.set(UTDataComponents.CONFIGURATOR_MODE.get(), next);
        player.level().playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(),
                SoundSource.PLAYERS, 0.3F, 1.0F + next.ordinal() * 0.1F);
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                "message.ultimatetransport.configurator_mode", next.label()), true);
    }

    private static ItemStack held(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof ConfiguratorItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
