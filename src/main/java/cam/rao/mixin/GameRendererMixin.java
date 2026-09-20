package cam.rao.mixin;

import cam.rao.Camerao;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    /** NanoTime of the previous getFov call, used for framerate-independent zoom easing. */
    @Unique
    long camerao$lastFovNanos = 0L;

    /** No view bobbing while the camera is detached (it is parked, not moving). */
    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void camerao$noBobWhenDetached(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        if (Camerao.isCamDetached) {
            ci.cancel();
        }
    }

    /**
     * 1.21.11 computes FOV in GameRenderer.getFov. Ease the zoom FOV multiplier with an
     * exponential step scaled by the real frame delta, so the zoom speed is identical at
     * any framerate. getFov runs several times per frame (level, HUD, projections), but
     * the ~zero delta between same-frame calls makes only the first call advance the ease.
     */
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    public void camerao$zoomFov(net.minecraft.client.Camera camera, float partialTick, boolean useFovSetting,
                                CallbackInfoReturnable<Float> cir) {
        float target = Camerao.isZooming ? 100.0F / Camerao.currentZoomMagnification : 1.0F;
        long now = System.nanoTime();
        long last = camerao$lastFovNanos;
        camerao$lastFovNanos = now;
        float deltaSeconds = last == 0L ? 1.0F / 60.0F
                : Mth.clamp((now - last) / 1.0E9F, 0.0F, 0.25F);
        float blend = 1.0F - (float) Math.exp(-Camerao.ZOOM_FOV_RATE * deltaSeconds);
        Camerao.zoomFovFactor = Mth.lerp(blend, Camerao.zoomFovFactor, target);
        if (Math.abs(Camerao.zoomFovFactor - 1.0F) < 0.001F && target == 1.0F) {
            Camerao.zoomFovFactor = 1.0F;
            camerao$lastFovNanos = 0L; // restart timing from a sane delta on the next zoom
            return;
        }
        cir.setReturnValue(cir.getReturnValueF() * Camerao.zoomFovFactor);
    }
}
