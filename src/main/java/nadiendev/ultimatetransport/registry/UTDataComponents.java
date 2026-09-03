package nadiendev.ultimatetransport.registry;

import com.mojang.serialization.Codec;
import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.filter.DirectionalPosition;
import nadiendev.ultimatetransport.item.ConfiguratorMode;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class UTDataComponents {

    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, UltimateTransport.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> STORED_ENERGY =
            COMPONENTS.registerComponentType("stored_energy", builder -> builder
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockState>> FACADE_BLOCK =
            COMPONENTS.registerComponentType("facade_block", builder -> builder
                    .persistent(BlockState.CODEC)
                    .networkSynchronized(ByteBufCodecs.idMapper(
                            Block.BLOCK_STATE_REGISTRY::byId, Block.BLOCK_STATE_REGISTRY::getId)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DirectionalPosition>> DESTINATION =
            COMPONENTS.registerComponentType("destination", builder -> builder
                    .persistent(DirectionalPosition.CODEC)
                    .networkSynchronized(DirectionalPosition.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ConfiguratorMode>> CONFIGURATOR_MODE =
            COMPONENTS.registerComponentType("configurator_mode", builder -> builder
                    .persistent(StringRepresentable.fromEnum(() -> ConfiguratorMode.VALUES))
                    .networkSynchronized(ByteBufCodecs.idMapper(
                            id -> ConfiguratorMode.VALUES[id], ConfiguratorMode::ordinal)));

    private UTDataComponents() {
    }
}
