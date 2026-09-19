package nadiendev.ultimatetransport.cable;

import nadiendev.ultimatetransport.api.TransferType;
import net.minecraft.core.Direction;

/**
 * Sends a face to sleep once it has come up empty often enough. A cable with nowhere to put its
 * cargo walks its whole target list every time it is asked, and the answer does not change while
 * nothing around it moves, so on a server running accelerated ticks the same fruitless walk can be
 * repeated thousands of times a second until the tick loop drowns in it.
 *
 * <p>The wait is measured against the wall clock through {@link System#nanoTime()} rather than a
 * tick count, precisely so that a mod handing out extra ticks cannot shorten it: however many ticks
 * arrive, the face stays quiet for the same two seconds.</p>
 */
public final class TransferPause {

    /** Empty runs in a row before the face is put to sleep. */
    public static final int MISSES_BEFORE_PAUSE = 10;

    /** How long a sleeping face stays quiet, in real seconds. */
    public static final long PAUSE_MILLIS = 2000L;

    private static final int TYPES = TransferType.values().length;

    private final int[] misses = new int[Direction.values().length * TYPES];
    private final boolean[] sleeping = new boolean[Direction.values().length * TYPES];
    private final long[] wakeAt = new long[Direction.values().length * TYPES];

    private static int index(Direction side, TransferType cargo) {
        return side.ordinal() * TYPES + cargo.ordinal();
    }

    /** False while the face is still sleeping off its last run of empty attempts. */
    public boolean ready(Direction side, TransferType cargo) {
        int slot = index(side, cargo);
        if (!sleeping[slot]) {
            return true;
        }
        if (System.nanoTime() - wakeAt[slot] < 0L) {
            return false;
        }
        sleeping[slot] = false;
        return true;
    }

    /** Something was carried: the face is awake and starts counting again from zero. */
    public void moved(Direction side, TransferType cargo) {
        int slot = index(side, cargo);
        misses[slot] = 0;
        sleeping[slot] = false;
    }

    /** Nothing was carried. Enough of these in a row and the face goes quiet. */
    public void missed(Direction side, TransferType cargo) {
        int slot = index(side, cargo);
        if (++misses[slot] < MISSES_BEFORE_PAUSE) {
            return;
        }
        misses[slot] = 0;
        sleeping[slot] = true;
        wakeAt[slot] = System.nanoTime() + PAUSE_MILLIS * 1_000_000L;
    }

    /** Wakes every face, for when the network around the cable has changed under it. */
    public void wakeAll() {
        java.util.Arrays.fill(misses, 0);
        java.util.Arrays.fill(sleeping, false);
    }
}
