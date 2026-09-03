package nadiendev.ultimatetransport.cable;

import nadiendev.ultimatetransport.api.TransferType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Destination lookup for a cable network.
 *
 * <p>There is no persistent graph: an extracting side walks the cables it is joined to and collects
 * every face set to insert. The walk is cheap and its result is cached on the extracting block entity
 * until {@link #invalidate()} is called, which happens whenever a cable, a side configuration or a
 * neighbouring block changes.
 */
public final class CableNetwork {

    /** Ceiling on how many cables one walk visits, so a runaway network cannot stall the server. */
    public static final int MAX_CABLES = 4096;

    private static int version = 0;

    private CableNetwork() {
    }

    public record Target(BlockPos cable, Direction side, int priority, int distance) {

        public BlockPos destination() {
            return cable.relative(side);
        }

        public Direction face() {
            return side.getOpposite();
        }
    }

    public static int version() {
        return version;
    }

    public static void invalidate() {
        version++;
    }

    public static List<Target> findTargets(Level level, BlockPos origin, Direction originSide,
                                           TransferType type, TransferType cargo) {
        BlockPos source = origin.relative(originSide);
        List<Target> targets = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        Deque<Integer> depths = new ArrayDeque<>();

        visited.add(origin);
        queue.add(origin);
        depths.add(0);

        while (!queue.isEmpty() && visited.size() <= MAX_CABLES) {
            BlockPos pos = queue.poll();
            int depth = depths.poll();

            if (!(level.getBlockEntity(pos) instanceof CableBlockEntity cable)) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                SideConfig config = cable.config(direction, cargo);
                if (!cable.linked(direction)) {
                    continue;
                }
                BlockPos next = pos.relative(direction);

                if (level.getBlockEntity(next) instanceof CableBlockEntity neighbour) {
                    if (!neighbour.type().connectsTo(cable.type())) {
                        continue;
                    }
                    if (visited.add(next)) {
                        queue.add(next);
                        depths.add(depth + 1);
                    }
                    continue;
                }

                if (config.mode() != ConnectionMode.INSERT || !cable.container(direction) || next.equals(source)) {
                    continue;
                }
                targets.add(new Target(pos, direction, config.priority(), depth + 1));
            }
        }

        targets.sort(Comparator.comparingInt(Target::priority).reversed()
                .thenComparingInt(Target::distance));
        return targets;
    }
}
