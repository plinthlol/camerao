package cam.rao.mixin;

import cam.rao.Camerao;
import cam.rao.CameraDuck;
import cam.rao.freecam.FreeCam;
import cam.rao.freecam.Freecam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin implements CameraDuck {
    @Unique
    private float camerao$cameraPitch;

    @Unique
    private float camerao$cameraYaw;

    @Unique
    private float camerao$anchorYaw;

    @Unique
    private boolean camerao$hasAnchor = false;

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    public void camerao$changeCameraLookDirection(double xDelta, double yDelta, CallbackInfo ci) {
        if (Camerao.isFreeCam && (Object) this == Minecraft.getInstance().player) {
            // Route mouse look to the free cam instead of the player.
            FreeCam freeCam = Freecam.getFreeCam();
            if (freeCam != null) {
                freeCam.turn(xDelta * Camerao.zoomFovFactor, yDelta * Camerao.zoomFovFactor);
            }
            ci.cancel();
        } else if (Camerao.isPerspectiveActive && (Object) this instanceof LocalPlayer) {
            double pitchDelta = yDelta * 0.15 * Camerao.zoomFovFactor;
            double yawDelta = xDelta * 0.15 * Camerao.zoomFovFactor;

            if (Camerao.config.isInvertY()) {
                pitchDelta = -pitchDelta;
            }

            if (!camerao$hasAnchor) {
                camerao$anchorYaw = this.camerao$cameraYaw;
                camerao$hasAnchor = true;
            }

            this.camerao$cameraPitch = Mth.clamp(
                    this.camerao$cameraPitch + (float) pitchDelta,
                    -Camerao.config.getMaxPitch(),
                    Camerao.config.getMaxPitch());

            if (Camerao.config.getMaxYaw() >= 360) {
                this.camerao$cameraYaw += (float) yawDelta;
            } else {
                this.camerao$cameraYaw = Mth.clamp(
                        this.camerao$cameraYaw + (float) yawDelta,
                        camerao$anchorYaw - Camerao.config.getMaxYaw(),
                        camerao$anchorYaw + Camerao.config.getMaxYaw());
            }

            ci.cancel();
        } else if (camerao$hasAnchor) {
            camerao$hasAnchor = false;
        }
    }

    /** Prevents the player and the free cam entity from pushing each other. */
    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    public void camerao$noPush(Entity entity, CallbackInfo ci) {
        if (Camerao.isFreeCam || Camerao.isCamDetached) {
            FreeCam drone = Camerao.getActiveDrone();
            if (entity == drone || (Object) this == drone) {
                ci.cancel();
            }
        }
    }

    /** While the zoom key is held, scale mouse input so aiming speed matches the zoomed view. */
    @ModifyVariable(method = "turn", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public double camerao$scaleZoomSensitivityYaw(double value) {
        if (!Camerao.isPerspectiveActive && Camerao.zoomFovFactor != 1.0F
                && (Object) this instanceof LocalPlayer) {
            return value * Camerao.zoomFovFactor;
        }
        return value;
    }

    @ModifyVariable(method = "turn", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    public double camerao$scaleZoomSensitivityPitch(double value) {
        if (Camerao.zoomFovFactor != 1.0F && (Object) this instanceof LocalPlayer && !Camerao.isPerspectiveActive) {
            return value * Camerao.zoomFovFactor;
        }
        return value;
    }

    @Override
    @Unique
    public float camerao$getCameraPitch() {
        return this.camerao$cameraPitch;
    }

    @Override
    @Unique
    public float camerao$getCameraYaw() {
        return this.camerao$cameraYaw;
    }

    @Override
    @Unique
    public void camerao$setCameraPitch(float pitch) {
        this.camerao$cameraPitch = pitch;
    }

    @Override
    @Unique
    public void camerao$setCameraYaw(float yaw) {
        this.camerao$cameraYaw = yaw;
        this.camerao$anchorYaw = yaw;
        this.camerao$hasAnchor = true;
    }
}
