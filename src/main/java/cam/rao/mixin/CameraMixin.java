package cam.rao.mixin;

import cam.rao.Camerao;
import cam.rao.CameraDuck;
import cam.rao.freecam.FreeCamEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Unique
    boolean camerao$firstTime = true;

    /** NanoTime of the previous calculateFov call, used for framerate-independent zoom easing. */
    @Unique
    long camerao$lastFovNanos = 0L;

    @Shadow
    private Entity entity;

    @Shadow
    private float eyeHeight;

    @Shadow
    private float eyeHeightOld;

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void move(float f, float g, float h);

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Inject(method = "alignWithEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 1, shift = At.Shift.AFTER))
    public void camerao$lockRotation(float f, CallbackInfo ci) {
        if (Camerao.isPerspectiveActive && this.entity instanceof LocalPlayer) {
            CameraDuck overridden = (CameraDuck) this.entity;

            if (camerao$firstTime && Minecraft.getInstance().player != null) {
                // Seed the detached rotation from the player's real rotation on activation.
                overridden.camerao$setCameraPitch(Minecraft.getInstance().player.getXRot());
                overridden.camerao$setCameraYaw(Minecraft.getInstance().player.getYRot());
                camerao$firstTime = false;
            }

            this.setRotation(overridden.camerao$getCameraYaw(), overridden.camerao$getCameraPitch());
        }
        if (!Camerao.isPerspectiveActive && this.entity instanceof LocalPlayer) {
            camerao$firstTime = true;
        }
    }

    @ModifyArg(method = "alignWithEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"))
    public float camerao$modifyZoomDistance(float originalDistance) {
        if (Camerao.isPerspectiveActive && Camerao.config.isZoomOut()
                && Camerao.getActivePerspective() != CameraType.FIRST_PERSON) {
            float min = Math.min(Camerao.config.getMinZoom(), Camerao.config.getMaxZoom());
            float max = Math.max(Camerao.config.getMinZoom(), Camerao.config.getMaxZoom());
            return Mth.clamp(Camerao.zoomDistance, min, max);
        }
        return originalDistance;
    }

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    public void camerao$zoomFov(float partialTick, CallbackInfoReturnable<Float> cir) {
        float target = Camerao.isZooming ? 100.0F / Camerao.currentZoomMagnification : 1.0F;
        // Ease the FOV multiplier toward the target with an exponential step scaled by
        // the real frame delta, so the zoom speed is identical at any framerate.
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

    /** When the camera entity switches to/from the free cam, snap the eye height instantly. */
    @Inject(method = "setEntity", at = @At("HEAD"))
    public void camerao$onSetEntity(Entity entity, CallbackInfo ci) {
        if (entity == null || this.entity == null) {
            return;
        }
        if (entity instanceof FreeCamEntity || this.entity instanceof FreeCamEntity) {
            this.eyeHeightOld = this.eyeHeight = entity.getEyeHeight();
        }
    }

    /** Detached camera: the camera does not back off with the entity. */
    @Redirect(method = "alignWithEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;move(FFF)V", ordinal = 0))
    public void camerao$noMoveWhenDetached(Camera instance, float f, float g, float h) {
        if (!Camerao.isCamDetached) {
            move(f, g, h);
        }
    }

    /** Detached camera: park position and rotation exactly where the player detached. */
    @Inject(method = "alignWithEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", shift = At.Shift.AFTER))
    public void camerao$detachPosition(CallbackInfo ci) {
        if (Camerao.isCamDetached) {
            setRotation(Camerao.detachedYRot, Camerao.detachedXRot);
            setPosition(Camerao.detachedX, Camerao.detachedY, Camerao.detachedZ);
        }
    }

    /** Removes the underwater/lava overlay while in free cam. */
    @Inject(method = "getFluidInCamera", at = @At("HEAD"), cancellable = true)
    public void camerao$noSubmersionFog(CallbackInfoReturnable<FogType> cir) {
        if (Camerao.isFreeCam) {
            cir.setReturnValue(FogType.NONE);
        }
    }
}
