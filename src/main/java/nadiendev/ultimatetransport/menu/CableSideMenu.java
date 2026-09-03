package nadiendev.ultimatetransport.menu;

import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.api.TransferType;
import net.minecraft.core.Direction;

/** Both cable screens address one face of one cable, and every packet is checked against that. */
public interface CableSideMenu {

    CableBlockEntity cable();

    Direction side();

    TransferType cargo();
}
