package nadiendev.ultimatetransport.network;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.menu.CableSideMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = UltimateTransport.MODID)
public final class UTNetwork {

    private UTNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(CableActionPayload.TYPE, CableActionPayload.STREAM_CODEC, CableActionPayload::handle);
        registrar.playToServer(EditFilterPayload.TYPE, EditFilterPayload.STREAM_CODEC, EditFilterPayload::handle);
        registrar.playToServer(RemoveFilterPayload.TYPE, RemoveFilterPayload.STREAM_CODEC, RemoveFilterPayload::handle);
        registrar.playToServer(CellActionPayload.TYPE, CellActionPayload.STREAM_CODEC, CellActionPayload::handle);
        registrar.playToServer(SideConfigPayload.TYPE, SideConfigPayload.STREAM_CODEC, SideConfigPayload::handle);
        registrar.playToServer(ConfiguratorModePayload.TYPE, ConfiguratorModePayload.STREAM_CODEC,
                ConfiguratorModePayload::handle);
    }

    /**
     * A configuration packet is only honoured while the sender actually has that cable face open,
     * which keeps the whole side-config surface behind the same check as the menu itself.
     */
    @Nullable
    static CableBlockEntity verify(IPayloadContext context, BlockPos pos, int side) {
        if (side < 0 || side >= Direction.values().length) {
            return null;
        }
        Player player = context.player();
        if (!(player.containerMenu instanceof CableSideMenu menu)
                || !(player.containerMenu instanceof AbstractContainerMenu container)) {
            return null;
        }
        if (!container.stillValid(player)
                || !menu.cable().getBlockPos().equals(pos)
                || menu.side() != Direction.values()[side]) {
            return null;
        }
        return menu.cable();
    }

    static TransferType cargoOf(IPayloadContext context) {
        return context.player().containerMenu instanceof CableSideMenu menu
                ? menu.cargo()
                : TransferType.ENERGY;
    }
}
