package cam.rao.mixin;

import cam.rao.Camerao;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class OptionsMixin {
    /** Prevents F5 perspective switching while in free cam (the camera is managed for you). */
    @Inject(method = "setCameraType", at = @At("HEAD"), cancellable = true)
    public void camerao$onSetCameraType(CallbackInfo ci) {
        if (Camerao.isFreeCam) {
            ci.cancel();
        }
    }
}
