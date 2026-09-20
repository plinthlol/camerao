package cam.rao.mixin;

import cam.rao.Camerao;
import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CameraType.class)
public class CameraTypeMixin {
    /** Pressing F5 while the camera is detached exits detach mode gracefully. */
    @Inject(method = "cycle", at = @At("HEAD"))
    public void camerao$onCycle(CallbackInfoReturnable<CameraType> cir) {
        if (Camerao.isCamDetached) {
            Camerao.exitDetach();
        }
    }
}
