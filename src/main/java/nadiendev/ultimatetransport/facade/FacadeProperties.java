package nadiendev.ultimatetransport.facade;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public final class FacadeProperties {

    /** Six entries indexed by {@code Direction.ordinal()}; null means the face is bare. */
    public static final ModelProperty<BlockState[]> FACADES = new ModelProperty<>();

    private FacadeProperties() {
    }
}
