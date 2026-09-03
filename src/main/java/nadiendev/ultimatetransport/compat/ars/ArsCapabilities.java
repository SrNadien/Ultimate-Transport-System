package nadiendev.ultimatetransport.compat.ars;

import com.hollingsworth.arsnouveau.api.source.ISourceCap;
import nadiendev.ultimatetransport.compat.ModIds;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;

public final class ArsCapabilities {
    public static final BlockCapability<ISourceCap, Direction> SOURCE = BlockCapability.createSided(
            ResourceLocation.fromNamespaceAndPath(ModIds.ARS_NOUVEAU, "source"), ISourceCap.class);

    private ArsCapabilities() {
    }
}
