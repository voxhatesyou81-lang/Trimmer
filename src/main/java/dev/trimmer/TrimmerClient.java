package dev.trimmer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public final class TrimmerClient implements ClientModInitializer {
    private static final Logger LOG = LoggerFactory.getLogger("Trimmer");

    /** Ticks to wait at the menu after leaving a world before cleaning up (-1 = nothing pending). */
    private int cleanupCountdown = -1;
    private long lastLogNanos = System.nanoTime();

    @Override
    public void onInitializeClient() {
        TrimmerConfig.load();

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (TrimmerConfig.postSessionCleanup) {
                cleanupCountdown = 100; // ~5 seconds, lets the old world finish unloading
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);

        LOG.info("Trimmer loaded (backgroundThrottle={}, particleTrim={}, postSessionCleanup={})",
                TrimmerConfig.backgroundThrottle, TrimmerConfig.particleTrim, TrimmerConfig.postSessionCleanup);
    }

    private void onTick(Minecraft client) {
        ParticleGovernor.resetTick();

        if (cleanupCountdown >= 0) {
            if (client.level != null) {
                cleanupCountdown = -1; // rejoined a world, skip
            } else if (--cleanupCountdown == 0) {
                cleanupCountdown = -1;
                TrimmerStats.cleanupsRun++;
                System.gc();
            }
        }

        if (TrimmerConfig.logStats) {
            long now = System.nanoTime();
            if (now - lastLogNanos >= TimeUnit.SECONDS.toNanos(60)) {
                lastLogNanos = now;
                LOG.info("Stats: {}", TrimmerStats.summary());
            }
        }
    }
}
