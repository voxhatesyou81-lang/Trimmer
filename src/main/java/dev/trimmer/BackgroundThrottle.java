package dev.trimmer;

import net.minecraft.client.Minecraft;

import java.util.concurrent.TimeUnit;

/**
 * Decides the FPS cap to apply while the game window is unfocused.
 * Returns 0 when no extra cap should apply (window focused, or feature disabled).
 */
public final class BackgroundThrottle {
    private static long unfocusedSinceNanos = -1L;

    private BackgroundThrottle() {
    }

    public static int currentCap() {
        if (!TrimmerConfig.backgroundThrottle) {
            unfocusedSinceNanos = -1L;
            return 0;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.isWindowActive()) {
            unfocusedSinceNanos = -1L;
            return 0;
        }
        long now = System.nanoTime();
        if (unfocusedSinceNanos < 0L) {
            unfocusedSinceNanos = now;
        }
        long idleAfter = TimeUnit.SECONDS.toNanos(TrimmerConfig.idleAfterSeconds);
        return (now - unfocusedSinceNanos >= idleAfter) ? TrimmerConfig.idleFps : TrimmerConfig.backgroundFps;
    }
}
