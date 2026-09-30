package cam.rao.mixin;

import cam.rao.Camerao;
import cam.rao.CameraDuck;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
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

    /**
     * 1.21.11 folds the old alignWithEntity into {@code setup}. The main (non-minecart)
     * path sets rotation, then the eye position via the single setPosition(DDD) call site.
     * Hooking right after that call is a stable point where vanilla rotation/position are
     * final and the detached back-off move hasn't run yet. (setRotation ordinals are
     * unreliable here: ordinal 2 only executes in detached third-person-reverse mode.)
     */
    @Inject(method = "setup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", shift = At.Shift.AFTER))
    public void camerao$lockRotationAndPark(Level level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        if (Camerao.isCamDetached) {
            // Detached camera: park position and rotation exactly where the player detached.
            this.setRotation(Camerao.detachedYRot, Camerao.detachedXRot);
            this.setPosition(Camerao.detachedX, Camerao.detachedY, Camerao.detachedZ);
        } else if (Camerao.isPerspectiveActive && this.entity instanceof LocalPlayer) {
            CameraDuck overridden = (CameraDuck) this.entity;

            if (camerao$firstTime && Minecraft.getInstance().player != null) {
                // Seed the detached rotation from the player's real rotation on activation.
                overridden.camerao$setCameraPitch(Minecraft.getInstance().player.getXRot());
                overridden.camerao$setCameraYaw(Minecraft.getInstance().player.getYRot());
                camerao$firstTime = false;
            }

            this.setRotation(overridden.camerao$getCameraYaw(), overridden.camerao$getCameraPitch());
        } else if (this.entity instanceof LocalPlayer) {
            camerao$firstTime = true;
        }
    }

    /**
     * Free cam toggles instantly: collapse the eye-height interpolation.
     *
     * <p>This has to run in setup() because on this MC version Camera.tick() has no call
     * sites anywhere in the game. eyeHeightOld is only ever assigned in that dead method,
     * so it stays 0.0 forever and setup() interpolates the eye height from 0.0 toward the
     * camera entity's value on every frame:
     *
     *     Mth.lerp(f, entity.yo, entity.getY()) + Mth.lerp(f, eyeHeightOld, eyeHeight)
     *
     * Free cam's entity reports 0.4 (it locks the swimming pose) against the player's
     * ~1.62, so that interpolation is a permanent visible sweep both entering and leaving
     * free cam. Assigning both fields to the entity's real eye height removes it.
     *
     * <p>The release flag covers the exit, because setup() assigns {@code this.entity =
     * entity} on its first lines, so the outgoing free cam entity is no longer reachable
     * through the camera's own field at this point.
     */
    @Inject(method = "setup", at = @At("HEAD"))
    public void camerao$snapEyeHeight(Level level, Entity entity, boolean detached,
                                      boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        if (entity instanceof cam.rao.freecam.FreeCamEntity || Camerao.freeCamJustReleased) {
            Camerao.freeCamJustReleased = false;
            this.eyeHeightOld = this.eyeHeight = entity.getEyeHeight();
        }
    }

    @ModifyArg(method = "setup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"))
    public float camerao$modifyZoomDistance(float originalDistance) {
        if (Camerao.isPerspectiveActive && Camerao.config.isZoomOut()
                && Camerao.getActivePerspective() != net.minecraft.client.CameraType.FIRST_PERSON) {
            float min = Math.min(Camerao.config.getMinZoom(), Camerao.config.getMaxZoom());
            float max = Math.max(Camerao.config.getMinZoom(), Camerao.config.getMaxZoom());
            return Mth.clamp(Camerao.zoomDistance, min, max);
        }
        return originalDistance;
    }

    /**
     * Free cam toggles instantly. This MC version has no tickFov/fovModifier to snap, so
     * only the eye-height interpolation is collapsed; the position lerp is handled by
     * FreeCamEntity collapsing its own previous-position fields on spawn.
     *
     * <p>The outgoing entity has to be checked too, not just the incoming one: the eye
     * height eases toward the camera entity's value every tick, and free cam's entity
     * reports 0.4 against the player's ~1.62, so leaving free cam would otherwise sink
     * the camera by that difference and ease it back over several frames. That is the
     * unshift on exit.
     */
    /** Detached camera: the camera does not back off with the entity. */
    @Redirect(method = "setup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;move(FFF)V", ordinal = 0))
    public void camerao$noMoveWhenDetached(Camera instance, float f, float g, float h) {
        if (!Camerao.isCamDetached) {
            move(f, g, h);
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
