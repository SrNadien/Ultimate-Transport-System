package nadiendev.ultimatetransport.compat.jade;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.cable.CableBlock;
import nadiendev.ultimatetransport.cell.EnergyCellBlock;
import nadiendev.ultimatetransport.tube.block.BlockTube;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/** Puts what a cable, pipe or cell is doing into the Jade box, so it reads without opening anything. */
@WailaPlugin(UltimateTransport.MODID)
public class UTJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CableInfo.INSTANCE, CableBlock.class);
        registration.registerBlockComponent(CellInfo.INSTANCE, EnergyCellBlock.class);
        registration.registerBlockComponent(TubeInfo.INSTANCE, BlockTube.class);
    }
}
