package cam.rao.mixin;

import cam.rao.Camerao;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    public void camerao$onScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci) {
        // With a screen open (inventory, chest, chat, ...), leave the scroll alone so
        // vanilla can route it to the GUI; cancelling here would break screen scrolling.
        if (Minecraft.getInstance().screen() != null) {
            return;
        }

        // While in free cam, scroll adjusts the fly speed.
        if (Camerao.isFreeCam) {
            // Default: scroll up = faster. Invert flips it.
            float exponent = (Camerao.config.isInvertScroll() ? -1.0F : 1.0F) * (float) yOffset;
            float speed = Camerao.freeCamSpeed * (float) Math.pow(1.15, exponent);
            Camerao.freeCamSpeed = Mth.clamp(speed,
                    Camerao.FREECAM_MIN_SPEED, Camerao.FREECAM_MAX_SPEED);
            ci.cancel();
            return;
        }

        // While perspective with scroll zoom enabled, scroll adjusts the camera distance.
        if (Camerao.isPerspectiveActive && Camerao.config.isZoomOut()
                && Camerao.getActivePerspective() != CameraType.FIRST_PERSON) {
            float min = Math.min(Camerao.config.getMinZoom(), Camerao.config.getMaxZoom());
            float max = Math.max(Camerao.config.getMinZoom(), Camerao.config.getMaxZoom());
            // Perspective: scroll down zooms out (camera further), scroll up zooms in.
            float direction = Camerao.config.isInvertScroll() ? 1.0F : -1.0F;
            Camerao.zoomDistance = Mth.clamp(Camerao.zoomDistance + direction * (float) yOffset, min, max);
            ci.cancel();
            return;
        }

        // While the zoom key is held, scroll adjusts the zoom strength for this session.
        // Steps are proportional so deep zooms change smoothly, and small deltas (trackpads)
        // only move the zoom a little.
        // Default: scroll down zooms in. Invert flips it.
        if (Camerao.isZooming) {
            float direction = Camerao.config.isInvertScroll() ? -1.0F : 1.0F;
            float magnification = Camerao.currentZoomMagnification
                    * (float) Math.pow(1.15, direction * yOffset);
            Camerao.currentZoomMagnification = Math.round(Math.clamp(magnification, 100F, 10000F));
            ci.cancel();
        }
    }
}
