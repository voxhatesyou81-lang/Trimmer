package dev.trimmer;

/**
 * Counters so you can verify the optimizations are really doing something.
 * All updated on the client main thread only.
 */
public final class TrimmerStats {
    public static long throttledFrames;
    public static long particlesDroppedDistance;
    public static long particlesDroppedBudget;
    public static long cleanupsRun;

    private TrimmerStats() {
    }

    public static String summary() {
        return "throttledFrames=" + throttledFrames
                + " particlesDroppedDistance=" + particlesDroppedDistance
                + " particlesDroppedBudget=" + particlesDroppedBudget
                + " cleanupsRun=" + cleanupsRun;
    }
}
