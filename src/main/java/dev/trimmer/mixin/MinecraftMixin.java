package dev.trimmer.mixin;

import dev.trimmer.BackgroundThrottle;
import dev.trimmer.TrimmerStats;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lowers the frame limit while the window is unfocused. We only ever LOWER the value the game
 * computed, so this composes with anything else that adjusts the limit.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(method = "getFramerateLimit", at = @At("RETURN"), cancellable = true, require = 0)
    private void trimmer$backgroundLimit(CallbackInfoReturnable<Integer> cir) {
        int cap = BackgroundThrottle.currentCap();
        if (cap > 0 && cir.getReturnValueI() > cap) {
            cir.setReturnValue(cap);
            TrimmerStats.throttledFrames++;
        }
    }
}
