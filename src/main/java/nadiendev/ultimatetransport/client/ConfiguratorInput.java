package nadiendev.ultimatetransport.client;

import com.mojang.blaze3d.platform.InputConstants;
import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.item.ConfiguratorItem;
import nadiendev.ultimatetransport.network.ConfiguratorModePayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Turning the wheel with the configurator in hand steps through its modes, and the mode key opens the
 * wheel to pick one outright.
 */
@EventBusSubscriber(modid = UltimateTransport.MODID, value = Dist.CLIENT)
public final class ConfiguratorInput {

    public static final KeyMapping MODE_MENU = new KeyMapping(
            "key.ultimatetransport.configurator_menu",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.ultimatetransport");

    private ConfiguratorInput() {
    }

    @EventBusSubscriber(modid = UltimateTransport.MODID, value = Dist.CLIENT,
            bus = EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        public static void register(RegisterKeyMappingsEvent event) {
            event.register(MODE_MENU);
        }
    }

    public static ItemStack held(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof ConfiguratorItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null || !player.isShiftKeyDown()) {
            return;
        }
        if (held(player).isEmpty()) {
            return;
        }
        double delta = event.getScrollDeltaY();
        if (delta == 0.0) {
            return;
        }
        PacketDistributor.sendToServer(ConfiguratorModePayload.step(delta > 0 ? -1 : 1));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null || event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        if (!MODE_MENU.matches(event.getKey(), event.getScanCode()) || held(player).isEmpty()) {
            return;
        }
        minecraft.setScreen(new ConfiguratorModeScreen());
    }
}
