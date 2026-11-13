package dev.trimmer.mixin;

import dev.trimmer.ParticleGovernor;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Drops cosmetic particles that exceed the per-tick budget or are absurdly far from the camera. */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Inject(method = "add(Lnet/minecraft/client/particle/Particle;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void trimmer$gate(Particle particle, CallbackInfo ci) {
        if (!ParticleGovernor.allow(particle)) {
            ci.cancel();
        }
    }
}
