package nadiendev.ultimatetransport.registry;

import nadiendev.ultimatetransport.UltimateTransport;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class UTTags {
    public static final TagKey<Block> MACHINES =
            TagKey.create(Registries.BLOCK, UltimateTransport.id("machines"));

    private UTTags() {
    }
}
