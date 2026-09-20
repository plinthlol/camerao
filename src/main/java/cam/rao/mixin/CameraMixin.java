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
