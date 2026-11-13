package dev.trimmer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Gate for particles being added to the client particle engine.
 * Particles are cosmetic and client-only, so dropping them cannot affect gameplay or servers.
 */
public final class ParticleGovernor {
    private static int addedThisTick;

    private ParticleGovernor() {
    }

    /** Called once per client tick to reset the per-tick budget. */
    public static void resetTick() {
        addedThisTick = 0;
    }

    /** @return true if the particle may be added, false to drop it. */
    public static boolean allow(Particle particle) {
        if (!TrimmerConfig.particleTrim) {
            return true;
        }

        double maxDist = TrimmerConfig.particleMaxDistance;
        if (maxDist > 0.0) {
            Entity camera = Minecraft.getInstance().getCameraEntity();
            if (camera != null) {
                Vec3 c = particle.getBoundingBox().getCenter();
                if (camera.distanceToSqr(c.x, c.y, c.z) > maxDist * maxDist) {
                    TrimmerStats.particlesDroppedDistance++;
                    return false;
                }
            }
        }

        if (++addedThisTick > TrimmerConfig.particlesPerTick) {
            TrimmerStats.particlesDroppedBudget++;
            return false;
        }
        return true;
    }
}
