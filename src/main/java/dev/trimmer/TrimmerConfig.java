package dev.trimmer;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Plain-properties config at {@code config/trimmer.properties}. Written with defaults on first launch.
 * Values are clamped so a typo can never produce something harmful (e.g. a 0 FPS cap).
 */
public final class TrimmerConfig {
    private static final Logger LOG = LoggerFactory.getLogger("Trimmer");

    public static volatile boolean backgroundThrottle = true;
    public static volatile int backgroundFps = 15;
    public static volatile int idleAfterSeconds = 60;
    public static volatile int idleFps = 5;

    public static volatile boolean particleTrim = true;
    public static volatile int particlesPerTick = 500;
    public static volatile double particleMaxDistance = 80.0;

    public static volatile boolean postSessionCleanup = true;

    public static volatile boolean logStats = false;

    private TrimmerConfig() {
    }

    public static void load() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("trimmer.properties");
        if (Files.exists(file)) {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(file)) {
                p.load(in);
            } catch (IOException | IllegalArgumentException e) {
                LOG.warn("Could not read {}, using defaults", file, e);
                return;
            }
            backgroundThrottle = bool(p, "backgroundThrottle", backgroundThrottle);
            backgroundFps = clamp(integer(p, "backgroundFps", backgroundFps), 5, 240);
            idleAfterSeconds = clamp(integer(p, "idleAfterSeconds", idleAfterSeconds), 5, 86400);
            idleFps = clamp(integer(p, "idleFps", idleFps), 5, 240);

            particleTrim = bool(p, "particleTrim", particleTrim);
            particlesPerTick = clamp(integer(p, "particlesPerTick", particlesPerTick), 50, 100000);
            particleMaxDistance = Math.max(0.0, Math.min(1024.0, dbl(p, "particleMaxDistance", particleMaxDistance)));

            postSessionCleanup = bool(p, "postSessionCleanup", postSessionCleanup);
            logStats = bool(p, "logStats", logStats);
        } else {
            writeDefaults(file);
        }
    }

    private static void writeDefaults(Path file) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("# Trimmer configuration. Restart the game after editing.").append(nl).append(nl);

        sb.append("# Cap FPS while the game window is not focused (alt-tabbed). Saves CPU/GPU/power.").append(nl);
        sb.append("# NOTE: if you record or stream with a game-capture source while unfocused, set this to false.").append(nl);
        sb.append("backgroundThrottle=").append(backgroundThrottle).append(nl);
        sb.append("# FPS cap while unfocused (5-240).").append(nl);
        sb.append("backgroundFps=").append(backgroundFps).append(nl);
        sb.append("# After this many seconds unfocused, drop to idleFps instead.").append(nl);
        sb.append("idleAfterSeconds=").append(idleAfterSeconds).append(nl);
        sb.append("idleFps=").append(idleFps).append(nl).append(nl);

        sb.append("# Limit how many particles may be added to the client per tick (spike protection for").append(nl);
        sb.append("# explosions, fireworks, mob-farm effects). Dropped particles are purely cosmetic.").append(nl);
        sb.append("particleTrim=").append(particleTrim).append(nl);
        sb.append("# Maximum particles added per client tick (50-100000).").append(nl);
        sb.append("particlesPerTick=").append(particlesPerTick).append(nl);
        sb.append("# Drop particles farther than this many blocks from the camera. Vanilla already culls most").append(nl);
        sb.append("# particles beyond 32 blocks; this catches force-spawned ones that bypass that check. 0 = off.").append(nl);
        sb.append("particleMaxDistance=").append(particleMaxDistance).append(nl).append(nl);

        sb.append("# Run one garbage collection a few seconds after leaving a world, back at the menu,").append(nl);
        sb.append("# so the heap shrinks instead of holding the old world's garbage. No effect during play.").append(nl);
        sb.append("postSessionCleanup=").append(postSessionCleanup).append(nl).append(nl);

        sb.append("# Log a one-line summary of what Trimmer has done to latest.log every 60 seconds.").append(nl);
        sb.append("logStats=").append(logStats).append(nl);
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.warn("Could not write default config {}", file, e);
        }
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }

    private static int integer(Properties p, String key, int def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static double dbl(Properties p, String key, double def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
