package nadiendev.ultimatetransport.cell;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cells of the same rung that touch each other work as one bank: their room and their charge add up,
 * and whatever arrives at any face lands in the emptiest block of the group.
 */
public final class CellBank {

    public static final int LIMIT = 512;

    private static int revision;

    private CellBank() {
    }

    /** Any placement or break makes every cached group stale, whichever group it belonged to. */
    public static void invalidate() {
        revision++;
    }

    public static int revision() {
        return revision;
    }

    public static List<EnergyCellBlockEntity> gather(EnergyCellBlockEntity root) {
        List<EnergyCellBlockEntity> members = new ArrayList<>();
        members.add(root);
        Level level = root.getLevel();
        if (level == null) {
            return members;
        }
        EnergyCellTier tier = root.tier();
        Set<BlockPos> seen = new HashSet<>();
        seen.add(root.getBlockPos());
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(root.getBlockPos());

        while (!queue.isEmpty() && members.size() < LIMIT) {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (!seen.add(next) || !level.isLoaded(next)) {
                    continue;
                }
                if (level.getBlockEntity(next) instanceof EnergyCellBlockEntity other
                        && other.tier() == tier
                        && !other.isRemoved()) {
                    members.add(other);
                    queue.add(next);
                    if (members.size() >= LIMIT) {
                        break;
                    }
                }
            }
        }
        return members;
    }

    public static long capacity(List<EnergyCellBlockEntity> members) {
        EnergyCellTier tier = members.get(0).tier();
        if (tier.bottomless()) {
            return Long.MAX_VALUE;
        }
        return saturate(tier.capacity(), members.size());
    }

    public static long stored(List<EnergyCellBlockEntity> members) {
        long total = 0L;
        for (EnergyCellBlockEntity member : members) {
            long value = member.ownStored();
            if (Long.MAX_VALUE - total < value) {
                return Long.MAX_VALUE;
            }
            total += value;
        }
        return total;
    }

    public static int transfer(List<EnergyCellBlockEntity> members) {
        return (int) Math.min(saturate(members.get(0).tier().transfer(), members.size()), Integer.MAX_VALUE);
    }

    private static long saturate(long each, int count) {
        if (each <= 0 || count <= 0) {
            return 0L;
        }
        return each > Long.MAX_VALUE / count ? Long.MAX_VALUE : each * count;
    }

    /**
     * Levels the whole bank, the way Ender IO's capacitor banks even themselves out on a timer, so a
     * full block placed against an empty one shares out without waiting for something to draw.
     */
    public static void equalise(List<EnergyCellBlockEntity> members) {
        if (members.size() < 2 || members.get(0).tier().bottomless()) {
            return;
        }
        long total = 0L;
        for (EnergyCellBlockEntity member : members) {
            total += member.ownStored();
        }
        long each = total / members.size();
        long remainder = total - each * members.size();
        for (EnergyCellBlockEntity member : members) {
            long share = each + (remainder > 0 ? 1 : 0);
            if (remainder > 0) {
                remainder--;
            }
            if (member.ownStored() != share) {
                member.setStored(share);
                member.setChanged();
            }
        }
    }

    /** Fills the emptiest blocks first, so a bank charges level rather than front to back. */
    public static long receive(List<EnergyCellBlockEntity> members, long amount, boolean simulate) {
        if (amount <= 0) {
            return 0L;
        }
        List<EnergyCellBlockEntity> order = new ArrayList<>(members);
        order.sort((left, right) -> Long.compare(left.ownStored(), right.ownStored()));
        long remaining = amount;
        long accepted = 0L;
        for (EnergyCellBlockEntity member : order) {
            long moved = member.receiveOwn(remaining, simulate);
            accepted += moved;
            remaining -= moved;
            if (remaining <= 0) {
                break;
            }
        }
        return accepted;
    }

    /** Drains the fullest blocks first, which keeps the bank level on the way down too. */
    public static long extract(List<EnergyCellBlockEntity> members, long amount, boolean simulate) {
        if (amount <= 0) {
            return 0L;
        }
        List<EnergyCellBlockEntity> order = new ArrayList<>(members);
        order.sort((left, right) -> Long.compare(right.ownStored(), left.ownStored()));
        long remaining = amount;
        long removed = 0L;
        for (EnergyCellBlockEntity member : order) {
            long moved = member.extractOwn(remaining, simulate);
            removed += moved;
            remaining -= moved;
            if (remaining <= 0) {
                break;
            }
        }
        return removed;
    }
}
