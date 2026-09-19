package nadiendev.ultimatetransport.cable;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Keeps a push from looping back into itself. Pipes from other mods answer an insert by asking their
 * own network which neighbours can take the cargo, and a cable is one of those neighbours, so the two
 * can bounce a stack between them until the thread runs out of stack. Every push claims the cable it
 * runs on: a cable that is already pushing, or a chain that has grown past {@link #MAX_DEPTH} hops,
 * refuses the transfer instead of recursing.
 */
public final class TransferGuard {

    /** How many pipes one transfer may travel through before it counts as a loop. */
    private static final int MAX_DEPTH = 8;

    private static final ThreadLocal<Deque<Object>> ACTIVE = ThreadLocal.withInitial(ArrayDeque::new);

    private TransferGuard() {
    }

    /** False when this node is already pushing, in which case the caller must move nothing. */
    public static boolean enter(Object node) {
        Deque<Object> active = ACTIVE.get();
        if (active.size() >= MAX_DEPTH) {
            return false;
        }
        for (Object running : active) {
            if (running == node) {
                return false;
            }
        }
        active.push(node);
        return true;
    }

    /** Releases a node claimed by {@link #enter}. Always call it from a finally block. */
    public static void exit(Object node) {
        Deque<Object> active = ACTIVE.get();
        if (active.peek() == node) {
            active.pop();
            return;
        }
        active.remove(node);
    }
}
