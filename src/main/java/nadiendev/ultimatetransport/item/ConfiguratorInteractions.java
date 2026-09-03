package nadiendev.ultimatetransport.item;

import nadiendev.ultimatetransport.UltimateTransport;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * The configurator has to reach the block before the block does. It answers to the wrench tags, so a
 * machine that takes wrenches would rotate itself the moment it saw one in hand, and denying the block
 * its turn is not enough when the other mod handles the click from an event of its own.
 *
 * <p>Only the server takes the click outright. Cancelling it on the client stops the packet ever being
 * sent, and then nothing is configured anywhere.
 */
@EventBusSubscriber(modid = UltimateTransport.MODID)
public final class ConfiguratorInteractions {

    private ConfiguratorInteractions() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void beforeBlockUse(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getItemStack().getItem() instanceof ConfiguratorItem)) {
            return;
        }
        event.setUseBlock(TriState.FALSE);
        event.setUseItem(TriState.TRUE);
        if (event.getLevel().isClientSide) {
            return;
        }
        InteractionResult result = ConfiguratorItem.act(new UseOnContext(
                event.getLevel(), event.getEntity(), event.getHand(), event.getItemStack(), event.getHitVec()));
        event.setCanceled(true);
        event.setCancellationResult(result == InteractionResult.PASS ? InteractionResult.CONSUME : result);
    }
}
