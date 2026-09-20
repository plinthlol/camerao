package cam.rao.mixin;

import cam.rao.Camerao;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Keeps the crosshair on screen while in free cam.
 * Activating free cam switches the camera to third person (so you can see your own body),
 * and vanilla only draws the crosshair in first person - that check is the early return
 * this hook overrides.
 */
@Mixin(Hud.class)
public class HudCrosshairMixin {
    @Redirect(method = "extractCrosshair", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean camerao$showCrosshairInFreeCam(CameraType cameraType) {
        return Camerao.isFreeCam || cameraType.isFirstPerson();
    }
}
